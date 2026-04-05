package com.goblinbank.house;

import com.goblinbank.account.HouseAccount;
import com.goblinbank.account.HouseAccountRepository;
import com.goblinbank.security.JwtService;
import com.goblinbank.web.dto.ChangePortalPasswordRequestDto;
import com.goblinbank.web.dto.HouseLoginRequestDto;
import com.goblinbank.web.dto.TokenResponseDto;
import java.time.Instant;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/house/auth")
public class HousePortalAuthController {

  private final HouseAccountRepository accounts;
  private final PasswordEncoder encoder;
  private final JwtService jwt;

  public HousePortalAuthController(
      HouseAccountRepository accounts, PasswordEncoder encoder, JwtService jwt) {
    this.accounts = accounts;
    this.encoder = encoder;
    this.jwt = jwt;
  }

  @PostMapping("/login")
  public TokenResponseDto login(@RequestBody HouseLoginRequestDto body) {
    HouseAccount h =
        accounts
            .findActiveById(body.houseId())
            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
    if (h.getPortalPasswordHash() == null) {
      throw new IllegalArgumentException("Invalid credentials");
    }
    if (!encoder.matches(body.password(), h.getPortalPasswordHash())) {
      throw new IllegalArgumentException("Invalid credentials");
    }
    return new TokenResponseDto(jwt.createHouseToken(h.getId()));
  }

  @PutMapping("/password")
  public void changePassword(
      @RequestBody ChangePortalPasswordRequestDto body, Authentication auth) {
    long houseId = (Long) auth.getPrincipal();
    HouseAccount h =
        accounts
            .findActiveByIdForUpdate(houseId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    if (h.getPortalPasswordHash() == null
        || !encoder.matches(body.currentPassword(), h.getPortalPasswordHash())) {
      throw new IllegalArgumentException("Current password incorrect");
    }
    if (body.newPassword() == null || body.newPassword().isBlank()) {
      throw new IllegalArgumentException("newPassword required");
    }
    h.setPortalPasswordHash(encoder.encode(body.newPassword()));
    h.setPortalPasswordUpdatedAt(Instant.now());
    accounts.save(h);
  }
}
