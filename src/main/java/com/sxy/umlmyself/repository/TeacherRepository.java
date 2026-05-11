package com.sxy.umlmyself.repository;

import com.sxy.umlmyself.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Integer> {
    Optional<Teacher> findByTeaId(Integer teaId);
}

