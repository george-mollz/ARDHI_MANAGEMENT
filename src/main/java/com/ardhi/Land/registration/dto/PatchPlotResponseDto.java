package com.ardhi.Land.registration.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatchPlotResponseDto {

    private UUID id;
    private String plotNo;
    private String region;

}
