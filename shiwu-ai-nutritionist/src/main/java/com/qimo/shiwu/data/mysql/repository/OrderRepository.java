package com.qimo.shiwu.data.mysql.repository;

import com.qimo.shiwu.data.mysql.entity.*;
import com.qimo.shiwu.data.mysql.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {}
