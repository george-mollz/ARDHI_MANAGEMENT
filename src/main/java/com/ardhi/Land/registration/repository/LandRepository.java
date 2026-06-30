package com.ardhi.Land.registration.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ardhi.Land.registration.model.Land;

public interface LandRepository extends JpaRepository<Land, UUID> {
   Optional<Land> findById(UUID id);

   Optional<Land> findByPlotNo(String plotNo);

   List<Land> findByRegion(String region);

   Optional<Land> findByPlotNoAndRegion(String plotNo, String region);



}
