package com.goblinbank.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "goblin.jwt")
public class JwtProperties {

  /** HS256 secret; must be >= 256 bits (32 chars) for jjwt */
  private String secret =
      "dev-only-change-me-to-at-least-32-characters-long-secret!!";

  private long expirationSeconds = 86400;

  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public long getExpirationSeconds() {
    return expirationSeconds;
  }

  public void setExpirationSeconds(long expirationSeconds) {
    this.expirationSeconds = expirationSeconds;
  }
}
