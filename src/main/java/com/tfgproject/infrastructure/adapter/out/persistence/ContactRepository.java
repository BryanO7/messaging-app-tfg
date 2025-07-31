package com.tfgproject.infrastructure.adapter.out.persistence;

import com.tfgproject.domain.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    Optional<Contact> findByEmail(String email);

    Optional<Contact> findByPhone(String phone);

    List<Contact> findByNameContainingIgnoreCase(String name);

    @Query("SELECT c FROM Contact c WHERE " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "c.phone LIKE CONCAT('%', :search, '%')")
    List<Contact> searchContacts(@Param("search") String search);

    @Query("SELECT c FROM Contact c JOIN c.categories cat WHERE cat.id = :categoryId")
    List<Contact> findByCategoryId(@Param("categoryId") Long categoryId);

    @Query("SELECT c FROM Contact c WHERE c.email IS NOT NULL AND c.email != ''")
    List<Contact> findContactsWithEmail();

    @Query("SELECT c FROM Contact c WHERE c.phone IS NOT NULL AND c.phone != ''")
    List<Contact> findContactsWithPhone();

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}