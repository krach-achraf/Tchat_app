package tchat.microervices.ms_content_management.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(url="http://localhost:8080/api/v1/authentication/", name = "authentication")
public interface AuthenticationClient {

    @PutMapping("locking/{username}")
    void updateLocking(@PathVariable String username);
}
