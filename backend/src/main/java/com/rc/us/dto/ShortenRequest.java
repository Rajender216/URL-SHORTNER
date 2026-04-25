package com.rc.us.dto;

import jakarta.validation.constraints.NotBlank;

public record ShortenRequest(
        @NotBlank(message = "URL is required")
        String url
) {}
