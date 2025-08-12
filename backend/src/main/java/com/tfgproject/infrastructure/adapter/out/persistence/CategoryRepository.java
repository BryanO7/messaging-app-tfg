package com.tfgproject.infrastructure.adapter.out.persistence;

import com.tfgproject.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    List<Category> findByParentIsNull(); // Categorías raíz

    List<Category> findByParentId(Long parentId); // Subcategorías

    List<Category> findByNameContainingIgnoreCase(String name);

    @Query("SELECT c FROM Category c WHERE c.parent IS NULL ORDER BY c.name")
    List<Category> findRootCategoriesOrderByName();

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.contacts WHERE c.id = :id")
    Optional<Category> findByIdWithContacts(@Param("id") Long id);

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.subcategories WHERE c.id = :id")
    Optional<Category> findByIdWithSubcategories(@Param("id") Long id);

    @Query("SELECT COUNT(cont) FROM Category c JOIN c.contacts cont WHERE c.id = :categoryId")
    Long countContactsByCategoryId(@Param("categoryId") Long categoryId);

    boolean existsByName(String name);

    boolean existsByNameAndParentId(String name, Long parentId);
}