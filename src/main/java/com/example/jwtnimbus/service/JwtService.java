package com.example.jwtnimbus.service;

import com.example.jwtnimbus.exception.JwtValidationException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final JWSAlgorithm ALGORITHM = JWSAlgorithm.HS256;
    private static final long CLOCK_SKEW_SECONDS = 30;

    private final byte[] secret;
    private final long expirationMillis;
    private final String issuer;
    private final Clock clock;

    @Autowired
    public JwtService(
            @Value("${security.jwt.secret-key}") String secret,
            @Value("${security.jwt.expiration-time}") long expirationMillis,
            @Value("${security.jwt.issuer}") String issuer
    ) {
        this(secret, expirationMillis, issuer, Clock.systemUTC());
    }

    JwtService(String secret, long expirationMillis, String issuer, Clock clock) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        if (this.secret.length < 32) {
            throw new IllegalArgumentException("Khóa HS256 phải có ít nhất 32 byte (256 bit)");
        }
        if (expirationMillis <= 0) {
            throw new IllegalArgumentException("Thời hạn JWT phải lớn hơn 0");
        }
        this.expirationMillis = expirationMillis;
        this.issuer = issuer;
        this.clock = clock;
    }

    public String generateToken(UserDetails userDetails) {
        var now = clock.instant();
        var authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        var claims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject(userDetails.getUsername())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusMillis(expirationMillis)))
                .jwtID(UUID.randomUUID().toString())
                .claim("authorities", authorities)
                .build();

        var signedJwt = new SignedJWT(
                new JWSHeader.Builder(ALGORITHM).type(com.nimbusds.jose.JOSEObjectType.JWT).build(),
                claims
        );
        try {
            signedJwt.sign(new MACSigner(secret));
            return signedJwt.serialize();
        } catch (JOSEException exception) {
            throw new IllegalStateException("Không thể ký JWT", exception);
        }
    }

    public String extractUsername(String token) {
        return verifyAndReadClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return userDetails.getUsername().equalsIgnoreCase(extractUsername(token));
    }

    public long getExpirationTime() {
        return expirationMillis;
    }

    JWTClaimsSet verifyAndReadClaims(String token) {
        try {
            var signedJwt = SignedJWT.parse(token);
            if (!ALGORITHM.equals(signedJwt.getHeader().getAlgorithm())) {
                throw new JwtValidationException("Thuật toán JWT không được hỗ trợ");
            }
            if (!signedJwt.verify(new MACVerifier(secret))) {
                throw new JwtValidationException("Chữ ký JWT không hợp lệ");
            }

            var claims = signedJwt.getJWTClaimsSet();
            var now = clock.instant();
            if (!issuer.equals(claims.getIssuer())) {
                throw new JwtValidationException("JWT issuer không hợp lệ");
            }
            if (claims.getSubject() == null || claims.getSubject().isBlank()) {
                throw new JwtValidationException("JWT không có subject");
            }
            if (claims.getExpirationTime() == null
                    || claims.getExpirationTime().toInstant().isBefore(now.minusSeconds(CLOCK_SKEW_SECONDS))) {
                throw new JwtValidationException("JWT đã hết hạn");
            }
            if (claims.getIssueTime() != null
                    && claims.getIssueTime().toInstant().isAfter(now.plusSeconds(CLOCK_SKEW_SECONDS))) {
                throw new JwtValidationException("Thời điểm phát hành JWT không hợp lệ");
            }
            return claims;
        } catch (ParseException | JOSEException exception) {
            throw new JwtValidationException("JWT không hợp lệ", exception);
        }
    }
}
