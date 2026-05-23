package dev.threadly.core.workspace;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Encrypts and decrypts credential values using AES-256-GCM.
 *
 * <p>The master key is derived from {@code CREDENTIAL_MASTER_KEY} via PBKDF2WithHmacSHA256 with a
 * fixed salt embedded in the ciphertext encoding.
 *
 * <p>Stored format: {@code Base64(iv[12] + salt[16] + ciphertext)} — all concatenated, single
 * Base64 string.
 */
@Slf4j
@Service
public class CredentialService {

  private static final String CIPHER_ALGO = "AES/GCM/NoPadding";
  private static final String KEY_FACTORY_ALGO = "PBKDF2WithHmacSHA256";
  private static final int GCM_TAG_BITS = 128;
  private static final int IV_LENGTH = 12;
  private static final int SALT_LENGTH = 16;
  private static final int PBKDF2_ITERATIONS = 65536;
  private static final int KEY_LENGTH_BITS = 256;

  private final String masterKey;
  private final SecureRandom secureRandom = new SecureRandom();

  public CredentialService(
      @Value("${CREDENTIAL_MASTER_KEY:threadly_dev_change_in_production_min32chars}") String masterKey) {
    if (masterKey.length() < 16) {
      throw new IllegalArgumentException("CREDENTIAL_MASTER_KEY must be at least 16 characters");
    }
    this.masterKey = masterKey;
  }

  /**
   * Encrypts plaintext to a Base64-encoded ciphertext that includes IV and salt.
   */
  public String encrypt(String plaintext) {
    try {
      byte[] salt = new byte[SALT_LENGTH];
      byte[] iv = new byte[IV_LENGTH];
      secureRandom.nextBytes(salt);
      secureRandom.nextBytes(iv);

      SecretKey key = deriveKey(masterKey, salt);
      Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
      cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));

      byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

      // Pack: iv (12) + salt (16) + ciphertext
      byte[] packed = new byte[IV_LENGTH + SALT_LENGTH + encrypted.length];
      System.arraycopy(iv, 0, packed, 0, IV_LENGTH);
      System.arraycopy(salt, 0, packed, IV_LENGTH, SALT_LENGTH);
      System.arraycopy(encrypted, 0, packed, IV_LENGTH + SALT_LENGTH, encrypted.length);

      return Base64.getEncoder().encodeToString(packed);
    } catch (Exception e) {
      throw new IllegalStateException("Encryption failed", e);
    }
  }

  /**
   * Decrypts a stored ciphertext produced by {@link #encrypt(String)}.
   */
  public String decrypt(String stored) {
    try {
      byte[] packed = Base64.getDecoder().decode(stored);
      if (packed.length < IV_LENGTH + SALT_LENGTH + 1) {
        throw new IllegalArgumentException("Invalid ciphertext length");
      }

      byte[] iv = new byte[IV_LENGTH];
      byte[] salt = new byte[SALT_LENGTH];
      byte[] ciphertext = new byte[packed.length - IV_LENGTH - SALT_LENGTH];

      System.arraycopy(packed, 0, iv, 0, IV_LENGTH);
      System.arraycopy(packed, IV_LENGTH, salt, 0, SALT_LENGTH);
      System.arraycopy(packed, IV_LENGTH + SALT_LENGTH, ciphertext, 0, ciphertext.length);

      SecretKey key = deriveKey(masterKey, salt);
      Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
      cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));

      byte[] decrypted = cipher.doFinal(ciphertext);
      return new String(decrypted, StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new IllegalStateException("Decryption failed", e);
    }
  }

  private SecretKey deriveKey(String password, byte[] salt) throws Exception {
    SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_FACTORY_ALGO);
    KeySpec spec =
        new PBEKeySpec(
            password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS);
    byte[] keyBytes = factory.generateSecret(spec).getEncoded();
    return new SecretKeySpec(keyBytes, "AES");
  }
}
