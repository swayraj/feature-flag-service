package com.flagservice.feature_flag_service.controller;

import com.flagservice.feature_flag_service.exception.FlagNotFoundException;
import com.flagservice.feature_flag_service.exception.FlagValidationException;
import com.flagservice.feature_flag_service.model.Flag;
import com.flagservice.feature_flag_service.service.FlagService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/flags")
public class FlagController {

    private FlagService flagService;

    public FlagController(FlagService flagService)
    {
        this.flagService = flagService;
    }

    @GetMapping
    public ResponseEntity<List<Flag>> getAllFlags()
    {
        List<Flag> flags = flagService.getAllFlags();
        return ResponseEntity.ok(flags);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFlagById(@PathVariable Long id) {
        try {
            Flag flag = flagService.getFlagById(id)
                    .orElseThrow(() -> new FlagNotFoundException(id));
            return ResponseEntity.ok(flag);
        } catch (FlagNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    //create a Flag
    @PostMapping
    public ResponseEntity<?> createFlag(@RequestBody Flag flag) {
        try {
            Flag created = flagService.createFlag(flag);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (FlagValidationException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //Update an existing flag
    @PutMapping("/{id}")
    public ResponseEntity<?> updateFlag(@PathVariable Long id, @RequestBody Flag flag) {
        try {
            Flag updated = flagService.updateFlag(id, flag);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //Delete a Flag
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFlag(@PathVariable Long id) {
        try {
            flagService.deleteFlag(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //Search Flag by Name
    @GetMapping("/search")
    public ResponseEntity<List<Flag>> searchFlags(@RequestParam(required = false) String name) {
        List<Flag> results = flagService.searchFlagsByName(name);
        return ResponseEntity.ok(results);
    }

    //Get enabled Flags
    @GetMapping("/enabled")
    public ResponseEntity<List<Flag>> getEnabledFlags() {
        List<Flag> enabled = flagService.getEnabledFlags();
        return ResponseEntity.ok(enabled);
    }

    //Toggle Flag On or Off
    @PostMapping("/{id}/toggle")
    public ResponseEntity<?> toggleFlag(@PathVariable Long id) {
        try {
            Flag toggled = flagService.toggleFlag(id);
            return ResponseEntity.ok(toggled);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



}
