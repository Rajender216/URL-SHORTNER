package com.rc.us;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;

@SpringBootApplication
@EnableMongoAuditing
@EnableScheduling
public class UrlShortnerApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrlShortnerApplication.class, args);
    }

}
