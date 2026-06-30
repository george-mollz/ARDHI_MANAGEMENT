# Phase Two — Authentication and Authorization

This document is a step-by-step guide for adding authentication (who you are) and authorization (what you can do) to the Land Registration API.

Complete **Phase One** (`PHASE-ONE-README.md`) first so all CRUD endpoints are stable before securing them.

**Base path:** `/api/v1/land`  
**Server port:** `8081`  
**Tech stack:** Spring Boot 4.1.0, Java 21, Spring Security, JWT

All code examples below are complete and ready to copy.

---

## Overview

| Concept | Meaning in this project |
|---------|------------------------|
| **Authentication** | Verify identity via login (username + password → JWT token) |
| **Authorization** | Control which roles can call which endpoints |
| **JWT** | JSON Web Token — stateless token sent in the `Authorization` header |

### Planned roles

| Role | Permissions |
|------|-------------|
| `ADMIN` | Full access — register, read, update, delete |
| `OFFICER` | Register and read plots; update existing plots |
| `PUBLIC` | Read-only — search/get plot info only |

---

## Step 1 — Add Dependencies

Add to **`pom.xml`** inside `<dependencies>`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>

<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

---

## Step 2 — User Entity and Repository

### 2.1 User entity

**`model/User.java`**

```java
package com.ardhi.Land.registration.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;
}
```

### 2.2 User repository

**`repository/UserRepository.java`**

```java
package com.ardhi.Land.registration.repository;

import com.ardhi.Land.registration.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}
```

---

## Step 3 — Password Encoding

**`config/PasswordConfig.java`**

```java
package com.ardhi.Land.registration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

## Step 4 — JWT Utility

### 4.1 Add to application.yaml

```yaml
jwt:
  secret: your-256-bit-secret-key-change-this-in-production-min-32-chars
  expiration-ms: 86400000
```

### 4.2 JwtUtil

**`security/JwtUtil.java`**

```java
package com.ardhi.Land.registration.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(
                java.util.Base64.getEncoder().encodeToString(secret.getBytes()));
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
```

---

## Step 5 — UserDetailsService

**`security/CustomUserDetailsService.java`**

```java
package com.ardhi.Land.registration.security;

import com.ardhi.Land.registration.model.User;
import com.ardhi.Land.registration.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElse(null);

        if (user == null) {
            return null;
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }
}
```

---

## Step 6 — JWT Authentication Filter

**`security/JwtAuthFilter.java`**

```java
package com.ardhi.Land.registration.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (userDetails != null && jwtUtil.isTokenValid(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

---

## Step 7 — Security Configuration

**`config/SecurityConfig.java`**

```java
package com.ardhi.Land.registration.config;

import com.ardhi.Land.registration.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/land/**").hasAnyRole("ADMIN", "OFFICER", "PUBLIC")
                .requestMatchers(HttpMethod.POST, "/api/v1/land/register").hasAnyRole("ADMIN", "OFFICER")
                .requestMatchers(HttpMethod.PUT, "/api/v1/land/**").hasAnyRole("ADMIN", "OFFICER")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/land/**").hasAnyRole("ADMIN", "OFFICER")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/land/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\":\"Authentication required\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\":\"Access denied\"}");
                })
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

### Endpoint access matrix

| Endpoint | ADMIN | OFFICER | PUBLIC | Unauthenticated |
|----------|-------|---------|--------|-----------------|
| `POST /auth/login` | Yes | Yes | Yes | Yes |
| `POST /auth/register` | Yes | No | No | No |
| `GET /land/**` | Yes | Yes | Yes | No |
| `POST /land/register` | Yes | Yes | No | No |
| `PUT /land/**` | Yes | Yes | No | No |
| `PATCH /land/**` | Yes | Yes | No | No |
| `DELETE /land/**` | Yes | No | No | No |

---

## Step 8 — Auth DTOs

**`dto/LoginRequestDto.java`**

```java
package com.ardhi.Land.registration.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {

    private String username;
    private String password;
}
```

**`dto/LoginResponseDto.java`**

```java
package com.ardhi.Land.registration.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {

    private String token;
    private String username;
    private String role;
    private long expiresIn;
    private String message;
}
```

**`dto/RegisterUserRequestDto.java`**

```java
package com.ardhi.Land.registration.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserRequestDto {

    private String username;
    private String password;
    private String role;
}
```

**`dto/RegisterUserResponseDto.java`**

```java
package com.ardhi.Land.registration.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserResponseDto {

    private UUID id;
    private String username;
    private String role;
    private String message;
}
```

---

## Step 9 — Auth Controller

**`controller/AuthController.java`**

```java
package com.ardhi.Land.registration.controller;

import com.ardhi.Land.registration.dto.*;
import com.ardhi.Land.registration.model.User;
import com.ardhi.Land.registration.repository.UserRepository;
import com.ardhi.Land.registration.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request) {

        if (request.getUsername() == null || request.getUsername().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            LoginResponseDto error = new LoginResponseDto();
            error.setMessage("Username and password are required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

        if (userDetails == null) {
            LoginResponseDto error = new LoginResponseDto();
            error.setMessage("Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        if (!passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
            LoginResponseDto error = new LoginResponseDto();
            error.setMessage("Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        String token = jwtUtil.generateToken(userDetails);
        String role = userDetails.getAuthorities().iterator().next().getAuthority()
                .replace("ROLE_", "");

        LoginResponseDto response = new LoginResponseDto();
        response.setToken(token);
        response.setUsername(userDetails.getUsername());
        response.setRole(role);
        response.setExpiresIn(jwtUtil.getExpirationMs());
        response.setMessage("Login successful");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RegisterUserResponseDto> registerUser(
            @RequestBody RegisterUserRequestDto request) {

        if (request.getUsername() == null || request.getUsername().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()
                || request.getRole() == null || request.getRole().isBlank()) {
            RegisterUserResponseDto error = new RegisterUserResponseDto();
            error.setMessage("username, password, and role are required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            RegisterUserResponseDto error = new RegisterUserResponseDto();
            error.setMessage("Username already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole().toUpperCase());

        User saved = userRepository.save(user);

        RegisterUserResponseDto response = new RegisterUserResponseDto();
        response.setId(saved.getId());
        response.setUsername(saved.getUsername());
        response.setRole(saved.getRole());
        response.setMessage("User registered successfully");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

---

## Step 10 — Secure Land Controller

Add `@PreAuthorize` to each method in **`controller/LandController.java`**:

```java
import org.springframework.security.access.prepost.PreAuthorize;

@PostMapping("/register")
@PreAuthorize("hasAnyRole('ADMIN', 'OFFICER')")
public ResponseEntity<String> registerLand(@RequestBody RegisterPlotRequestDto request) {
    String response = registerPlotService.registerPlot(request);
    return ResponseEntity.ok(response);
}

@GetMapping("/getInfo/plotNo/{plotNo}/region/{region}")
@PreAuthorize("hasAnyRole('ADMIN', 'OFFICER', 'PUBLIC')")
public ResponseEntity<SearchPlotResponseDto> getInfo(
        @PathVariable String plotNo,
        @PathVariable String region) {
    // existing code
}

@PatchMapping("/plotNo/{plotNo}/region/{region}")
@PreAuthorize("hasAnyRole('ADMIN', 'OFFICER')")
public ResponseEntity<PatchPlotResponseDto> patchPlot(
        @PathVariable String plotNo,
        @PathVariable String region,
        @RequestBody PatchPlotRequestDto request) {
    // existing code
}

@PutMapping("/plotNo/{plotNo}/region/{region}")
@PreAuthorize("hasAnyRole('ADMIN', 'OFFICER')")
public ResponseEntity<UpdatePlotResponseDto> updatePlot(
        @PathVariable String plotNo,
        @PathVariable String region,
        @RequestBody UpdatePlotRequestDto request) {
    // existing code
}

@DeleteMapping("/plotNo/{plotNo}/region/{region}")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<DeletePlotResponseDto> deletePlot(
        @PathVariable String plotNo,
        @PathVariable String region) {
    // existing code
}
```

---

## Step 11 — Seed Default Users (dev only)

**`config/DataInitializer.java`**

```java
package com.ardhi.Land.registration.config;

import com.ardhi.Land.registration.model.User;
import com.ardhi.Land.registration.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createUserIfMissing("admin", "admin123", "ADMIN");
        createUserIfMissing("officer", "officer123", "OFFICER");
        createUserIfMissing("public", "public123", "PUBLIC");
    }

    private void createUserIfMissing(String username, String password, String role) {
        if (!userRepository.existsByUsername(username)) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            userRepository.save(user);
        }
    }
}
```

Add to **`application.yaml`** for development:

```yaml
spring:
  profiles:
    active: dev
```

**Default test accounts (dev only):**

| Username | Password | Role |
|----------|----------|------|
| `admin` | `admin123` | ADMIN |
| `officer` | `officer123` | OFFICER |
| `public` | `public123` | PUBLIC |

---

## Step 12 — Update CORS for Authorization Header

Update **`config/CorsConfig.java`**:

```java
registry.addMapping("/api/**")
        .allowedOrigins("http://localhost:3000")
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        .allowedHeaders("Authorization", "Content-Type")
        .exposedHeaders("Authorization")
        .maxAge(3600);
```

---

## Step 13 — File Checklist

```
src/main/java/com/ardhi/Land/registration/
├── config/
│   ├── CorsConfig.java              (update)
│   ├── PasswordConfig.java          (new)
│   ├── SecurityConfig.java          (new)
│   └── DataInitializer.java         (new)
├── controller/
│   ├── AuthController.java          (new)
│   └── LandController.java          (add @PreAuthorize)
├── dto/
│   ├── LoginRequestDto.java         (new)
│   ├── LoginResponseDto.java        (new)
│   ├── RegisterUserRequestDto.java  (new)
│   └── RegisterUserResponseDto.java (new)
├── model/
│   └── User.java                    (new)
├── repository/
│   └── UserRepository.java          (new)
└── security/
    ├── JwtUtil.java                 (new)
    ├── JwtAuthFilter.java           (new)
    └── CustomUserDetailsService.java (new)
```

---

## Step 14 — Manual Test Sequence

```bash
# 1. Login as admin
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 2. Save token and access protected endpoint
TOKEN="<paste-token-here>"
curl http://localhost:8081/api/v1/land/getInfo/plotNo/P-001/region/Nairobi \
  -H "Authorization: Bearer $TOKEN"

# 3. Register a plot (ADMIN or OFFICER)
curl -X POST http://localhost:8081/api/v1/land/register \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"plotNo":"P-001","region":"Nairobi","landUse":"Residential"}'

# 4. Login as public user
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"public","password":"public123"}'

# 5. Public user can read
PUBLIC_TOKEN="<paste-public-token>"
curl http://localhost:8081/api/v1/land/getInfo/plotNo/P-001/region/Nairobi \
  -H "Authorization: Bearer $PUBLIC_TOKEN"

# 6. Public user cannot delete (403)
curl -X DELETE http://localhost:8081/api/v1/land/plotNo/P-001/region/Nairobi \
  -H "Authorization: Bearer $PUBLIC_TOKEN"

# 7. Admin registers a new user
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"username":"newofficer","password":"pass123","role":"OFFICER"}'
```

---

## Step 15 — Security Tests

**`LandControllerSecurityTest.java`**

```java
package com.ardhi.Land.registration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LandControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    void deletePlot_asAdmin_returnsOk() throws Exception {
        mockMvc.perform(delete("/api/v1/land/plotNo/P-001/region/Nairobi"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "PUBLIC")
    void deletePlot_asPublic_returnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/land/plotNo/P-001/region/Nairobi"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PUBLIC")
    void getInfo_asPublic_returnsOk() throws Exception {
        mockMvc.perform(get("/api/v1/land/getInfo/plotNo/P-001/region/Nairobi"))
                .andExpect(status().isOk());
    }

    @Test
    void getInfo_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/land/getInfo/plotNo/P-001/region/Nairobi"))
                .andExpect(status().isUnauthorized());
    }
}
```

---

## Step 16 — Production Configuration

For production, use environment variables in **`application.yaml`**:

```yaml
spring:
  profiles:
    active: prod

jwt:
  secret: ${JWT_SECRET}
  expiration-ms: ${JWT_EXPIRATION_MS:3600000}
```

Set `JWT_SECRET` to a strong 32+ character value in your deployment environment. Keep `DataInitializer` on `@Profile("dev")` only.

---

## Quick Reference — Authenticated API Usage

```bash
# Login and store token
TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  | jq -r '.token')

# Use token on any request
curl http://localhost:8081/api/v1/land/getInfo/plotNo/P-001/region/Nairobi \
  -H "Authorization: Bearer $TOKEN"
```

---

## Suggested Implementation Order

1. Add dependencies to `pom.xml`
2. Create `User` entity and `UserRepository`
3. Add `PasswordConfig`
4. Implement `JwtUtil`
5. Implement `CustomUserDetailsService`
6. Implement `JwtAuthFilter`
7. Configure `SecurityConfig`
8. Create auth DTOs and `AuthController`
9. Add `DataInitializer` for dev users
10. Add `@PreAuthorize` to `LandController`
11. Update CORS for `Authorization` header
12. Run manual curl tests
13. Add security tests

After both phases are complete, update the main `README.md` with authentication instructions and the full secured API reference.
