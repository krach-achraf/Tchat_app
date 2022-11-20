package tchat.microervices.ms_content_management.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import tchat.microervices.ms_content_management.vos.User;

import java.util.List;

@FeignClient(name = "users", url = "http://localhost:8083/")
public interface UsersClient {

    @GetMapping("user/id/{id}")
    User getUserById(@PathVariable Long id);

    @GetMapping("users")
    List<User> getAllUsers();
}
