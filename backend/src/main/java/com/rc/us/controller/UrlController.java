package com.rc.us.controller;

import com.rc.us.dto.RecentUrlResponse;
import com.rc.us.dto.ResolveResponse;
import com.rc.us.dto.ShortenRequest;
import com.rc.us.dto.ShortenResponse;
import com.rc.us.payload.ApiResponse;
import com.rc.us.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping()
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UrlController {
    private final UrlService urlService;

    @PostMapping("/shorten")
    public ResponseEntity<ApiResponse<ShortenResponse>> shorten(@RequestBody ShortenRequest shortenRequest){
        ShortenResponse dto = urlService.shorten(shortenRequest);
        ApiResponse<ShortenResponse> res = new ApiResponse<>();
        res.setData(dto);
        res.setMessage("Done");
        res.setStatusCode(HttpStatus.OK.value());
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @GetMapping("/{shortId}")
    public ResponseEntity<ApiResponse> redirect(@PathVariable String shortId){
        Optional<String> originalUrl =  urlService.resolve(shortId);

        ApiResponse res = new ApiResponse<>();

        if(originalUrl.isPresent()){
            res.setStatusCode(HttpStatus.FOUND.value());
            res.setMessage("Successfully Redirected");
            res.setData(originalUrl);
            return ResponseEntity.status(HttpStatus.FOUND).header("Location", originalUrl.get()).body(res);
        }
        res.setMessage("URL not found");
        res.setStatusCode(HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
    }

    @GetMapping("/stats/{shortId}")
    public ResponseEntity<?> stats(@PathVariable String shortId) {
        Long clicks = urlService.getClickCount(shortId);
        return ResponseEntity.ok(Map.of("shortId", shortId, "clicks", clicks));
    }


    @GetMapping("/resolve/{shortId}")
    public ResponseEntity<?> resolve(@PathVariable String shortId) {

        return urlService.resolveOriginal(shortId)
                .map(url -> ResponseEntity.ok(new ResolveResponse(url)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/recent")
    public ResponseEntity<List<RecentUrlResponse>> getRecentUrls() {

        List<RecentUrlResponse> urls = urlService.getRecentUrls();

        return ResponseEntity.ok(urls);
    }
}
