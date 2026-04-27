package com.ls.spaceBookingSystem.controllers;

import com.ls.spaceBookingSystem.dtos.requests.CreateSpaceRequestDto;
import com.ls.spaceBookingSystem.dtos.requests.UpdateSpaceRequestDto;
import com.ls.spaceBookingSystem.dtos.responses.SpaceResponseDto;
import com.ls.spaceBookingSystem.services.SpaceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("spaces")
public class SpaceController {

    @Autowired
    SpaceService spaceService;

    @GetMapping
    public ResponseEntity<Page<SpaceResponseDto>> getSpaces(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(spaceService.getSpaces(name, page, size));
    }

    @GetMapping("/{spaceId}")
    public ResponseEntity<SpaceResponseDto> getSpace(@PathVariable Long spaceId) {
        return ResponseEntity.ok(spaceService.getSpace(spaceId));
    }

    @PostMapping
    public ResponseEntity<SpaceResponseDto> createSpace(
            @RequestBody @Valid CreateSpaceRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(spaceService.createSpace(request));
    }

    @PutMapping("/{spaceId}")
    public ResponseEntity<SpaceResponseDto> updateSpace(
            @PathVariable Long spaceId,
            @RequestBody @Valid UpdateSpaceRequestDto request) {
        return ResponseEntity.ok(spaceService.updateSpace(spaceId, request));
    }

    @DeleteMapping("/{spaceId}")
    public ResponseEntity<Void> deleteSpace(@PathVariable Long spaceId) {
        spaceService.deleteSpace(spaceId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{spaceId}/toggle")
    public ResponseEntity<SpaceResponseDto> toggleActive(@PathVariable Long spaceId) {
        return ResponseEntity.ok(spaceService.toggleActive(spaceId));
    }
}
