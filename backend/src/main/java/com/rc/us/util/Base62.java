package com.rc.us.util;

import org.springframework.stereotype.Service;

@Service
public class Base62 {
    private static final String ALPHABET  = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public static String encode(Long value){
        StringBuilder sb = new StringBuilder();
        while(value>0){
            int idx = (int) (value%62);
            sb.append(ALPHABET.charAt(idx));
            value /= 62;
        }
        return sb.reverse().toString();
    }
}
