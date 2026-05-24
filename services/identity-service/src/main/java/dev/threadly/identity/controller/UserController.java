package dev.threadly.identity.controller;

import dev.threadly.identity.dto.UserDto;
import dev.threadly.identity.entity.User;
import dev.threadly.identity.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for user management endpoints.
 * Handles user profile retrieval and updates.
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  /**
   * Get current user profile (authenticated user).
   * Usually called with /me endpoint that resolves to current user ID via JWT.
   *
   * @param userId the current user ID (injected from auth context)
   * @return UserDto with current user details
   */
  @GetMapping("/me")
  public ResponseEntity<UserDto> getCurrentUser(@RequestHeader("X-User-Id") String userId) {

    log.debug("Fetching profile for user: {}", userId);
    User user = userService.getUserById(userId);

    return ResponseEntity.ok(userService.toDto(user));
  }

  /**
   * Get a user by ID.
   *
   * @param userId the user ID to retrieve
   * @return UserDto with user details
   */
  @GetMapping("/{userId}")
  public ResponseEntity<UserDto> getUser(@PathVariable String userId) {

    log.debug("Fetching user: {}", userId);
    User user = userService.getUserById(userId);

    return ResponseEntity.ok(userService.toDto(user));
  }

  /**
   * Update current user profile.
   *
   * @param userId the current user ID (injected from auth context)
   * @param fullName new full name (optional)
   * @param jobTitle new job title (optional)
   * @param profilePictureUrl new profile picture URL (optional)
   * @return updated UserDto
   */
  @PatchMapping("/{userId}")
  public ResponseEntity<UserDto> updateUserProfile(
      @PathVariable String userId,
      @RequestParam(required = false) String fullName,
      @RequestParam(required = false) String jobTitle,
      @RequestParam(required = false) String profilePictureUrl) {

    log.info("Updating profile for user: {}", userId);

    User updated = userService.updateProfile(
        userId,
        fullName,
        jobTitle,
        profilePictureUrl
    );

    return ResponseEntity.ok(userService.toDto(updated));
  }

  /**
   * Reset user password.
   *
   * @param userId the user ID
   * @param newPassword the new password
   * @return success response
   */
  @PostMapping("/{userId}/reset-password")
  public ResponseEntity<Void> resetPassword(
      @PathVariable String userId,
      @RequestParam String newPassword) {

    log.info("Resetting password for user: {}", userId);
    userService.resetPassword(userId, newPassword);

    return ResponseEntity.noContent().build();
  }

  /**
   * Deactivate user account.
   *
   * @param userId the user ID to deactivate
   * @return success response
   */
  @PostMapping("/{userId}/deactivate")
  public ResponseEntity<Void> deactivateUser(@PathVariable String userId) {

    log.info("Deactivating user: {}", userId);
    userService.deactivateUser(userId);

    return ResponseEntity.noContent().build();
  }
}
