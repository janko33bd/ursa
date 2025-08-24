package jb.wrk.tribe;

import jb.wrk.tribe.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class TribeApplication {

	public static void main(String[] args) {
		SpringApplication.run(TribeApplication.class, args);
	}

}
