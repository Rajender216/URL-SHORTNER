package com.rc.us.service;

import com.rc.us.dto.RecentUrlResponse;
import com.rc.us.dto.ShortenRequest;
import com.rc.us.dto.ShortenResponse;
import com.rc.us.model.Url;
import com.rc.us.repository.UrlRepo;
import com.rc.us.util.Base62;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepo urlRepo;
    private final StringRedisTemplate redisTemplate;

    private static final String COUNTER_KEY = "url:counter";
    private static final String SHORT_KEY_PREFIX = "short:";
    private static final String CLICK_KEY_PREFIX = "clicks:";

    // SHORTEN
    public ShortenResponse shorten(ShortenRequest shortenRequest) {

        String originalUrl = shortenRequest.url();

        if (!originalUrl.startsWith("http://") && !originalUrl.startsWith("https://")) {
            originalUrl = "https://" + originalUrl;
        }

        String maskedUrl = maskUrl(originalUrl);

        log.info("Shorten request received for domain={}", maskedUrl);

        // Dedup check
        Optional<Url> existing = urlRepo.findByOriginalUrl(originalUrl);
        if (existing.isPresent()) {

            String shortId = existing.get().getShortId();

            log.debug("Duplicate URL detected. shortId={}", shortId);

            redisTemplate.opsForValue().set(
                    SHORT_KEY_PREFIX + shortId,
                    originalUrl,
                    Duration.ofDays(7)
            );

            return new ShortenResponse(shortId);
        }

        // Generate ID
        Long id = redisTemplate.opsForValue().increment(COUNTER_KEY);

        if (id == null) {
            id = Instant.now().toEpochMilli();
            log.warn("Redis counter failed, using fallback id={}", id);
        }

        String shortId = Base62.encode(id);

        Url mapping = Url.builder()
                .shortId(shortId)
                .originalUrl(originalUrl)
                .clickCount(0L)
                .createdAt(Instant.now())
                .build();

        try {
            urlRepo.save(mapping);

            log.info("Created short URL shortId={}", shortId);

        } catch (Exception e) {

            log.warn("Race condition detected for domain={}", maskedUrl);

            Optional<Url> retry = urlRepo.findByOriginalUrl(originalUrl);
            if (retry.isPresent()) {
                return new ShortenResponse(retry.get().getShortId());
            }

            log.error("Failed to save URL mapping for domain={}", maskedUrl, e);
            throw e;
        }

        redisTemplate.opsForValue().set(
                SHORT_KEY_PREFIX + shortId,
                originalUrl,
                Duration.ofDays(7)
        );

        log.debug("Cached shortId={} in Redis", shortId);

        return new ShortenResponse(shortId);
    }

    // RESOLVE
    public Optional<String> resolve(String shortId) {

        String key = SHORT_KEY_PREFIX + shortId;

        String original = redisTemplate.opsForValue().get(key);

        if (original != null) {
            redisTemplate.opsForValue().increment(CLICK_KEY_PREFIX + shortId);

            log.debug("Cache hit for shortId={}", shortId);
            return Optional.of(original);
        }

        Optional<Url> opt = urlRepo.findByShortId(shortId);
        if (opt.isPresent()) {

            original = opt.get().getOriginalUrl();

            redisTemplate.opsForValue().set(key, original);
            redisTemplate.opsForValue().increment(CLICK_KEY_PREFIX + shortId);

            log.debug("Cache miss → DB hit for shortId={}", shortId);

            return Optional.of(original);
        }

        log.warn("ShortId not found={}", shortId);
        return Optional.empty();
    }

    // ================= CLICK COUNT =================
    public Long getClickCount(String shortId) {

        long redisCount = 0;

        String v = redisTemplate.opsForValue().get(CLICK_KEY_PREFIX + shortId);
        if (v != null) {
            redisCount = Long.parseLong(v);
        }

        long dbCount = urlRepo.findByShortId(shortId)
                .map(Url::getClickCount)
                .orElse(0L);

        long total = redisCount + dbCount;

        log.debug("ClickCount shortId={} => total={}", shortId, total);

        return total;
    }

    // HELPER
    private String maskUrl(String url) {
        if (url == null) return "null";

        try {
            int idx = url.indexOf("/", url.indexOf("//") + 2);
            if (idx != -1) {
                return url.substring(0, idx) + "/...";
            }
            return url;
        } catch (Exception e) {
            return "invalid-url";
        }
    }

    public Optional<String> resolveOriginal(String shortId) {

        String key = SHORT_KEY_PREFIX + shortId;

        // 🔹 1. Check Redis
        String original = redisTemplate.opsForValue().get(key);

        if (original != null) {
            log.debug("Cache hit for shortId={}", shortId);
            return Optional.of(original);
        }

        // 🔹 2. Check DB
        Optional<Url> opt = urlRepo.findByShortId(shortId);

        if (opt.isPresent()) {
            original = opt.get().getOriginalUrl();

            // 🔹 3. Save to Redis
            redisTemplate.opsForValue().set(
                    key,
                    original,
                    Duration.ofDays(7)
            );

            log.debug("Cache miss → DB hit for shortId={}", shortId);

            return Optional.of(original);
        }

        log.warn("ShortId not found={}", shortId);
        return Optional.empty();
    }


    public List<RecentUrlResponse> getRecentUrls() {

        List<Url> urls = urlRepo.findTop10ByOrderByCreatedAtDesc();

        return urls.stream()
                .map(url -> new RecentUrlResponse(
                        url.getShortId(),
                        url.getOriginalUrl(),
                        url.getClickCount(),
                        url.getCreatedAt()
                ))
                .toList();
    }
}