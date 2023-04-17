package com.hjy.example.UserGradeStudy;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;

public interface UserRepository extends JpaRepository<User, Long> {
    Collection<User> findAllByUpdatedDate(LocalDate updateddate);
}
