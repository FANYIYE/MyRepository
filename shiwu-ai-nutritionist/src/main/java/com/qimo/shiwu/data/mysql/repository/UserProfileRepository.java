package com.qimo.shiwu.data.mysql.repository;

import com.qimo.shiwu.data.mysql.entity.*;
import com.qimo.shiwu.data.mysql.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {}
