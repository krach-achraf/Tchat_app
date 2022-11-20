package com.tchat.ms_authentification.service.facade;

import com.tchat.ms_authentification.bean.User;
import com.tchat.ms_authentification.dto.AdminDTO;
import com.tchat.ms_authentification.dto.UserDTO;
import com.tchat.ms_authentification.dto.UserSigninDTO;
import com.tchat.ms_authentification.dto.UserSignupDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    ResponseEntity<Object> signIn(UserSigninDTO user);
    ResponseEntity<String> signUp(UserSignupDTO user);

    ResponseEntity<String> confirmToken(String token);

    void updateLocking(String username);
    ResponseEntity<String> saveAdmin(AdminDTO admin);

    ResponseEntity<String> updateAdmin(String username, AdminDTO admin);

    void deleteAdmin(String username);

    ResponseEntity<String> updateUser(String username, UserDTO userDTO);

    void deleteUser(String username);

    ResponseEntity<String> sendMailVerific(String email, int codeVerific);

    void changePassword(String email, String newPassword);
}
