package com.scavengerhunt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Payload for submitting an answer and optional telemetry data.")
public class SubmitAnswerRequest {

    @NotBlank
    @Schema(
        description = "Unique identifier of the player submitting the answer.",
        example = "user-12345",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String userId;

    @NotNull
    @Schema(
        description = "Time spent (in seconds) before submitting the answer.",
        example = "42",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long secondsUsed;

    @Schema(
        description = "Current viewing angle of the player (degrees).",
        example = "135"
    )
    private Double currentAngle;

    @Schema(
        description = "Latitude reported when submitting the answer.",
        example = "51.894964"
    )
    private Double latitude;

    @Schema(
        description = "Longitude reported when submitting the answer.",
        example = "-8.489178"
    )
    private Double longitude;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getSecondsUsed() {
        return secondsUsed;
    }

    public void setSecondsUsed(Long secondsUsed) {
        this.secondsUsed = secondsUsed;
    }

    public Double getCurrentAngle() {
        return currentAngle;
    }

    public void setCurrentAngle(Double currentAngle) {
        this.currentAngle = currentAngle;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}

