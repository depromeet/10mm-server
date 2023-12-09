package com.depromeet.global.util;

import com.depromeet.global.config.security.PrincipalDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    private void setMockAuthentication() {
        PrincipalDetails principal = new PrincipalDetails(1L, "USER");
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        principal, "password", principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public Long getCurrentMemberId() {
        setMockAuthentication();
        PrincipalDetails principal =
                (PrincipalDetails)
                        SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Long.parseLong(principal.getUsername());
    }
}
