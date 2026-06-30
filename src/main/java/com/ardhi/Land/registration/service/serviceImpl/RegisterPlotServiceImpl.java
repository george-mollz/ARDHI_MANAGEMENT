package com.ardhi.Land.registration.service.serviceImpl;

import com.ardhi.Land.registration.dto.RegisterPlotRequestDto;
import com.ardhi.Land.registration.model.Land;
import com.ardhi.Land.registration.repository.LandRepository;
import com.ardhi.Land.registration.service.RegisterPlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RegisterPlotServiceImpl implements RegisterPlotService {

   @Autowired
   private LandRepository landRepository;


   @Override
   public String registerPlot(RegisterPlotRequestDto request){
      String plotNo = request.getPlotNo();
      String region = request.getRegion();
      String landUse = request.getLandUse();
    
    
    if (plotNo == null || plotNo.isBlank()
            || region == null || region.isBlank()
            || landUse == null || landUse.isBlank()) {
        return "plotNo, Region, and landUse are required";
    }
    
    
    Optional<Land> existing = landRepository.findByPlotNoAndRegion(plotNo, region);
    

    if (existing.isPresent()) {
        Land land = existing.get();
        if (!land.isRegistered()) {
            return "Land already registered!!";
        }

        land.setLandUse(landUse);
        land.setRegistered(true);
        landRepository.save(land);
        return "Land successfully registered";
    }
    
    Land newLand = new Land();
    newLand.setPlotNo(plotNo);
    newLand.setRegion(region);
    newLand.setLandUse(landUse);
    newLand.setRegistered(true);
    landRepository.save(newLand);
    return "Land successfully registered";


   }

}


