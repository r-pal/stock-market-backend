package com.goblinbank.security;

import com.goblinbank.GoblinConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final JwtProperties props;

  public JwtService(JwtProperties props) {
    this.props = props;
  }

  private SecretKey key() {
    return Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
  }

  public String createBankerToken(String username) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject("banker:" + username)
        .claim("role", GoblinConstants.ROLE_BANKER)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(props.getExpirationSeconds())))
        .signWith(key())
        .compact();
  }

  public String createHouseToken(long houseId) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject("house:" + houseId)
        .claim("role", GoblinConstants.ROLE_HOUSE_PORTAL)
        .claim("houseId", houseId)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(props.getExpirationSeconds())))
        .signWith(key())
        .compact();
  }

  public Claims parse(String token) {
    return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
  }
}
