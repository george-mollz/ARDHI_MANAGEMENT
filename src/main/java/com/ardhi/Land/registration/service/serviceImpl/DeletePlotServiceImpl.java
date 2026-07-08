package com.ardhi.Land.registration.service.serviceImpl;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ardhi.Land.registration.dto.DeletePlotRequestDto;
import com.ardhi.Land.registration.dto.DeletePlotResponseDto;
import com.ardhi.Land.registration.model.Land;
import com.ardhi.Land.registration.repository.LandRepository;
import com.ardhi.Land.registration.service.DeletePlotService;


@Service
public class DeletePlotServiceImpl implements DeletePlotService {


    @Autowired
    LandRepository landRepository;


    @Override
    public DeletePlotResponseDto deletePlot(UUID id, DeletePlotRequestDto  deletePlotRequestDto) {


        //Create response DTO
        DeletePlotResponseDto deletePlotResponseDto = new DeletePlotResponseDto();

        deletePlotResponseDto.setPlotNo(deletePlotRequestDto.getPlotNo());
        deletePlotResponseDto.setRegion(deletePlotRequestDto.getRegion());

        // If plotNo or region is null → set message, return response
        if (deletePlotRequestDto.getPlotNo() == null
                || deletePlotRequestDto.getRegion() == null
        ) {
            deletePlotResponseDto.setMessage("PlotNo and Region required");

            return deletePlotResponseDto;
        }


        //findById(id) → if empty, set message "Plot Not Found!!!", return response
        Optional<Land> landId = landRepository.findById(id);




        // If empty → set message, return response
        if (landId.isEmpty()) {
            deletePlotResponseDto.setMessage("Land not found");
            return deletePlotResponseDto;
        }

        // findByPlotNoAndRegion(...)
        Optional<Land> plotNoRegion = landRepository.findByPlotNoAndRegion(deletePlotRequestDto.getPlotNo(), deletePlotRequestDto.getRegion());

        //If id does not match → set message, return response (do NOT delete)
        if(!landId.get().getId().equals(id)){
            deletePlotResponseDto.setMessage("DO NOT DELETE");
            return deletePlotResponseDto;
        }


        //landRepository.delete(plotNoRegion.get())   ← delete HERE, before success return
        landRepository.delete(landId.get());

        //Set plotNo, region, message "Plot Deleted Successfully"
        deletePlotResponseDto.setPlotNo(null);
        deletePlotResponseDto.setRegion(null);
        deletePlotResponseDto.setMessage("Land deleted");
        //return response
        return deletePlotResponseDto;


    }

}
