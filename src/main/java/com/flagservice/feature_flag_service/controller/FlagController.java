package com.flagservice.feature_flag_service.controller;

import com.flagservice.feature_flag_service.model.Flag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/flags")
public class FlagController {

    private List<Flag> flags = new ArrayList<>();
    private Long nextId = 1L;

    public FlagController()
    {
        flags.add(new Flag(nextId++, "dark_mode", "Enable Dark Mode", false, 0));
        flags.add(new Flag(nextId++, "new_checkout", "New checkout flow", true, 25));
        flags.add(new Flag(nextId++, "ai_recommendations", "AI-powered product recommendations", true, 50));
    }

    //Get all flags
    @GetMapping
    public ResponseEntity<List<Flag>> getAllFlags()
    {
        return ResponseEntity.ok(flags);
    }

    //Get flag by id
    @GetMapping("/{id}")
    public ResponseEntity<Flag> getFlagById(@PathVariable Long id)
    {
        Optional<Flag> flag = flags.stream()
                .filter(f -> f.getId().equals(id))
                .findFirst();

        if(flag.isPresent())
        {
            return ResponseEntity.ok(flag.get());
        }
        else
        {
            return ResponseEntity.notFound().build();
        }
    }

    //Create a new flag
    @PostMapping
    public ResponseEntity<Flag> createFlag(@RequestBody Flag flag)
    {
        flag.setId(nextId++);
        flags.add(flag);
        return ResponseEntity.status(HttpStatus.CREATED).body(flag);
    }

    //Update a flag
    @PutMapping("/{id}")
    public ResponseEntity<Flag> updateFlag(@PathVariable Long id, @RequestBody Flag updatedFlag)
    {
        Optional<Flag> existingFlag = flags.stream()
                .filter(f -> f.getId().equals(id))
                .findFirst();

        if (existingFlag.isPresent()) {
            Flag flag = existingFlag.get();
            flag.setName(updatedFlag.getName());
            flag.setDescription(updatedFlag.getDescription());
            flag.setEnabled(updatedFlag.isEnabled());
            flag.setRolloutPercentage(updatedFlag.getRolloutPercentage());
            return ResponseEntity.ok(flag);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    //Delete a flag
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlag(@PathVariable Long id) {
        boolean removed = flags.removeIf(f -> f.getId().equals(id));

        if (removed) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    //Search flag by name
    @GetMapping("/search")
    public ResponseEntity<List<Flag>> searchFlags(@RequestParam String name) {
        List<Flag> results = flags.stream()
                .filter(f -> f.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
        return ResponseEntity.ok(results);
    }



    

}
