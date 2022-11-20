package com.tchat.ms_authentification.dao;

import com.tchat.ms_authentification.bean.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
public interface UserDao extends JpaRepository<User, Long> {

    User findByUsername(String username);
    User findByUsernameOrEmail(String username, String email);
    User findByEmail(String email);
    void deleteByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    @Modifying
    @Transactional
    @Query("UPDATE User u set u.isLocked = ?2 where u.email = ?1")
    void updateLocking(String email, boolean lockedOrNot);

}
