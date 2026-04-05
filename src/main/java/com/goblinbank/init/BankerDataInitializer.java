package com.goblinbank.init;

import com.goblinbank.GoblinConstants;
import com.goblinbank.config.BankerSeedProperties;
import com.goblinbank.user.AppUser;
import com.goblinbank.user.AppUserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BankerDataInitializer implements ApplicationRunner {

  private final AppUserRepository users;
  private final PasswordEncoder encoder;
  private final BankerSeedProperties seed;

  public BankerDataInitializer(
      AppUserRepository users, PasswordEncoder encoder, BankerSeedProperties seed) {
    this.users = users;
    this.encoder = encoder;
    this.seed = seed;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (users.findByUsername(seed.getUsername()).isPresent()) {
      return;
    }
    AppUser u = new AppUser();
    u.setUsername(seed.getUsername());
    u.setPasswordHash(encoder.encode(seed.getPassword()));
    u.setRole(GoblinConstants.ROLE_BANKER);
    users.save(u);
  }
}
