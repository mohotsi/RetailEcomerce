package za.co.monate.retail.catalog.service; // Adjust package

import za.co.monate.retail.catalog.dto.CategoryNodeDto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.monate.retail.catalog.model.Category;
import za.co.monate.retail.catalog.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Serves the Angular Navigation Bar
     */
    @Transactional(readOnly = true)
    public List<CategoryNodeDto> getNavigationTree() {
        // Only fetches root categories where showInNav = true and time-boxing is valid
        List<Category> rootCategories = categoryRepository.findActiveNavigationTree();
        
        return rootCategories.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Serves the Angular Product Grid when clicking a promotional banner
     */
    @Transactional(readOnly = true)
    public Optional<CategoryNodeDto> getValidCategoryBySlug(String slug) {
        return categoryRepository.findBySeoSlug(slug)
                .filter(Category::isCurrentlyValid) // Enforces time-boxing expiration
                .map(this::mapToDto);
    }

    /**
     * Recursive mapping utilizing your existing CategoryNodeDto
     */
    private CategoryNodeDto mapToDto(Category category) {
        CategoryNodeDto dto = new CategoryNodeDto();
        dto.setName(category.getName());
        dto.setSeoSlug(category.getSeoSlug());

        // The recursive magic happens here
        if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
            List<CategoryNodeDto> children = category.getSubCategories().stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
            dto.setSubCategories(children);
        }

        return dto;
    }
}