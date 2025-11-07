package com.delivery.repository;

import com.delivery.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    @Query("SELECT w FROM Warehouse w WHERE w.openingHours IS NOT NULL")
    List<Warehouse> findActiveWarehouses();

    Warehouse findByName(String name);
}