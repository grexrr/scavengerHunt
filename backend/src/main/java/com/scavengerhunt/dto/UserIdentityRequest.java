package com.scavengerhunt.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class UserIdentityRequest {

    @Schema(
        description = "Unique user identifier",
        example = "uuid-string",
        required = false
    )
    private String userId;

    @Schema(
        description = "Username",
        example = "testuser",
        required = true,
        minLength = 3,
        maxLength = 20
    )
    private String username;

    @Schema(
        description = "Password",
        example = "password123",
        required = true,
        minLength = 6
    )
    private String password;

    @Schema(
        description = "Email",
        example = "email@abc.com",
        required = false
    )
    private String email;

    @Schema(
    description = "Preferred Language",
    example = "english",
    required = false
    )
    private String preferredLanguage;

    @Schema(
        description = "Preferred Style",
        example = "medieval",
        required = false
    )
    private String preferredStyle;

    @Schema(
        description = "Created At (ISO 8601 format)",
        example = "2024-01-01T12:00:00",
        required = false
    )
    private String createdAt;

    // ========== getters & setters ==========

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }

    public String getPreferredStyle() { return preferredStyle; }
    public void setPreferredStyle(String preferredStyle) { this.preferredStyle = preferredStyle; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
