
package ru.practicum.ewm.category;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryDto createCategory(NewCategoryDto request) {
        Category category = CategoryMapper.toCategory(request);

        Category savedCategory = categoryRepository.save(category);

        return CategoryMapper.toCategoryDto(savedCategory);
    }

    public List<CategoryDto> getCategories(int from, int size) {
        return categoryRepository.findAll()
                .stream()
                .skip(from)
                .limit(size)
                .map(CategoryMapper::toCategoryDto)
                .toList();
    }


    public CategoryDto getCategoryById(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Category not found: " + catId
                ));

        return CategoryMapper.toCategoryDto(category);
    }


    public CategoryDto updateCategory(Long catId, NewCategoryDto request) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Category not found: " + catId
                ));

        category.setName(request.getName());

        Category updatedCategory = categoryRepository.save(category);

        return CategoryMapper.toCategoryDto(updatedCategory);
    }


    public void deleteCategory(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Category not found: " + catId
                ));

        categoryRepository.delete(category);
    }




}
