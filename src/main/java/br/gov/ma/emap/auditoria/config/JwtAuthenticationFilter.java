package br.gov.ma.emap.auditoria.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);
        if (!jwtService.tokenValido(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        Claims claims = jwtService.extrairClaims(token);
        String login = claims.getSubject();
        List<String> perfis = claims.get("perfis",List.class);
        //List<SimpleGrantedAuthority> authorities = perfis.stream().map(SimpleGrantedAuthority::new).toList();// Para usar com hasHole nos controllers
        List<SimpleGrantedAuthority> authorities = perfis.stream().map(p -> new SimpleGrantedAuthority("ROLE_" + p)).toList();//authorities.forEach(System.out::println);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(login,null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}