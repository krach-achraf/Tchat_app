package com.tchat.ms_authentification.feign;

import com.tchat.ms_authentification.dto.UserSignupDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(url="http://localhost:8083", name="users")
public interface UsersClient {

    @PostMapping("/user/save")
    void saveUser(@RequestBody UserSignupDTO userSignup);

    @PutMapping("/user/locking/{username}/{locking}")
    void setLocking(@PathVariable String username, @PathVariable boolean locking);

    @PutMapping("/user/password/{username}/{password}")
    void updatePassword(@PathVariable String username, @PathVariable String password);
}
