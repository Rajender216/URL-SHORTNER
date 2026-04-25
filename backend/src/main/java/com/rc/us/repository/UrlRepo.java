package com.rc.us.repository;

import com.rc.us.model.Url;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UrlRepo extends MongoRepository<Url, String> {
    Optional<Url> findByShortId(String shortId);

    Optional<Url> findByOriginalUrl(String originalUrl);

    List<Url> findTop10ByOrderByCreatedAtDesc();
}
