package com.ardhi.Land.registration.service;

import com.ardhi.Land.registration.dto.DeletePlotRequestDto;
import com.ardhi.Land.registration.dto.DeletePlotResponseDto;

import java.util.UUID;

public interface DeletePlotService {

    DeletePlotResponseDto deletePlot(UUID id, DeletePlotRequestDto deletePlotRequestDto);

}
