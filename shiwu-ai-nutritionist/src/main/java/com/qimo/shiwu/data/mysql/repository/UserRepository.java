package com.qimo.shiwu.data.mysql.repository;

import com.qimo.shiwu.data.mysql.entity.*;
import com.qimo.shiwu.data.mysql.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 衍生查询 (Derived Query):
     * Spring Data JPA 会自动解析方法名并生成 SQL: "SELECT * FROM User WHERE user_name = ?"
     * @param userName 用户名
     * @return 包含用户的 Optional，可能为空
     */
    Optional<User> findByUserName(String userName);

    /**
     * 获取完整的用户聚合信息。
     * 使用 JOIN FETCH 一次性加载所有关联的 OneToOne 实体，避免 N+1 查询。
     */
    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.userProfile " +
            "LEFT JOIN FETCH u.dietPreference " +
            "LEFT JOIN FETCH u.healthGoal " +
            "WHERE u.userId = :userId")
    Optional<User> findByIdWithDetails(Long userId);
}
