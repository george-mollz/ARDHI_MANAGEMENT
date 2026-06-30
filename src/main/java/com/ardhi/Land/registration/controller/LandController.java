package com.ardhi.Land.registration.controller;


import com.ardhi.Land.registration.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ardhi.Land.registration.service.serviceImpl.RegisterPlotServiceImpl;
import com.ardhi.Land.registration.service.serviceImpl.PatchPlotServiceImpl;
import com.ardhi.Land.registration.service.serviceImpl.SearchPlotServiceImpl;

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



    @PatchMapping("/patch/plotNo")
    public ResponseEntity<PatchPlotResponseDto> patchPlotController(@RequestBody PatchPlotRequestDto patchPlotRequestDto){
        
        PatchPlotResponseDto response = patchPlotServiceImpl.patchPlot(patchPlotRequestDto);
        return ResponseEntity.ok(response);
    }







}
