package com.ardhi.Land.registration.service.serviceImpl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ardhi.Land.registration.dto.PatchPlotRequestDto;
import com.ardhi.Land.registration.dto.PatchPlotResponseDto;
import com.ardhi.Land.registration.model.Land;
import com.ardhi.Land.registration.repository.LandRepository;
import com.ardhi.Land.registration.service.PatchPlotService;

@Service
public class PatchPlotServiceImpl implements PatchPlotService {

    @Autowired
    LandRepository landRepository;

    @Override
    public PatchPlotResponseDto patchPlot(UUID id, PatchPlotRequestDto patchPlotRequestDto){

        //Create new model object of User type
        //The store the user id from repository
        Land land = landRepository.findById(id)
                .orElseThrow(()->
                        new RuntimeException("Plot not found!!!"));

        if(patchPlotRequestDto.getPlotNo() != null ){
            land.setPlotNo(patchPlotRequestDto.getPlotNo());
        }


        Land savedLand = landRepository.save(land);

        PatchPlotResponseDto response = new PatchPlotResponseDto();


        response.setId(savedLand.getId());
        response.setRegion(savedLand.getRegion());
        response.setPlotNo(savedLand.getPlotNo());



        return response;
        /***
         *  Example
         * {
         *    id: 08432u049303939840394,
         *    plotNo: P-1000,
         *    region: Nairobi
         */



    }
}
