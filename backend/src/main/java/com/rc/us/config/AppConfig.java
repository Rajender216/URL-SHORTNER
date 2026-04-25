package com.rc.us.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class AppConfig {

    @Value("${app.click-sync.interval}")
    private long clickSyncInterval;

    public long getClickSyncInterval() {
        return clickSyncInterval;
    }
}