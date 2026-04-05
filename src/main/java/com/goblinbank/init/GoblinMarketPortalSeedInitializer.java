package com.goblinbank.init;

import com.goblinbank.account.HouseAccount;
import com.goblinbank.account.HouseAccountRepository;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds portal passwords (plaintext fruits from Christina Rossetti's "Goblin Market") for the six
 * named houses, only when no hash is set yet so bankers can override.
 */
@Component
@Order(1)
public class GoblinMarketPortalSeedInitializer implements ApplicationRunner {

  /** House display name → fruit password from the poem (lowercase). */
  private static final Map<String, String> HOUSE_FRUIT_PASSWORDS = new LinkedHashMap<>();

  static {
    HOUSE_FRUIT_PASSWORDS.put("Laura", "cherries");
    HOUSE_FRUIT_PASSWORDS.put("Lizzie", "quinces");
    HOUSE_FRUIT_PASSWORDS.put("Jeanie", "mulberries");
    HOUSE_FRUIT_PASSWORDS.put("Golden Head", "peaches");
    HOUSE_FRUIT_PASSWORDS.put("Brookside", "figs");
    HOUSE_FRUIT_PASSWORDS.put("Moonlight", "pomegranates");
  }

  private final HouseAccountRepository accounts;
  private final PasswordEncoder encoder;

  public GoblinMarketPortalSeedInitializer(
      HouseAccountRepository accounts, PasswordEncoder encoder) {
    this.accounts = accounts;
    this.encoder = encoder;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    for (Map.Entry<String, String> e : HOUSE_FRUIT_PASSWORDS.entrySet()) {
      accounts
          .findActiveByHouseNameIgnoreCase(e.getKey())
          .filter(h -> h.getPortalPasswordHash() == null)
          .ifPresent(h -> setPortal(h, e.getValue()));
    }
  }

  private void setPortal(HouseAccount h, String rawPassword) {
    h.setPortalPasswordHash(encoder.encode(rawPassword));
    h.setPortalPasswordUpdatedAt(Instant.now());
    accounts.save(h);
  }
}
