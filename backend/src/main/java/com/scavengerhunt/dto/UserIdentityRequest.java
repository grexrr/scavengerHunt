package com.scavengerhunt.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class UserIdentityRequest {
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

    // ========== getters & setters ==========
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
