package com.ardhi.Land.registration.service.serviceImpl;

import com.ardhi.Land.registration.dto.SearchPlotRequestDto;
import com.ardhi.Land.registration.dto.SearchPlotResponseDto;
import com.ardhi.Land.registration.model.Land;
import com.ardhi.Land.registration.repository.LandRepository;
import com.ardhi.Land.registration.service.SearchPlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class SearchPlotServiceImpl implements SearchPlotService {

    @Autowired
    LandRepository landRepository;

    @Override
    public SearchPlotResponseDto getPlot(SearchPlotRequestDto searchPlotRequestDto) {

        String plotNo = searchPlotRequestDto.getPlotNo();
        String region = searchPlotRequestDto.getRegion();

        if (plotNo == null || plotNo.isBlank()
            || region == null || region.isBlank()
        ){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please fill the required fields"
            );
        }

        Optional<Land> exists = landRepository.findByPlotNoAndRegion(plotNo, region);

        if(exists.isEmpty()){
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Plot Not Found"
            );
        }

        Land land = exists.get();

        SearchPlotResponseDto response = new SearchPlotResponseDto();
        response.setPlotNo(land.getPlotNo());
        response.setRegion(land.getRegion());
        response.setLandUse(land.getLandUse());
        response.setRegistered(land.isRegistered());
        return response;
    }
}
