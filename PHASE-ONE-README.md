# Phase One — HTTP Methods Plan (PATCH, PUT, DELETE, OPTIONS)

This document is the implementation plan for completing Phase One of the Land Registration API. It covers the four HTTP methods that are missing or incomplete, building on the existing `GET` and `POST` endpoints in `LandController`.

**Base path:** `/api/v1/land`  
**Server port:** `8081`  
**Tech stack:** Spring Boot 4.1.0, Java 21, Spring Data JPA, PostgreSQL

All code examples below are complete and ready to copy. Error cases are handled with `if/else` and `ResponseEntity` — no thrown exceptions.

---

## Current State

| Method  | Endpoint | Status |
|---------|----------|--------|
| `POST`  | `/register` | Done |
| `GET`   | `/getInfo/plotNo/{plotNo}/region/{region}` | Done |
| `PATCH` | `/plotNo/{plotNo}` | Started — has compile error and incomplete logic |
| `PUT`   | — | Not implemented |
| `DELETE`| — | Not implemented |
| `OPTIONS` | — | Not implemented |

---

## Step 1 — PATCH (Partial Update)

### 1.1 Request / response contract

**Endpoint:** `PATCH /api/v1/land/plotNo/{plotNo}/region/{region}`

```json
{
  "newPlotNo": "P-002",
  "landUse": "Residential",
  "isRegistered": true
}
```

All body fields are optional. Only provided fields are updated.

### 1.2 DTOs

**`dto/PatchPlotRequestDto.java`**

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
public class PatchPlotRequestDto {

    private String newPlotNo;
    private String landUse;
    private Boolean isRegistered;
}
```

**`dto/PatchPlotResponseDto.java`**

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
public class PatchPlotResponseDto {

    private String plotNo;
    private String region;
    private String landUse;
    private boolean isRegistered;
    private String message;
}
```

### 1.3 Service interface

**`service/PatchUpdatePlotService.java`**

```java
package com.ardhi.Land.registration.service;

import com.ardhi.Land.registration.dto.PatchPlotRequestDto;
import com.ardhi.Land.registration.dto.PatchPlotResponseDto;
import org.springframework.http.ResponseEntity;

public interface PatchUpdatePlotService {

    ResponseEntity<PatchPlotResponseDto> patchPlot(
            String plotNo, String region, PatchPlotRequestDto request);
}
```

### 1.4 Service implementation

**`service/serviceImpl/PatchPlotServiceImpl.java`**

```java
package com.ardhi.Land.registration.service.serviceImpl;

import com.ardhi.Land.registration.dto.PatchPlotRequestDto;
import com.ardhi.Land.registration.dto.PatchPlotResponseDto;
import com.ardhi.Land.registration.model.Land;
import com.ardhi.Land.registration.repository.LandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PatchPlotServiceImpl implements PatchUpdatePlotService {

    @Autowired
    private LandRepository landRepository;

    @Override
    public ResponseEntity<PatchPlotResponseDto> patchPlot(
            String plotNo, String region, PatchPlotRequestDto request) {

        if (plotNo == null || plotNo.isBlank() || region == null || region.isBlank()) {
            PatchPlotResponseDto error = new PatchPlotResponseDto();
            error.setMessage("plotNo and region are required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        Optional<Land> existing = landRepository.findByPlotNoAndRegion(plotNo, region);

        if (existing.isEmpty()) {
            PatchPlotResponseDto error = new PatchPlotResponseDto();
            error.setMessage("Land not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        Land land = existing.get();

        if (request.getNewPlotNo() != null && !request.getNewPlotNo().isBlank()
                && !request.getNewPlotNo().equals(land.getPlotNo())) {

            Optional<Land> duplicate = landRepository.findByPlotNo(request.getNewPlotNo());
            if (duplicate.isPresent()) {
                PatchPlotResponseDto error = new PatchPlotResponseDto();
                error.setMessage("Plot number already exists");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }
            land.setPlotNo(request.getNewPlotNo());
        }

        if (request.getLandUse() != null && !request.getLandUse().isBlank()) {
            land.setLandUse(request.getLandUse());
        }

        if (request.getIsRegistered() != null) {
            land.setRegistered(request.getIsRegistered());
        }

        landRepository.save(land);

        PatchPlotResponseDto response = new PatchPlotResponseDto();
        response.setPlotNo(land.getPlotNo());
        response.setRegion(land.getRegion());
        response.setLandUse(land.getLandUse());
        response.setRegistered(land.isRegistered());
        response.setMessage("Plot updated successfully");

        return ResponseEntity.ok(response);
    }
}
```

### 1.5 Controller method

Add to **`controller/LandController.java`**:

```java
@Autowired
private PatchUpdatePlotService patchUpdatePlotService;

@PatchMapping("/plotNo/{plotNo}/region/{region}")
public ResponseEntity<PatchPlotResponseDto> patchPlot(
        @PathVariable String plotNo,
        @PathVariable String region,
        @RequestBody PatchPlotRequestDto request) {

    return patchUpdatePlotService.patchPlot(plotNo, region, request);
}
```

### 1.6 Test with curl

```bash
curl -X PATCH http://localhost:8081/api/v1/land/plotNo/P-001/region/Nairobi \
  -H "Content-Type: application/json" \
  -d '{"landUse": "Commercial"}'
```

---

## Step 2 — PUT (Full Replacement)

### 2.1 Request / response contract

**Endpoint:** `PUT /api/v1/land/plotNo/{plotNo}/region/{region}`

```json
{
  "plotNo": "P-001",
  "region": "Nairobi",
  "landUse": "Agricultural",
  "isRegistered": false
}
```

### 2.2 DTOs

**`dto/UpdatePlotRequestDto.java`**

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
public class UpdatePlotRequestDto {

    private String plotNo;
    private String region;
    private String landUse;
    private boolean isRegistered;
}
```

**`dto/UpdatePlotResponseDto.java`**

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
public class UpdatePlotResponseDto {

    private String plotNo;
    private String region;
    private String landUse;
    private boolean isRegistered;
    private String message;
}
```

### 2.3 Service interface

**`service/UpdatePlotService.java`**

```java
package com.ardhi.Land.registration.service;

import com.ardhi.Land.registration.dto.UpdatePlotRequestDto;
import com.ardhi.Land.registration.dto.UpdatePlotResponseDto;
import org.springframework.http.ResponseEntity;

public interface UpdatePlotService {

    ResponseEntity<UpdatePlotResponseDto> updatePlot(
            String plotNo, String region, UpdatePlotRequestDto request);
}
```

### 2.4 Service implementation

**`service/serviceImpl/UpdatePlotServiceImpl.java`**

```java
package com.ardhi.Land.registration.service.serviceImpl;

import com.ardhi.Land.registration.dto.UpdatePlotRequestDto;
import com.ardhi.Land.registration.dto.UpdatePlotResponseDto;
import com.ardhi.Land.registration.model.Land;
import com.ardhi.Land.registration.repository.LandRepository;
import com.ardhi.Land.registration.service.UpdatePlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UpdatePlotServiceImpl implements UpdatePlotService {

    @Autowired
    private LandRepository landRepository;

    @Override
    public ResponseEntity<UpdatePlotResponseDto> updatePlot(
            String plotNo, String region, UpdatePlotRequestDto request) {

        if (plotNo == null || plotNo.isBlank() || region == null || region.isBlank()) {
            UpdatePlotResponseDto error = new UpdatePlotResponseDto();
            error.setMessage("plotNo and region are required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        if (request.getPlotNo() == null || request.getPlotNo().isBlank()
                || request.getRegion() == null || request.getRegion().isBlank()
                || request.getLandUse() == null || request.getLandUse().isBlank()) {
            UpdatePlotResponseDto error = new UpdatePlotResponseDto();
            error.setMessage("plotNo, region, and landUse are required in request body");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        Optional<Land> existing = landRepository.findByPlotNoAndRegion(plotNo, region);

        if (existing.isEmpty()) {
            UpdatePlotResponseDto error = new UpdatePlotResponseDto();
            error.setMessage("Land not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        if (!request.getPlotNo().equals(plotNo) || !request.getRegion().equals(region)) {
            Optional<Land> conflict = landRepository.findByPlotNoAndRegion(
                    request.getPlotNo(), request.getRegion());
            if (conflict.isPresent() && !conflict.get().getId().equals(existing.get().getId())) {
                UpdatePlotResponseDto error = new UpdatePlotResponseDto();
                error.setMessage("A plot with the new plotNo and region already exists");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }
        }

        Land land = existing.get();
        land.setPlotNo(request.getPlotNo());
        land.setRegion(request.getRegion());
        land.setLandUse(request.getLandUse());
        land.setRegistered(request.isRegistered());
        landRepository.save(land);

        UpdatePlotResponseDto response = new UpdatePlotResponseDto();
        response.setPlotNo(land.getPlotNo());
        response.setRegion(land.getRegion());
        response.setLandUse(land.getLandUse());
        response.setRegistered(land.isRegistered());
        response.setMessage("Plot replaced successfully");

        return ResponseEntity.ok(response);
    }
}
```

### 2.5 Controller method

Add to **`controller/LandController.java`**:

```java
@Autowired
private UpdatePlotService updatePlotService;

@PutMapping("/plotNo/{plotNo}/region/{region}")
public ResponseEntity<UpdatePlotResponseDto> updatePlot(
        @PathVariable String plotNo,
        @PathVariable String region,
        @RequestBody UpdatePlotRequestDto request) {

    return updatePlotService.updatePlot(plotNo, region, request);
}
```

### 2.6 Test with curl

```bash
curl -X PUT http://localhost:8081/api/v1/land/plotNo/P-001/region/Nairobi \
  -H "Content-Type: application/json" \
  -d '{"plotNo":"P-001","region":"Nairobi","landUse":"Residential","isRegistered":true}'
```

---

## Step 3 — DELETE

### 3.1 DTO

**`dto/DeletePlotResponseDto.java`**

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
public class DeletePlotResponseDto {

    private String plotNo;
    private String region;
    private String message;
}
```

### 3.2 Service interface

**`service/DeletePlotService.java`**

```java
package com.ardhi.Land.registration.service;

import com.ardhi.Land.registration.dto.DeletePlotResponseDto;
import org.springframework.http.ResponseEntity;

public interface DeletePlotService {

    ResponseEntity<DeletePlotResponseDto> deletePlot(String plotNo, String region);
}
```

### 3.3 Service implementation

**`service/serviceImpl/DeletePlotServiceImpl.java`**

```java
package com.ardhi.Land.registration.service.serviceImpl;

import com.ardhi.Land.registration.dto.DeletePlotResponseDto;
import com.ardhi.Land.registration.model.Land;
import com.ardhi.Land.registration.repository.LandRepository;
import com.ardhi.Land.registration.service.DeletePlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DeletePlotServiceImpl implements DeletePlotService {

    @Autowired
    private LandRepository landRepository;

    @Override
    public ResponseEntity<DeletePlotResponseDto> deletePlot(String plotNo, String region) {

        if (plotNo == null || plotNo.isBlank() || region == null || region.isBlank()) {
            DeletePlotResponseDto error = new DeletePlotResponseDto();
            error.setMessage("plotNo and region are required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        Optional<Land> existing = landRepository.findByPlotNoAndRegion(plotNo, region);

        if (existing.isEmpty()) {
            DeletePlotResponseDto error = new DeletePlotResponseDto();
            error.setMessage("Land not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        Land land = existing.get();
        landRepository.delete(land);

        DeletePlotResponseDto response = new DeletePlotResponseDto();
        response.setPlotNo(plotNo);
        response.setRegion(region);
        response.setMessage("Plot deleted successfully");

        return ResponseEntity.ok(response);
    }
}
```

### 3.4 Controller method

Add to **`controller/LandController.java`**:

```java
@Autowired
private DeletePlotService deletePlotService;

@DeleteMapping("/plotNo/{plotNo}/region/{region}")
public ResponseEntity<DeletePlotResponseDto> deletePlot(
        @PathVariable String plotNo,
        @PathVariable String region) {

    return deletePlotService.deletePlot(plotNo, region);
}
```

### 3.5 Test with curl

```bash
curl -X DELETE http://localhost:8081/api/v1/land/plotNo/P-001/region/Nairobi
```

---

## Step 4 — OPTIONS and CORS

### 4.1 CORS configuration

**`config/CorsConfig.java`**

```java
package com.ardhi.Land.registration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:3000")
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .maxAge(3600);
            }
        };
    }
}
```

### 4.2 OPTIONS handlers

Add to **`controller/LandController.java`**:

```java
import org.springframework.http.HttpMethod;

@RequestMapping(method = RequestMethod.OPTIONS)
public ResponseEntity<Void> landOptions() {
    return ResponseEntity.ok()
            .allow(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT,
                   HttpMethod.PATCH, HttpMethod.DELETE, HttpMethod.OPTIONS)
            .build();
}

@RequestMapping(value = "/plotNo/{plotNo}/region/{region}", method = RequestMethod.OPTIONS)
public ResponseEntity<Void> plotOptions() {
    return ResponseEntity.ok()
            .allow(HttpMethod.GET, HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.DELETE)
            .build();
}
```

### 4.3 Test with curl

```bash
curl -X OPTIONS http://localhost:8081/api/v1/land \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: DELETE" \
  -v
```

---

## Step 5 — Complete LandController

**`controller/LandController.java`** (full file after Phase One):

```java
package com.ardhi.Land.registration.controller;

import com.ardhi.Land.registration.dto.*;
import com.ardhi.Land.registration.service.DeletePlotService;
import com.ardhi.Land.registration.service.RegisterPlotService;
import com.ardhi.Land.registration.service.SearchPlotService;
import com.ardhi.Land.registration.service.UpdatePlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/land")
public class LandController {

    @Autowired
    private RegisterPlotService registerPlotService;

    @Autowired
    private SearchPlotService searchPlotService;

    @Autowired
    private PatchUpdatePlotService patchUpdatePlotService;

    @Autowired
    private UpdatePlotService updatePlotService;

    @Autowired
    private DeletePlotService deletePlotService;

    @PostMapping("/register")
    public ResponseEntity<String> registerLand(@RequestBody RegisterPlotRequestDto request) {
        String response = registerPlotService.registerPlot(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getInfo/plotNo/{plotNo}/region/{region}")
    public ResponseEntity<SearchPlotResponseDto> getInfo(
            @PathVariable String plotNo,
            @PathVariable String region) {

        SearchPlotRequestDto searchPlotRequestDto = new SearchPlotRequestDto();
        searchPlotRequestDto.setPlotNo(plotNo);
        searchPlotRequestDto.setRegion(region);

        SearchPlotResponseDto response = searchPlotService.getPlot(searchPlotRequestDto);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/plotNo/{plotNo}/region/{region}")
    public ResponseEntity<PatchPlotResponseDto> patchPlot(
            @PathVariable String plotNo,
            @PathVariable String region,
            @RequestBody PatchPlotRequestDto request) {

        return patchUpdatePlotService.patchPlot(plotNo, region, request);
    }

    @PutMapping("/plotNo/{plotNo}/region/{region}")
    public ResponseEntity<UpdatePlotResponseDto> updatePlot(
            @PathVariable String plotNo,
            @PathVariable String region,
            @RequestBody UpdatePlotRequestDto request) {

        return updatePlotService.updatePlot(plotNo, region, request);
    }

    @DeleteMapping("/plotNo/{plotNo}/region/{region}")
    public ResponseEntity<DeletePlotResponseDto> deletePlot(
            @PathVariable String plotNo,
            @PathVariable String region) {

        return deletePlotService.deletePlot(plotNo, region);
    }

    @RequestMapping(method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> landOptions() {
        return ResponseEntity.ok()
                .allow(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT,
                        HttpMethod.PATCH, HttpMethod.DELETE, HttpMethod.OPTIONS)
                .build();
    }

    @RequestMapping(value = "/plotNo/{plotNo}/region/{region}", method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> plotOptions() {
        return ResponseEntity.ok()
                .allow(HttpMethod.GET, HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.DELETE)
                .build();
    }
}
```

---

## Step 6 — Fix Register Logic

In **`service/serviceImpl/RegisterPlotServiceImpl.java`**, fix the inverted condition:

```java
if (existing.isPresent()) {
    Land land = existing.get();
    if (land.isRegistered()) {
        return "Land already registered!!";
    }

    land.setLandUse(landUse);
    land.setRegistered(true);
    landRepository.save(land);
    return "Land successfully registered";
}
```

---

## Step 7 — Fix application.yaml

```yaml
spring:
  application:
    name: Land-registration
  datasource:
    url: jdbc:postgresql://localhost:5432/ardhidb
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

server:
  port: 8081
```

---

## Step 8 — File Checklist

```
src/main/java/com/ardhi/Land/registration/
├── config/
│   └── CorsConfig.java
├── controller/
│   └── LandController.java
├── dto/
│   ├── PatchPlotRequestDto.java
│   ├── PatchPlotResponseDto.java
│   ├── UpdatePlotRequestDto.java
│   ├── UpdatePlotResponseDto.java
│   └── DeletePlotResponseDto.java
├── service/
│   ├── PatchUpdatePlotService.java
│   ├── UpdatePlotService.java
│   ├── DeletePlotService.java
│   └── serviceImpl/
│       ├── PatchPlotServiceImpl.java
│       ├── UpdatePlotServiceImpl.java
│       ├── DeletePlotServiceImpl.java
│       └── RegisterPlotServiceImpl.java   (fix register bug)
```

---

## Step 9 — Manual Test Sequence

Run these in order on port `8081`:

```bash
# 1. Register
curl -X POST http://localhost:8081/api/v1/land/register \
  -H "Content-Type: application/json" \
  -d '{"plotNo":"P-001","region":"Nairobi","landUse":"Residential"}'

# 2. Get info
curl http://localhost:8081/api/v1/land/getInfo/plotNo/P-001/region/Nairobi

# 3. Patch landUse
curl -X PATCH http://localhost:8081/api/v1/land/plotNo/P-001/region/Nairobi \
  -H "Content-Type: application/json" \
  -d '{"landUse":"Commercial"}'

# 4. Put full replacement
curl -X PUT http://localhost:8081/api/v1/land/plotNo/P-001/region/Nairobi \
  -H "Content-Type: application/json" \
  -d '{"plotNo":"P-001","region":"Nairobi","landUse":"Agricultural","isRegistered":true}'

# 5. OPTIONS
curl -X OPTIONS http://localhost:8081/api/v1/land -v

# 6. Delete
curl -X DELETE http://localhost:8081/api/v1/land/plotNo/P-001/region/Nairobi
```

---

## Final API Summary

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/land/register` | Register a new land plot |
| `GET` | `/api/v1/land/getInfo/plotNo/{plotNo}/region/{region}` | Get plot details |
| `PATCH` | `/api/v1/land/plotNo/{plotNo}/region/{region}` | Partial update |
| `PUT` | `/api/v1/land/plotNo/{plotNo}/region/{region}` | Full replacement |
| `DELETE` | `/api/v1/land/plotNo/{plotNo}/region/{region}` | Delete a plot |
| `OPTIONS` | `/api/v1/land` | List allowed methods / CORS |

Proceed to **Phase Two** (`PHASE-TWO-README.md`) for authentication and authorization.
