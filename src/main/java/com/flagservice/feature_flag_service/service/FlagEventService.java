package com.flagservice.feature_flag_service.service;

import com.flagservice.feature_flag_service.model.Flag;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class FlagEventService {

    private final SimpMessagingTemplate messagingTemplate;

    public FlagEventService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastFlagCreated(Flag flag) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "FLAG_CREATED");
        event.put("flagId", flag.getId());
        event.put("flagName", flag.getName());
        event.put("enabled", flag.isEnabled());
        event.put("rolloutPercentage", flag.getRolloutPercentage());
        event.put("timestamp", LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/flags", (Object) event);  // ← Cast to Object
        System.out.println("📡 Broadcasted: FLAG_CREATED - " + flag.getName());
    }

    public void broadcastFlagUpdated(Flag flag) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "FLAG_UPDATED");
        event.put("flagId", flag.getId());
        event.put("flagName", flag.getName());
        event.put("enabled", flag.isEnabled());
        event.put("rolloutPercentage", flag.getRolloutPercentage());
        event.put("timestamp", LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/flags", (Object) event);  // ← Cast to Object
        System.out.println("📡 Broadcasted: FLAG_UPDATED - " + flag.getName());
    }

    public void broadcastFlagDeleted(Long flagId, String flagName) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "FLAG_DELETED");
        event.put("flagId", flagId);
        event.put("flagName", flagName);
        event.put("timestamp", LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/flags", (Object) event);  // ← Cast to Object
        System.out.println("📡 Broadcasted: FLAG_DELETED - " + flagName);
    }

    public void broadcastFlagToggled(Flag flag) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "FLAG_TOGGLED");
        event.put("flagId", flag.getId());
        event.put("flagName", flag.getName());
        event.put("enabled", flag.isEnabled());
        event.put("timestamp", LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/flags", (Object) event);  // ← Cast to Object
        System.out.println("📡 Broadcasted: FLAG_TOGGLED - " + flag.getName() + " → " + flag.isEnabled());
    }
}