package com.ardhi.Land.registration.service;

import com.ardhi.Land.registration.dto.SearchPlotRequestDto;
import com.ardhi.Land.registration.dto.SearchPlotResponseDto;

public interface SearchPlotService {

    SearchPlotResponseDto getPlot(SearchPlotRequestDto searchPlotRequestDto);
}
