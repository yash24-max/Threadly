package dev.threadly.core.identity;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.*;
import java.time.Instant;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JwtService {

  @Value("${threadly.jwt.private-key-path}")
  private String privateKeyPath;

  @Value("${threadly.jwt.public-key-path}")
  private String publicKeyPath;

  @Value("${threadly.jwt.access-token-expiry-minutes:15}")
  private long accessTokenExpiryMinutes;

  private PrivateKey privateKey;
  private PublicKey publicKey;

  @PostConstruct
  void init() throws Exception {
    privateKey = loadPrivateKey(privateKeyPath);
    publicKey = loadPublicKey(publicKeyPath);
  }

  public String generateAccessToken(UUID userId, UUID orgId, String role) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(userId.toString())
        .claim("org", orgId.toString())
        .claim("role", role)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(accessTokenExpiryMinutes * 60)))
        .signWith(privateKey)
        .compact();
  }

  public Claims parseToken(String token) {
    return Jwts.parser()
        .verifyWith(publicKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public boolean isValid(String token) {
    try {
      parseToken(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  private PrivateKey loadPrivateKey(String path) throws Exception {
    String pem = Files.readString(Paths.get(path))
        .replace("-----BEGIN PRIVATE KEY-----", "")
        .replace("-----END PRIVATE KEY-----", "")
        .replaceAll("\\s", "");
    byte[] keyBytes = Base64.getDecoder().decode(pem);
    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
    return KeyFactory.getInstance("RSA").generatePrivate(spec);
  }

  private PublicKey loadPublicKey(String path) throws Exception {
    String pem = Files.readString(Paths.get(path))
        .replace("-----BEGIN PUBLIC KEY-----", "")
        .replace("-----END PUBLIC KEY-----", "")
        .replaceAll("\\s", "");
    byte[] keyBytes = Base64.getDecoder().decode(pem);
    X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
    return KeyFactory.getInstance("RSA").generatePublic(spec);
  }
}
