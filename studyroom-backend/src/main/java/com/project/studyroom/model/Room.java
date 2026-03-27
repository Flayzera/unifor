package com.project.studyroom.model;

import com.google.cloud.firestore.annotation.DocumentId;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class Room {

    @DocumentId
    private String id;
    @NotBlank(message = "Room name is required.")
    private String name;
    @Min(value = 1, message = "Capacity must be at least 1.")
    private int capacity;
    private List<String> resources;
    private boolean active;

    public Room() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public List<String> getResources() {
        return resources;
    }

    public void setResources(List<String> resources) {
        this.resources = resources;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
