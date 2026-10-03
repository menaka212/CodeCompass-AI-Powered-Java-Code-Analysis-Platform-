package com.codecompass.backend.service;

import com.codecompass.backend.dto.CreateProjectRequest;
import com.codecompass.backend.dto.UpdateProjectRequest;
import com.codecompass.backend.entity.Project;
import com.codecompass.backend.entity.User;
import com.codecompass.backend.repository.ProjectRepository;
import com.codecompass.backend.repository.UserRepository;
import java.util.List;
import com.codecompass.backend.exception.ProjectNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            UserRepository userRepository
    ) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public Project createProject(
            Long userId,
            CreateProjectRequest request
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        Project project = new Project();

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setSourceType(request.getSourceType());
        project.setGithubUrl(request.getGithubUrl());
        project.setCreatedAt(LocalDateTime.now());

        // Connect this project to the currently logged-in user
        project.setUser(user);

        return projectRepository.save(project);
    }
    public List<Project> getMyProjects(Long userId) {

    User user = userRepository.findById(userId)
            .orElseThrow(() ->
                    new RuntimeException("User not found")
            );

    return projectRepository.findByUser(user);
}
public Project getProjectById(Long userId, Long projectId) {

    Project project = projectRepository.findById(projectId)
            .orElseThrow(() ->
                    new ProjectNotFoundException("Project not found")
            );

    // Check whether this project belongs to the logged-in user
    if (!project.getUser().getId().equals(userId)) {
        throw new AccessDeniedException("You do not have permission to access this project");
    }

    return project;
}
public Project updateProject(
        Long userId,
        Long projectId,
        UpdateProjectRequest request
) {

    Project project = projectRepository.findById(projectId)
            .orElseThrow(() ->
                    new ProjectNotFoundException("Project not found")
            );

    // Check whether the logged-in user owns this project
    if (!project.getUser().getId().equals(userId)) {
        throw new AccessDeniedException(
                "You do not have permission to update this project"
        );
    }

    // Update project details
    project.setName(request.getName());
    project.setDescription(request.getDescription());
    project.setSourceType(request.getSourceType());
    project.setGithubUrl(request.getGithubUrl());

    return projectRepository.save(project);
}

public void deleteProject(Long userId, Long projectId) {

    Project project = projectRepository.findById(projectId)
            .orElseThrow(() ->
                    new ProjectNotFoundException("Project not found")
            );

    // Check whether the logged-in user owns this project
    if (!project.getUser().getId().equals(userId)) {
        throw new AccessDeniedException(
                "You do not have permission to delete this project"
        );
    }

    projectRepository.delete(project);
}
}