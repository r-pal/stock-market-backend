package com.goblinbank;

import com.goblinbank.config.BankerSeedProperties;
import com.goblinbank.config.RateBoundsProperties;
import com.goblinbank.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({JwtProperties.class, RateBoundsProperties.class, BankerSeedProperties.class})
public class GoblinBankApplication {

	public static void main(String[] args) {
		SpringApplication.run(GoblinBankApplication.class, args);
	}

}
