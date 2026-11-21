package com.collegecart.dto;

import com.collegecart.entity.Category;

public class CategoryResponse {

    private Long id;
    private String name;
    private boolean active;

    public CategoryResponse(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.active = category.isActive();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }
}
