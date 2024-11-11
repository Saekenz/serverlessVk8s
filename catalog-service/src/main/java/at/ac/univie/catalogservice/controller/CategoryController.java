package at.ac.univie.catalogservice.controller;

import at.ac.univie.catalogservice.model.Category;
import at.ac.univie.catalogservice.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private ICategoryService categoryService;

    @GetMapping
    public ResponseEntity<?> findAllCategories() {
        return categoryService.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody Category category) {
        return categoryService.save(category);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findCategoryById(@PathVariable Long id) {
        return categoryService.findById(id);
    }
}
