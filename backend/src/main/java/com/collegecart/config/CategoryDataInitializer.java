package com.collegecart.config;

import com.collegecart.entity.Category;
import com.collegecart.repository.CategoryRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CategoryDataInitializer implements CommandLineRunner {

    private static final List<String> INITIAL_CATEGORIES = List.of(
            "Stationery",
            "Books",
            "Electronics",
            "Snacks",
            "Accessories"
    );

    private final CategoryRepository categoryRepository;

    public CategoryDataInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        for (String name : INITIAL_CATEGORIES) {
            if (!categoryRepository.existsByName(name)) {
                Category category = new Category();
                category.setName(name);
                categoryRepository.save(category);
            }
        }
    }
}
