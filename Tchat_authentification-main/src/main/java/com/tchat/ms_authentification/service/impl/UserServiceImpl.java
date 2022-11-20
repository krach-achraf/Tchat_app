package com.tchat.ms_authentification.service.impl;

import com.tchat.ms_authentification.bean.ConfirmationEmail;
import com.tchat.ms_authentification.bean.Role;
import com.tchat.ms_authentification.bean.User;
import com.tchat.ms_authentification.dao.UserDao;
import com.tchat.ms_authentification.dto.AdminDTO;
import com.tchat.ms_authentification.dto.UserDTO;
import com.tchat.ms_authentification.dto.UserSigninDTO;
import com.tchat.ms_authentification.dto.UserSignupDTO;
import com.tchat.ms_authentification.email.EmailSender;
import com.tchat.ms_authentification.feign.UsersClient;
import com.tchat.ms_authentification.mapper.UserMapperImpl;
import com.tchat.ms_authentification.service.facade.UserService;
import com.tchat.ms_authentification.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final RoleServiceImpl roleService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ConfirmationEmailServiceImpl emailService;
    private final EmailSender emailSender;
    private final UserMapperImpl userMapper;

    private final UsersClient usersClient;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String mail_address = "";

    @Value("${spring.mail.password}")
    private String mail_password = "";

    @Override
    public ResponseEntity<Object> signIn(UserSigninDTO user) {
        if(user.getUsernameOrEmail() != null && user.getPassword() != null){
            try {
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsernameOrEmail(), user.getPassword()));
            }catch (BadCredentialsException e){
                return new ResponseEntity<>("Bad credentials", HttpStatus.valueOf(403));
            }
            User u = loadUserByUsername(user.getUsernameOrEmail());
            String token = jwtUtil.generateToken(u);

            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            map.put("username", u.getUsername());
            return new ResponseEntity<>(map, HttpStatus.valueOf(200));
        }
        return new ResponseEntity<>("Empty request", HttpStatus.valueOf(400));
    }

    @Override
    @Transactional
    public ResponseEntity<String> signUp(UserSignupDTO user) {
        if(userDao.existsByEmail(user.getEmail()) || userDao.existsByUsername(user.getUsername()))
            return new ResponseEntity<>("User already exist", HttpStatus.valueOf(400));

        Role role = saveRole("ROLE_USER");
        User u = userMapper.fromSignupUser(user);
        u.setAuthorities(List.of(role));
        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        u.setPassword(encryptedPassword);
        userDao.save(u);

        user.setPassword(encryptedPassword);
        //System.setProperty("https.protocols", "TLSv1.3");
        usersClient.saveUser(user);

        sendMail(u);

        return new ResponseEntity<>("User created", HttpStatus.valueOf(201));
    }

    @Override
    public User loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userDao.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
        if (user == null)
            throw new UsernameNotFoundException("user not found");
        return user;
    }

    @Transactional
    @Override
    public ResponseEntity<String> confirmToken(String token){
        ConfirmationEmail confirmationEmail = emailService.findByToken(token);

        if (confirmationEmail == null)
            return new ResponseEntity<>("Token not found", HttpStatus.valueOf(405));

        if(confirmationEmail.getConfirmedAt() != null)
            return new ResponseEntity<>("Email already confirmed", HttpStatus.valueOf(406));

        LocalDateTime expiresAt = confirmationEmail.getExpiresAt();

        if (expiresAt.isBefore(LocalDateTime.now())){
            sendMail(confirmationEmail.getUser());
            return new ResponseEntity<>("Token expired", HttpStatus.valueOf(407));
        }

        emailService.setConfirmed(token, LocalDateTime.now());

        User user = confirmationEmail.getUser();
        userDao.updateLocking(user.getEmail(), false);

        usersClient.setLocking(user.getUsername(), false);
        return new ResponseEntity<>("Email confirmed", HttpStatus.valueOf(200) );
    }

    @Override
    @Transactional
    public void updateLocking(String username) {
        User user = userDao.findByUsername(username);
        user.setLocked(true);
        userDao.save(user);
        usersClient.setLocking(username, false);
    }

    @Override
    @Transactional
    public void changePassword(String email, String newPassword) {
        User user = userDao.findByEmail(email);
        String cryptedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(cryptedPassword);
        userDao.save(user);
        usersClient.updatePassword(user.getUsername(), cryptedPassword);
    }

    @Override
    public ResponseEntity<String> sendMailVerific(String email, int codeVerific) {
        if(userDao.existsByEmail(email)){
            Properties properties = new Properties();
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", "smtp.gmail.com");
            properties.put("mail.smtp.port", "587");
            Session session = Session.getInstance(properties, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication(){
                    return new PasswordAuthentication(mail_address, mail_password);
                }
            });
            Message message = prepareMessage(session, mail_address, email, codeVerific);
            try {
                Transport.send(message);
                return new ResponseEntity<>("Mail is sended", HttpStatus.valueOf(200));
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        }
        return new ResponseEntity<>("Email not found", HttpStatus.valueOf(404));
    }
    @Override
    public ResponseEntity<String> saveAdmin(AdminDTO adminDTO) {
        if(!userDao.existsByUsername(adminDTO.getUsername()) &&
                !userDao.existsByEmail(adminDTO.getEmail())){
            User admin = userMapper.fromAdmin(adminDTO);
            admin.setAuthorities(saveRolesAdmin(adminDTO.getAuthority()));
            userDao.save(admin);
            return new ResponseEntity<>("Admin created", HttpStatus.valueOf(201));
        }
        return new ResponseEntity<>("Username or email already exist", HttpStatus.valueOf(400));
    }

    @Override
    public ResponseEntity<String> updateAdmin(String username, AdminDTO adminDTO) {
        if(!userDao.existsByUsername(adminDTO.getUsername()) &&
                !userDao.existsByEmail(adminDTO.getEmail())){
            User admin = userDao.findByUsername(username);
            admin.setUsername(adminDTO.getUsername());
            admin.setEmail(adminDTO.getEmail());
            admin.setPassword(adminDTO.getPassword());
            admin.setLocked(adminDTO.isLocked());
            admin.setExpired(adminDTO.isExpired());
            admin.setAuthorities(saveRolesAdmin(adminDTO.getAuthority()));
            userDao.save(admin);
            return new ResponseEntity<>("Admin updated", HttpStatus.valueOf(201));
        }
        return new ResponseEntity<>("Username or email already exist", HttpStatus.valueOf(400));
    }

    @Override
    public void deleteAdmin(String username) {
        userDao.deleteByUsername(username);
    }

    @Override
    public ResponseEntity<String> updateUser(String username, UserDTO userDTO) {
        if(!userDao.existsByUsername(userDTO.getUsername()) && userDao.existsByEmail(userDTO.getEmail())){
            User user = userDao.findByUsername(username);
            user.setEmail(userDTO.getEmail());
            user.setUsername(userDTO.getUsername());
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            user.setLocked(userDTO.isLocked());
            user.setExpired(user.isExpired());
            return new ResponseEntity<>("user updated", HttpStatus.valueOf(200));
        }
        return new ResponseEntity<>("Username or email already exist", HttpStatus.valueOf(400));
    }

    @Override
    public void deleteUser(String username) {
        userDao.deleteByUsername(username);
    }

    private Role saveRole(String roleName){
        Role role = roleService.findByAuthority(roleName);
        if(role == null)
            role = roleService.save(new Role(roleName));
        return role;
    }

    private Collection<Role> saveRolesAdmin(String roleName){
        Role role1 = saveRole(roleName);
        Role role2 = saveRole("ROLE_USER");
        Collection<Role> roles = new ArrayList<>();
        roles.add(role1);
        roles.add(role2);
        return roles;
    }

    private static Message prepareMessage(Session session, String myAccount, String recepient, int code){
        try{
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(myAccount));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recepient));
            message.setSubject("Password recovery");
            message.setText("The verification code is : " + code);
            return message;
        }catch(Exception e){
            System.out.println(e);
        }
        return null;
    }

    private void sendMail(User user){
        String token = UUID.randomUUID().toString();

        ConfirmationEmail confirmationEmail = new ConfirmationEmail(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(30),
                user);

        emailService.save(confirmationEmail);
        emailSender.send(
                user.getEmail(),
                buildEmail(
                        user.getUsername(),
                        frontendUrl + "confirm_email/" + token));
    }

    private String buildEmail(String name, String link) {
        return "<div style=\"font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;color:#0b0c0c\">\n" +
                "\n" +
                "<span style=\"display:none;font-size:1px;color:#fff;max-height:0\"></span>\n" +
                "\n" +
                "  <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;min-width:100%;width:100%!important\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"100%\" height=\"53\" bgcolor=\"#0b0c0c\">\n" +
                "        \n" +
                "        <table role=\"presentation\" width=\"100%\" style=\"border-collapse:collapse;max-width:580px\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" align=\"center\">\n" +
                "          <tbody><tr>\n" +
                "            <td width=\"70\" bgcolor=\"#0b0c0c\" valign=\"middle\">\n" +
                "                <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td style=\"padding-left:10px\">\n" +
                "                  \n" +
                "                    </td>\n" +
                "                    <td style=\"font-size:28px;line-height:1.315789474;Margin-top:4px;padding-left:10px\">\n" +
                "                      <span style=\"font-family:Helvetica,Arial,sans-serif;font-weight:700;color:#ffffff;text-decoration:none;vertical-align:top;display:inline-block\">Confirm your email</span>\n" +
                "                    </td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "              </a>\n" +
                "            </td>\n" +
                "          </tr>\n" +
                "        </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td width=\"10\" height=\"10\" valign=\"middle\"></td>\n" +
                "      <td>\n" +
                "        \n" +
                "                <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse\">\n" +
                "                  <tbody><tr>\n" +
                "                    <td bgcolor=\"#1D70B8\" width=\"100%\" height=\"10\"></td>\n" +
                "                  </tr>\n" +
                "                </tbody></table>\n" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\" height=\"10\"></td>\n" +
                "    </tr>\n" +
                "  </tbody></table>\n" +
                "\n" +
                "\n" +
                "\n" +
                "  <table role=\"presentation\" class=\"m_-6186904992287805515content\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse:collapse;max-width:580px;width:100%!important\" width=\"100%\">\n" +
                "    <tbody><tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "      <td style=\"font-family:Helvetica,Arial,sans-serif;font-size:19px;line-height:1.315789474;max-width:560px\">\n" +
                "        \n" +
                "            <p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\">Hi " + name + ",</p><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> Thank you for registering. Please click on the below link to activate your account: </p><blockquote style=\"Margin:0 0 20px 0;border-left:10px solid #b1b4b6;padding:15px 0 0.1px 15px;font-size:19px;line-height:25px\"><p style=\"Margin:0 0 20px 0;font-size:19px;line-height:25px;color:#0b0c0c\"> <a href=\"" + link + "\">Activate Now</a> </p></blockquote>\n Link will expire in 30 minutes. <p>See you soon</p>" +
                "        \n" +
                "      </td>\n" +
                "      <td width=\"10\" valign=\"middle\"><br></td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td height=\"30\"><br></td>\n" +
                "    </tr>\n" +
                "  </tbody></table><div class=\"yj6qo\"></div><div class=\"adL\">\n" +
                "\n" +
                "</div></div>";
    }
}
