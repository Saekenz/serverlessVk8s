package at.ac.univie.catalogservice.controller;

import at.ac.univie.catalogservice.model.Category;
import at.ac.univie.catalogservice.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private ICategoryService categoryService;

    @GetMapping
    public List<Category> findAllCategories() {
        return categoryService.findAll();
    }

    @PostMapping
    public Category createCategory(@RequestBody Category category) {
        return categoryService.save(category);
    }

    @GetMapping("/{id}")
    public Category findCategoryById(@PathVariable Long id) {
        return categoryService.findById(id);
    }
}
