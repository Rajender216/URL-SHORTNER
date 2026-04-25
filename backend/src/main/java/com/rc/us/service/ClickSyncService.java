package com.rc.us.service;

import com.rc.us.repository.UrlRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickSyncService {

    private final StringRedisTemplate redisTemplate;
    private final UrlRepo urlRepo;

    private static final String CLICK_KEY_PREFIX = "clicks:";

    @Scheduled(fixedDelayString = "${app.click-sync.interval}")
    public void syncClicksToDB() {

        log.info("Starting click sync job...");

        Set<String> keys = redisTemplate.keys(CLICK_KEY_PREFIX + "*");

        if (keys == null || keys.isEmpty()) {
            log.info("No click keys found in Redis.");
            return;
        }

        for (String key : keys) {
            try {
                String shortId = key.replace(CLICK_KEY_PREFIX, "");

                String value = redisTemplate.opsForValue().get(key);
                if (value == null) {
                    log.warn("Value is null for key: {}", key);
                    continue;
                }

                long clicks = Long.parseLong(value);

                urlRepo.findByShortId(shortId).ifPresentOrElse(url -> {

                    long existing = url.getClickCount() != null ? url.getClickCount() : 0;

                    url.setClickCount(existing + clicks);
                    urlRepo.save(url);

                    log.info("Synced {} clicks for shortId={}", clicks, shortId);

                }, () -> {
                    log.error("No URL found for shortId={}", shortId);
                });

                // delete only after success
                redisTemplate.delete(key);

            } catch (Exception e) {
                log.error("Error syncing key: {}", key, e);
            }
        }

        log.info("Click sync job completed.");
    }
}