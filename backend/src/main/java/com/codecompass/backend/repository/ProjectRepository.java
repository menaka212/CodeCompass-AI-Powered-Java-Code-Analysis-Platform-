package com.codecompass.backend.repository;

import com.codecompass.backend.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import com.codecompass.backend.entity.User;
import java.util.List;
public interface ProjectRepository extends JpaRepository<Project, Long> {
  
    List<Project> findByUser(User user);
}