package com.java.scheduler.repository;

import com.java.scheduler.domain.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Integer> {

    // 1. Replaces manual HQL for finding available assets
	long countByStatus(Asset.Status status);

    // 2. If you need to find assets by category
    @Query("SELECT a FROM Asset a WHERE UPPER(a.category) = UPPER(:category)")
    List<Asset> findByCategoryIgnoreCase(String category);

    // 3. Custom query if you want to find assets by a partial name match
    @Query("SELECT a FROM Asset a WHERE a.name LIKE %:name%")
    List<Asset> searchByName(String name);
    
}