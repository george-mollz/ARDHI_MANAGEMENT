package com.ardhi.Land.registration.service.serviceImpl;

import com.ardhi.Land.registration.dto.DeletePlotRequestDto;
import com.ardhi.Land.registration.dto.DeletePlotResponseDto;
import com.ardhi.Land.registration.model.Land;
import com.ardhi.Land.registration.repository.LandRepository;
import com.ardhi.Land.registration.service.DeletePlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;


@Service
public class DeletePlotServiceImpl implements DeletePlotService {


    @Autowired
    LandRepository landRepository;


    @Override
    public DeletePlotResponseDto deletePlot(UUID id, DeletePlotRequestDto  deletePlotRequestDto) {

        //Create response DTO
        // If plotNo or region is null → set message, return response
       //findById(id) → if empty, set message "Plot Not Found!!!", return response
//findByPlotNoAndRegion(...)
//If empty → set message, return response
//If id does not match → set message, return response (do NOT delete)
//landRepository.delete(plotNoRegion.get())   ← delete HERE, before success return
//Set plotNo, region, message "Plot Deleted Successfully"
//return response



}
