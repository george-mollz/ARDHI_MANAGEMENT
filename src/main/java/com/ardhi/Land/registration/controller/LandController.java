package com.ardhi.Land.registration.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ardhi.Land.registration.dto.PatchPlotRequestDto;
import com.ardhi.Land.registration.dto.PatchPlotResponseDto;
import com.ardhi.Land.registration.dto.RegisterPlotRequestDto;
import com.ardhi.Land.registration.dto.SearchPlotRequestDto;
import com.ardhi.Land.registration.dto.SearchPlotResponseDto;
import com.ardhi.Land.registration.service.serviceImpl.PatchPlotServiceImpl;
import com.ardhi.Land.registration.service.serviceImpl.RegisterPlotServiceImpl;
import com.ardhi.Land.registration.service.serviceImpl.SearchPlotServiceImpl;

import java.util.UUID;

@RestController
@RequestMapping ("/api/v1/land")
public class LandController {

    @Autowired
    private RegisterPlotServiceImpl registerPlotServiceImpl;

    @Autowired()
    private SearchPlotServiceImpl searchPlotServiceImpl;
    
    @Autowired
    private PatchPlotServiceImpl patchPlotServiceImpl;


    @PostMapping("/register")
    public ResponseEntity<String> registerLand(@RequestBody RegisterPlotRequestDto registerPlotRequestDto){
        String response = registerPlotServiceImpl.registerPlot(registerPlotRequestDto);

                return ResponseEntity.ok(response);

    }



    @GetMapping("/getInfo/plotNo/{plotNo}/region/{region}")
    public ResponseEntity<SearchPlotResponseDto> getInfo(@PathVariable String plotNo, @PathVariable String region){

        SearchPlotRequestDto searchPlotRequestDto = new SearchPlotRequestDto();
        searchPlotRequestDto.setPlotNo(plotNo);
        searchPlotRequestDto.setRegion(region);

        SearchPlotResponseDto response = searchPlotServiceImpl.getPlot(searchPlotRequestDto);

        return ResponseEntity.ok(response);
    }



    @PatchMapping("/patch/plotNo/{id}")
    public ResponseEntity<PatchPlotResponseDto> patchPlotController(@PathVariable UUID id, @RequestBody PatchPlotRequestDto patchPlotRequestDto){
        PatchPlotResponseDto response = patchPlotServiceImpl.patchPlot(id, patchPlotRequestDto);
        return ResponseEntity.ok(response);
    }







}
