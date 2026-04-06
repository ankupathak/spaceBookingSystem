package com.ls.spaceBookingSystem.services.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Map;

public interface JwtTokenStrategy<T> {

    // Which token type this strategy handles
    String getType();

    // Secret key — unique per token type
    SecretKey getSigningKey();

    // Token lifetime
    long getExpiryInMs();

    // Who/what this token is about — each type decides
    String resolveSubject(T data);

    // What claims this token carries — each type decides
    void addClaims(JwtBuilder builder, T data);

    // Extract typed data from claims — Claims stays internal to jwt package
    T extractData(Claims claims, Instant issuedAt, Instant expiry);
}
