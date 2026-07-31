package com.hotrodoan.security.jwt;

import com.hotrodoan.security.userdetail.CustomUserDetail;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtProvider {
    private static final Logger logger = LoggerFactory.getLogger(JwtProvider.class);
    private String jwtSecret = "dannhanong@poksjs@lvk";
    private long jwtExpirration = 86400;
    public String createToken(Authentication authentication){
        CustomUserDetail userDetail = (CustomUserDetail) authentication.getPrincipal();
        return Jwts.builder().setSubject(userDetail.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime()+jwtExpirration*1000))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public boolean validateToken(String token){
        try {
            Jwts.parser().setSigningKey(jwtSecret).parsePlaintextJws(token);
            return true;
        } catch (SignatureException e){
            logger.error("Invalid jwt signature -> {}", e);
        }catch (MalformedJwtException e){
            logger.error("Invalid jwt format -> {}", e);
        }catch (UnsupportedJwtException e){
            logger.error("unsupported jwt token ->{}", e);
        }catch (ExpiredJwtException e){
            logger.error("Invalid jwt expired -> {}", e);
        }catch (IllegalArgumentException e){
            logger.error("Invalid jwt IllegalArgument -> {}", e);
        }
        return false;
    }

    public String getUsernameFromToken(String token){
        String username = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        return username;
    }
}
