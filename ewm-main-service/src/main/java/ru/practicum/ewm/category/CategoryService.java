
package ru.practicum.ewm.category;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.event.EventRepository;

@Service
@Transactional(readOnly = true)
public class CategoryService {


    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    public CategoryService(
            CategoryRepository categoryRepository,
            EventRepository eventRepository) {
        this.categoryRepository = categoryRepository;
        this.eventRepository = eventRepository;
    }


    @Transactional
    public CategoryDto createCategory(NewCategoryDto request) {
        Category category = CategoryMapper.toCategory(request);

        Category savedCategory = categoryRepository.save(category);

        return CategoryMapper.toCategoryDto(savedCategory);
    }


    public List<CategoryDto> getCategories(int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        return categoryRepository.findAll(pageable)
                .getContent()
                .stream()
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


    @Transactional
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



    @Transactional
    public void deleteCategory(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Category not found: " + catId
                ));

        if (eventRepository.existsByCategory_Id(catId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete category with existing events"
            );
        }

        categoryRepository.delete(category);
    }





}
