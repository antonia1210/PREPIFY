package com.anto.backend.repository;

import com.anto.backend.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Integer> {

    List<Recipe> findByUserId(Integer userId);

    List<Recipe> findByCategory(String category);

    @Query("SELECT r FROM Recipe r WHERE " +
            "LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(r.category) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Recipe> search(String query);

    @Query("SELECT r.category, COUNT(r) FROM Recipe r GROUP BY r.category")
    List<Object[]> countByCategory();

    long countByUserId(Integer userId);
}