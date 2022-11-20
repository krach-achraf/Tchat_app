package com.tchat.ms_authentification.rest;

import com.tchat.ms_authentification.bean.User;
import com.tchat.ms_authentification.dao.UserDao;
import com.tchat.ms_authentification.dto.AdminDTO;
import com.tchat.ms_authentification.dto.UserDTO;
import com.tchat.ms_authentification.dto.UserSigninDTO;
import com.tchat.ms_authentification.dto.UserSignupDTO;
import com.tchat.ms_authentification.service.facade.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/authentication")
@AllArgsConstructor
public class UserRest {

    private UserService userService;

    @PostMapping( "/sign-in")
    public ResponseEntity<Object> signIn(@RequestBody @Valid UserSigninDTO user) {
        return userService.signIn(user);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<String> signUp(@Valid @RequestBody UserSignupDTO user) {
        return userService.signUp(user);
    }

    @GetMapping("/confirm")
    public ResponseEntity<String> confirmToken(@RequestParam("token") String token) {
        return userService.confirmToken(token);
    }

    @PutMapping("/locking/{username}")
    public void updateLocking(@PathVariable String username) {
        userService.updateLocking(username);
    }

    @GetMapping("/forgotPassword/mail/{email}/{codeVerific}")
    public ResponseEntity<String> sendMailVerific(@PathVariable String email,@PathVariable int codeVerific) {
        return userService.sendMailVerific(email, codeVerific);
    }

    @PutMapping("forgotPassword/changePassword/{email}/{newPassword}")
    public void changePassword(@PathVariable String email, @PathVariable String newPassword) {
        userService.changePassword(email, newPassword);
    }

    @PostMapping("/admin/save")
    public ResponseEntity<String> saveAdmin(@RequestBody @Valid AdminDTO admin) {
        return userService.saveAdmin(admin);
    }

    @PutMapping("/admin/update/{username}")
    public ResponseEntity<String> updateAdmin(@PathVariable String username,@RequestBody @Valid AdminDTO adminDTO) {
        return userService.updateAdmin(username, adminDTO);
    }

    @DeleteMapping("/admin/delete/{username}")
    public void deleteAdmin(@PathVariable String username) {
        userService.deleteAdmin(username);
    }

    @PutMapping("/user/update/{username}")
    public ResponseEntity<String> updateUser(@PathVariable String username,@RequestBody @Valid UserDTO userDTO) {
        return userService.updateUser(username, userDTO);
    }

    @DeleteMapping("/user/delete/{username}")
    public void deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
    }

    private UserDao userDao;

    @GetMapping("/users")
    public List<User> getUsers(){
        return userDao.findAll();
    }

}
