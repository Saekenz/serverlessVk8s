package at.ac.univie.catalogservice.service;

import at.ac.univie.catalogservice.model.CategoryDTO;
import at.ac.univie.catalogservice.repository.CategoryRepository;
import at.ac.univie.catalogservice.model.Category;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements ICategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private Environment env;

    @Override
    public ResponseEntity<?> findAll() {
        List<CategoryDTO> categories = categoryRepository.findAll().stream()
                .map(cat -> modelMapper.map(cat, CategoryDTO.class))
                .toList();
        return ResponseEntity.ok(categories);
    }

    @Override
    public ResponseEntity<?> findById(Long id) {
        Optional<Category> category = categoryRepository.findById(id);

        if (category.isPresent()) {
            return ResponseEntity.ok(modelMapper.map(category.get(), CategoryDTO.class));
        }
        else {
            return new ResponseEntity<>(String.format("Category with id %s could not be found!", id),
                    HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ResponseEntity<?> save(Category category) {
        try {
            CategoryDTO categoryDTO = modelMapper.map(categoryRepository.save(category), CategoryDTO.class);
            String newCategoryLocation = env.getProperty("cat.app.url") + categoryDTO.getId();

            return ResponseEntity.created(new URI(newCategoryLocation)).body(categoryDTO);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Category getReferenceById(Long id) {
        return categoryRepository.getReferenceById(id);
    }
}
