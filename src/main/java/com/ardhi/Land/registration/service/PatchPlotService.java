package com.ardhi.Land.registration.service;


import com.ardhi.Land.registration.dto.PatchPlotRequestDto;
import com.ardhi.Land.registration.dto.PatchPlotResponseDto;

import java.util.UUID;

public interface PatchPlotService {

    PatchPlotResponseDto patchPlot(UUID id, PatchPlotRequestDto patchPlotRequestDto);
}
