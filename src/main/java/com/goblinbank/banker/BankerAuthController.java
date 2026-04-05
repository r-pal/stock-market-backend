package com.goblinbank.banker;

import com.goblinbank.GoblinConstants;
import com.goblinbank.security.JwtService;
import com.goblinbank.user.AppUser;
import com.goblinbank.user.AppUserRepository;
import com.goblinbank.web.dto.LoginRequestDto;
import com.goblinbank.web.dto.TokenResponseDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/banker/auth")
public class BankerAuthController {

  private final AppUserRepository users;
  private final PasswordEncoder encoder;
  private final JwtService jwt;

  public BankerAuthController(
      AppUserRepository users, PasswordEncoder encoder, JwtService jwt) {
    this.users = users;
    this.encoder = encoder;
    this.jwt = jwt;
  }

  @PostMapping("/login")
  public TokenResponseDto login(@RequestBody LoginRequestDto body) {
    AppUser u = users.findByUsername(body.username()).orElseThrow(this::badCreds);
    if (!GoblinConstants.ROLE_BANKER.equals(u.getRole())) {
      throw badCreds();
    }
    if (!encoder.matches(body.password(), u.getPasswordHash())) {
      throw badCreds();
    }
    return new TokenResponseDto(jwt.createBankerToken(u.getUsername()));
  }

  private IllegalArgumentException badCreds() {
    return new IllegalArgumentException("Invalid credentials");
  }
}
