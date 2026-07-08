package com.ardhi.Land.registration.controller;


import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ardhi.Land.registration.dto.DeletePlotRequestDto;
import com.ardhi.Land.registration.dto.DeletePlotResponseDto;
import com.ardhi.Land.registration.dto.PatchPlotRequestDto;
import com.ardhi.Land.registration.dto.PatchPlotResponseDto;
import com.ardhi.Land.registration.dto.RegisterPlotRequestDto;
import com.ardhi.Land.registration.dto.SearchPlotRequestDto;
import com.ardhi.Land.registration.dto.SearchPlotResponseDto;
import com.ardhi.Land.registration.service.DeletePlotService;
import com.ardhi.Land.registration.service.PatchPlotService;
import com.ardhi.Land.registration.service.RegisterPlotService;
import com.ardhi.Land.registration.service.SearchPlotService;

@RestController
@RequestMapping ("/api/v1/land")
public class LandController {

    @Autowired
    private RegisterPlotService registerPlotService;

    @Autowired()
    private SearchPlotService searchPlotService;
    
    @Autowired
    private PatchPlotService patchPlotService;

    @Autowired
    private DeletePlotService deletePlotService;


    @PostMapping("/register")
    public ResponseEntity<String> registerLand(@RequestBody RegisterPlotRequestDto registerPlotRequestDto){
        String response = registerPlotService.registerPlot(registerPlotRequestDto);

                return ResponseEntity.ok(response);

    }



    @GetMapping("/getInfo/plotNo/{plotNo}/region/{region}")
    public ResponseEntity<SearchPlotResponseDto> getInfo(@PathVariable String plotNo, @PathVariable String region){

        SearchPlotRequestDto searchPlotRequestDto = new SearchPlotRequestDto();
        searchPlotRequestDto.setPlotNo(plotNo);
        searchPlotRequestDto.setRegion(region);

        SearchPlotResponseDto response = searchPlotService.getPlot(searchPlotRequestDto);

        return ResponseEntity.ok(response);
    }



    @PatchMapping("/patch/plotNo/{id}")
    public ResponseEntity<PatchPlotResponseDto> patchPlotController(@PathVariable UUID id, @RequestBody PatchPlotRequestDto patchPlotRequestDto){
        PatchPlotResponseDto response = patchPlotService.patchPlot(id, patchPlotRequestDto);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/delete/plotNoRegion/{id}")
    public ResponseEntity<DeletePlotResponseDto> deletePlotController(@PathVariable UUID id, @RequestBody DeletePlotRequestDto deletePlotRequestDto){

        DeletePlotResponseDto deletePlotResponseDto = deletePlotService
                .deletePlot(id, deletePlotRequestDto);


        return ResponseEntity.ok(deletePlotResponseDto);
    }







}
