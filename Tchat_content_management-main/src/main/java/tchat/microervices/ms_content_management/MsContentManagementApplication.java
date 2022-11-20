package tchat.microervices.ms_content_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MsContentManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsContentManagementApplication.class, args);
	}

}
