package com.ardhi.Land.registration.service;


import com.ardhi.Land.registration.dto.PatchPlotRequestDto;
import com.ardhi.Land.registration.dto.PatchPlotResponseDto;

public interface PatchPlotService {

    PatchPlotResponseDto patchPlot(PatchPlotRequestDto patchPlotRequestDto);
}
