package com.rc.us.dto;

import java.time.Instant;

public record RecentUrlResponse(
        String shortId,
        String originalUrl,
        Long clickCount,
        Instant createdAt
) {}