package com.ls.spaceBookingSystem.controllers;

import com.ls.spaceBookingSystem.dtos.requests.CreateTemplateRequest;
import com.ls.spaceBookingSystem.dtos.requests.UpdateRulesRequest;
import com.ls.spaceBookingSystem.dtos.requests.UpdateTemplateRequest;
import com.ls.spaceBookingSystem.dtos.responses.CreateTemplateResponse;
import com.ls.spaceBookingSystem.dtos.responses.UpdateTemplateResponse;
import com.ls.spaceBookingSystem.dtos.responses.availability.AvailabilityRuleResponseDto;
import com.ls.spaceBookingSystem.dtos.responses.availability.AvailabilityTemplateDto;
import com.ls.spaceBookingSystem.services.AvailabilityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("availability")
public class AvailabilityController {

    @Autowired
    private AvailabilityService availabilityService;

    @GetMapping
    public ResponseEntity<List<AvailabilityTemplateDto>> getTemplates() {
        return new ResponseEntity<>(availabilityService.getTemplates(),HttpStatus.OK);
    }

    @GetMapping("{templateId}")
    public ResponseEntity<AvailabilityTemplateDto> getTemplate(@PathVariable Long templateId) {
        return new ResponseEntity<>(availabilityService.getTemplate(templateId),HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<CreateTemplateResponse> createTemplate(@RequestBody @Valid CreateTemplateRequest data) {
        return new ResponseEntity<>(availabilityService.createTemplate(data), HttpStatus.CREATED);
    }

    @PutMapping("{templateId}")
    public ResponseEntity<UpdateTemplateResponse> updateTemplate(@RequestBody @Valid UpdateTemplateRequest data, @PathVariable Long templateId) {
        System.out.println("update Template");
        return new ResponseEntity<>(availabilityService.updateTemplate(templateId, data), HttpStatus.CREATED);
    }

    @PutMapping("/{templateId}/rules")
    public ResponseEntity<String> updateRules(@RequestBody @Valid UpdateRulesRequest data, @PathVariable Long templateId) {
        availabilityService.updateRules(templateId, data);
        return ResponseEntity.ok("Successfully updated rules");
    }

    @GetMapping("/{templateId}/rules")
    public ResponseEntity<List<AvailabilityRuleResponseDto>> getTemplateRules(@PathVariable Long templateId) {

        return new ResponseEntity<>(availabilityService.getTemplateRules(templateId), HttpStatus.OK
        );
    }
}
