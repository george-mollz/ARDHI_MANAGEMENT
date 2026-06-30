package com.ardhi.Land.registration.service.serviceImpl;

import java.util.Optional;
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
    public PatchPlotResponseDto patchPlot(PatchPlotRequestDto patchPlotRequestDto){
        UUID plotId = patchPlotRequestDto.getPlotId();
        String plotNo = patchPlotRequestDto.getPlotNo();

        if (plotId == null) {
            PatchPlotResponseDto errorResponseDto = new PatchPlotResponseDto();
            errorResponseDto.setMessage("plotId is required");
            return errorResponseDto;
        }

        if (plotNo == null || plotNo.isBlank()) {
            PatchPlotResponseDto errorResponseDto = new PatchPlotResponseDto();
            errorResponseDto.setMessage("Please fill the required fields");
            return errorResponseDto;
        }

        Optional<Land> existing = landRepository.findById(plotId);

        if (existing.isEmpty()) {
            PatchPlotResponseDto errorResponseDto = new PatchPlotResponseDto();
            errorResponseDto.setMessage("PlotId not found!!!");
            return errorResponseDto;
        }

        Land land = existing.get();

        Optional<Land> duplicate = landRepository.findByPlotNo(plotNo);
        if (duplicate.isPresent() && !duplicate.get().getId().equals(plotId)) {
            PatchPlotResponseDto errorResponseDto = new PatchPlotResponseDto();
            errorResponseDto.setMessage("Plot number already exists");
            return errorResponseDto;
        }

        land.setPlotNo(plotNo);
        landRepository.save(land);

        PatchPlotResponseDto response = new PatchPlotResponseDto();
        response.setPlotNo(land.getPlotNo());
        response.setRegion(land.getRegion());
        response.setMessage("PlotNo. updated successfully!!!");

        return response;
    }
}
