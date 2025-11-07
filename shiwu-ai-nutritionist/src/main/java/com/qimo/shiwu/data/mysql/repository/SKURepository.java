package com.qimo.shiwu.data.mysql.repository;

import com.qimo.shiwu.data.mysql.entity.*;
import com.qimo.shiwu.data.mysql.entity.SKU;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SKURepository extends JpaRepository<SKU, Long> {

    @Query("SELECT s FROM SKU s " +
            "JOIN FETCH s.product p " +
            "JOIN FETCH s.inventory i " +
            "WHERE s.skuStatus = true")
    List<SKU> findAllActiveSKUsForSync();

    @Query("SELECT s FROM SKU s " +
            "JOIN FETCH s.product p " +
            "JOIN FETCH s.inventory i " +
            "WHERE s.skuId = :skuId AND s.skuStatus = true")
    Optional<SKU> findActiveSkuByIdForSync(Long skuId);
}
