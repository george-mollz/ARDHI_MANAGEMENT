package com.ardhi.Land.registration.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchPlotResponseDto {
    private String plotNo;
    private String region;
    private String landUse;
    private boolean isRegistered;
}
