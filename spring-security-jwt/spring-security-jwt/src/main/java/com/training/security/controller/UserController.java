package com.training.security.controller;

import com.training.security.dto.ApiResponse;
import com.training.security.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin-only user management. Entire path is also locked down at the
 * SecurityConfig level (/api/users/** -> hasRole(ADMIN)).
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<ApiResponse> enableUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.setEnabled(id, true));
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<ApiResponse> disableUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.setEnabled(id, false));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deleteUser(id));
    }
}
