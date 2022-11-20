package com.tchat.ms_authentification.mapper;

import com.tchat.ms_authentification.bean.User;
import com.tchat.ms_authentification.dto.AdminDTO;
import com.tchat.ms_authentification.dto.UserDTO;
import com.tchat.ms_authentification.dto.UserSignupDTO;
import org.springframework.stereotype.Component;

@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User fromSignupUser(UserSignupDTO signupDTO) {
        User user = new User();
        user.setUsername(signupDTO.getUsername());
        user.setEmail(signupDTO.getEmail());
        user.setPassword(signupDTO.getPassword());
        return user;
    }

    @Override
    public User fromAdmin(AdminDTO adminDTO) {
        User admin = new User();
        admin.setUsername(adminDTO.getUsername());
        admin.setEmail(adminDTO.getEmail());
        admin.setPassword(adminDTO.getPassword());
        admin.setLocked(adminDTO.isLocked());
        admin.setExpired(adminDTO.isExpired());
        return admin;
    }



}
