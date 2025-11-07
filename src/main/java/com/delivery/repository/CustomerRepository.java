package com.delivery.repository;

import com.delivery.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByNameContainingIgnoreCase(String name);

    @Query("SELECT c FROM Customer c WHERE c.address LIKE %:address%")
    List<Customer> findByAddressContaining(@Param("address") String address);

    Customer findByName(String name);
}