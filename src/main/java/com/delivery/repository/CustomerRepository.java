package com.delivery.repository;

import com.delivery.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Méthodes dérivées
    List<Customer> findByNameContainingIgnoreCase(String name);
    List<Customer> findByAddressContaining(String address);
    List<Customer> findByPreferredTimeSlot(String timeSlot);

    // Recherche avancée avec combinaisons
    List<Customer> findByNameContainingAndAddressContaining(String name, String address);
    List<Customer> findByNameContainingAndPreferredTimeSlot(String name, String timeSlot);
    List<Customer> findByAddressContainingAndPreferredTimeSlot(String address, String timeSlot);
    List<Customer> findByNameContainingAndAddressContainingAndPreferredTimeSlot(
            String name, String address, String timeSlot);

    // Requêtes personnalisées avec @Query
    @Query("SELECT c FROM Customer c WHERE " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) AND " +
            "LOWER(c.address) LIKE LOWER(CONCAT('%', :address, '%'))")
    Page<Customer> searchByNameAndAddress(@Param("name") String name,
                                          @Param("address") String address,
                                          Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE " +
            "c.latitude BETWEEN :minLat AND :maxLat AND " +
            "c.longitude BETWEEN :minLon AND :maxLon")
    List<Customer> findByLocationRange(@Param("minLat") Double minLat,
                                       @Param("maxLat") Double maxLat,
                                       @Param("minLon") Double minLon,
                                       @Param("maxLon") Double maxLon);

    // Pagination native
    @Query(value = "SELECT * FROM customers c WHERE " +
            "c.name ILIKE %:searchTerm% OR " +
            "c.address ILIKE %:searchTerm%",
            countQuery = "SELECT count(*) FROM customers c WHERE " +
                    "c.name ILIKE %:searchTerm% OR " +
                    "c.address ILIKE %:searchTerm%",
            nativeQuery = true)
    Page<Customer> fullTextSearch(@Param("searchTerm") String searchTerm, Pageable pageable);
}