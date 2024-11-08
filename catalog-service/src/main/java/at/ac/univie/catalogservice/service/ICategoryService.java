package at.ac.univie.catalogservice.service;

import at.ac.univie.catalogservice.model.Category;

import java.util.List;

public interface ICategoryService {

    List<Category> findAll();

    Category findById(Long id);

    Category save(Category category);

    Category getReferenceById(Long id);
}
