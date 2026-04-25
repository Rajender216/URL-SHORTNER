package com.rc.us.model;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Builder
@Document(collection = "urls")
public class Url {

    @Id
    private String id;

    @Indexed(unique = true)
    private String shortId;

    @Indexed(unique = true)
    private String originalUrl;

    private Long clickCount = 0L;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}