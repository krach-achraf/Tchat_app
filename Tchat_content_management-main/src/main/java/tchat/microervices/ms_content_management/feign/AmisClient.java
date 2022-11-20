package tchat.microervices.ms_content_management.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import tchat.microervices.ms_content_management.vos.User;

import java.util.List;

@FeignClient(name = "amis", url = "https://4871-41-250-12-176.eu.ngrok.io/")
public interface AmisClient {

    @GetMapping("listFriends/{username}")
    List<User> getAmisOfUser(@PathVariable String username);
}
