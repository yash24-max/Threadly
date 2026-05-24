package dev.threadly.identity.service;

import dev.threadly.identity.dto.UserDto;
import dev.threadly.identity.entity.User;
import dev.threadly.identity.event.EventPublisher;
import dev.threadly.identity.exception.DuplicateEmailException;
import dev.threadly.identity.exception.InvalidCredentialsException;
import dev.threadly.identity.exception.ResourceNotFoundException;
import dev.threadly.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing user accounts.
 * Handles user creation, authentication, password management, and profile updates.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthTokenService authTokenService;
  private final EventPublisher eventPublisher;

  /**
   * Registers a new user account.
   *
   * @param email user's email address (must be unique)
   * @param password plaintext password (will be hashed)
   * @param fullName user's full name
   * @param orgId organization ID to associate with
   * @return newly created User entity
   * @throws DuplicateEmailException if email already exists
   */
  @CacheEvict(value = "users", key = "#email", beforeInvocation = true)
  public User registerUser(String email, String password, String fullName, String orgId) {
    if (userRepository.existsByEmail(email)) {
      log.warn("Attempted registration with duplicate email: {}", email);
      throw new DuplicateEmailException(email);
    }

    String passwordHash = passwordEncoder.encode(password);

    User user = User.builder()
        .id(UUID.randomUUID().toString())
        .orgId(orgId)
        .email(email)
        .passwordHash(passwordHash)
        .fullName(fullName)
        .emailVerified(false)
        .active(true)
        .build();

    User savedUser = userRepository.save(user);
    log.info("Registered new user: {} with org: {}", email, orgId);

    eventPublisher.publishUserCreated(savedUser.getId(), orgId, email, fullName);

    return savedUser;
  }

  /**
   * Authenticates a user by email and password.
   *
   * @param email user's email
   * @param password plaintext password
   * @return authenticated User entity
   * @throws InvalidCredentialsException if email not found or password incorrect
   */
  @Transactional(readOnly = true)
  public User authenticateUser(String email, String password) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> {
          log.warn("Login attempt with non-existent email: {}", email);
          return new InvalidCredentialsException();
        });

    if (!passwordEncoder.matches(password, user.getPasswordHash())) {
      log.warn("Failed login attempt for user: {} (invalid password)", email);
      throw new InvalidCredentialsException();
    }

    if (!user.getActive()) {
      log.warn("Login attempt for inactive user: {}", email);
      throw new InvalidCredentialsException("User account is inactive");
    }

    user.setLastLoginAt(LocalDateTime.now(ZoneId.of("UTC")));
    userRepository.save(user);
    log.info("User authenticated: {}", email);

    return user;
  }

  /**
   * Gets a user by ID.
   *
   * @param userId the user ID
   * @return User entity
   * @throws ResourceNotFoundException if user not found
   */
  @Cacheable(value = "users", key = "#userId", unless = "#result == null")
  @Transactional(readOnly = true)
  public User getUserById(String userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User", userId));
  }

  /**
   * Gets a user by email.
   *
   * @param email the user's email
   * @return User entity
   * @throws ResourceNotFoundException if user not found
   */
  @Cacheable(value = "usersByEmail", key = "#email", unless = "#result == null")
  @Transactional(readOnly = true)
  public User getUserByEmail(String email) {
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User", email));
  }

  /**
   * Gets all active users in an organization.
   *
   * @param orgId the organization ID
   * @return list of active users
   */
  @Transactional(readOnly = true)
  public List<User> getActiveUsersInOrg(String orgId) {
    return userRepository.findActiveByOrgId(orgId);
  }

  /**
   * Updates user profile information.
   *
   * @param userId the user ID
   * @param fullName new full name (optional)
   * @param jobTitle new job title (optional)
   * @param profilePictureUrl new profile picture URL (optional)
   * @return updated User entity
   */
  @CacheEvict(value = "users", key = "#userId")
  public User updateProfile(String userId, String fullName, String jobTitle, String profilePictureUrl) {
    User user = getUserById(userId);

    if (fullName != null && !fullName.isBlank()) {
      user.setFullName(fullName);
    }
    if (jobTitle != null) {
      user.setJobTitle(jobTitle);
    }
    if (profilePictureUrl != null) {
      user.setProfilePictureUrl(profilePictureUrl);
    }

    User updated = userRepository.save(user);
    log.info("Updated profile for user: {}", userId);

    return updated;
  }

  /**
   * Resets a user's password.
   *
   * @param userId the user ID
   * @param newPassword the new password (plaintext)
   * @throws ResourceNotFoundException if user not found
   */
  @CacheEvict(value = "users", key = "#userId")
  public void resetPassword(String userId, String newPassword) {
    User user = getUserById(userId);

    user.setPasswordHash(passwordEncoder.encode(newPassword));
    userRepository.save(user);

    authTokenService.revokeAllRefreshTokens(userId);
    log.info("Password reset for user: {}", userId);
  }

  /**
   * Verifies a user's email address.
   *
   * @param userId the user ID
   */
  @CacheEvict(value = "users", key = "#userId")
  public void verifyEmail(String userId) {
    User user = getUserById(userId);
    user.setEmailVerified(true);
    user.setEmailVerifiedAt(LocalDateTime.now(ZoneId.of("UTC")));
    userRepository.save(user);
    log.info("Email verified for user: {}", userId);
  }

  /**
   * Deactivates a user account.
   *
   * @param userId the user ID
   */
  @CacheEvict(value = "users", key = "#userId")
  public void deactivateUser(String userId) {
    User user = getUserById(userId);
    user.setActive(false);
    userRepository.save(user);

    authTokenService.revokeAllRefreshTokens(userId);
    log.info("Deactivated user account: {}", userId);
  }

  /**
   * Converts a User entity to UserDto for API responses.
   *
   * @param user the User entity
   * @return UserDto with non-sensitive information
   */
  public UserDto toDto(User user) {
    return UserDto.builder()
        .id(user.getId())
        .email(user.getEmail())
        .fullName(user.getFullName())
        .organizationId(user.getOrgId())
        .jobTitle(user.getJobTitle())
        .profilePictureUrl(user.getProfilePictureUrl())
        .emailVerified(user.getEmailVerified())
        .active(user.getActive())
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .lastLoginAt(user.getLastLoginAt())
        .build();
  }

  /**
   * Converts a list of User entities to UserDto list.
   *
   * @param users list of User entities
   * @return list of UserDto
   */
  public List<UserDto> toDto(List<User> users) {
    return users.stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }
}
