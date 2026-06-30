package com.ardhi.Land.registration.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatchPlotRequestDto {

    private UUID plotId;
    private String plotNo;

}
