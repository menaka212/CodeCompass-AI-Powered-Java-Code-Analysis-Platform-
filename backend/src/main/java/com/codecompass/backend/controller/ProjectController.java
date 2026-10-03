package com.codecompass.backend.controller;

import com.codecompass.backend.dto.CreateProjectRequest;
import com.codecompass.backend.dto.FileRelationshipResponse;
import com.codecompass.backend.entity.Project;
import com.codecompass.backend.service.ProjectService;
import com.codecompass.backend.dto.GitHubFileContentResponse;
import jakarta.validation.Valid;
import com.codecompass.backend.dto.ProjectFileAnalysisResponse;
import com.codecompass.backend.dto.ImpactAnalysisResponse;
import com.codecompass.backend.dto.UpdateProjectRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.codecompass.backend.dto.ApiCallFlowResponse;
import com.codecompass.backend.dto.CallFlowNodeResponse;
import com.codecompass.backend.dto.CallFlowResponse;
import com.codecompass.backend.dto.MethodCallResponse;
import com.codecompass.backend.dto.DependencyGraphResponse;
import com.codecompass.backend.dto.ControllerApiResponse;
import com.codecompass.backend.dto.ApiEndpointResponse;
import com.codecompass.backend.dto.ArchitectureExplanationRequest;
import com.codecompass.backend.dto.ClassDependencyResponse;
import com.codecompass.backend.service.GitHubService;
import com.codecompass.backend.dto.GitHubFileResponse;
import com.codecompass.backend.dto.JavaFileAnalysisResponse;
import com.codecompass.backend.service.CodeAnalysisService;
import com.codecompass.backend.dto.GitHubRepositoryResponse;
import com.codecompass.backend.dto.ProjectArchitectureResponse;
@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    
    private final ProjectService projectService;
    private final GitHubService gitHubService;
    private final CodeAnalysisService codeAnalysisService;
    public ProjectController(ProjectService projectService, GitHubService gitHubService, CodeAnalysisService codeAnalysisService) {
        this.projectService = projectService;
        this.gitHubService = gitHubService;
        this.codeAnalysisService = codeAnalysisService;
    }

    @PostMapping
    public Project createProject(
            Authentication authentication,
            @Valid @RequestBody CreateProjectRequest request
    ) {

        Long userId = (Long) authentication.getPrincipal();

        return projectService.createProject(userId, request);
    }

    @GetMapping
    public List<Project> getMyProjects(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return projectService.getMyProjects(userId);
    }

    @GetMapping("/{projectId}")
    public Project getProjectById(
            @PathVariable Long projectId,
            Authentication authentication
    ) {

        Long userId = (Long) authentication.getPrincipal();

        return projectService.getProjectById(userId, projectId);
    }

    @PutMapping("/{projectId}")
    public Project updateProject(
            @PathVariable Long projectId,
            Authentication authentication,
            @Valid @RequestBody UpdateProjectRequest request
    ) {

        Long userId = (Long) authentication.getPrincipal();

        return projectService.updateProject(
                userId,
                projectId,
                request
        );
    }
    @DeleteMapping("/{projectId}")
    public void deleteProject(
            @PathVariable Long projectId,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        projectService.deleteProject(userId, projectId);
    }
    @GetMapping("/github")
    public GitHubRepositoryResponse getGitHubRepository(
            @RequestParam String url
    ) {
        return gitHubService.getRepositoryDetails(url);
    }
    @GetMapping("/github/contents")
    public List<GitHubFileResponse> getGitHubRepositoryContents(
            @RequestParam String url
    ) {
        return gitHubService.getRepositoryContents(url);
    }
    @GetMapping("/github/structure")
    public List<GitHubFileResponse> getCompleteRepositoryStructure(
            @RequestParam String url
    ) {
        return gitHubService.getCompleteRepositoryStructure(url);
    }
    @GetMapping("/github/files")
    public List<GitHubFileResponse> getRelevantGitHubFiles(
            @RequestParam String url
    ) {
        return gitHubService.getRelevantRepositoryFiles(url);
    }

    @GetMapping("/github/file-content")
    public String getGitHubFileContent(
            @RequestParam String url
    ) {
        return gitHubService.getFileContent(url);
    }
    @GetMapping("/github/file")
    public GitHubFileContentResponse getGitHubFileByPath(
            @RequestParam String url,
            @RequestParam String path
    ) {
        return gitHubService.getFileContentByPath(url, path);
    }
    @GetMapping("/github/analyze")
    public JavaFileAnalysisResponse analyzeGitHubFile(
            @RequestParam String url,
            @RequestParam String path
    ) {

        GitHubFileContentResponse file =
                gitHubService.getFileContentByPath(url, path);

        return codeAnalysisService.analyzeJavaFile(
                file.getContent()
        );
    }   
        @GetMapping("/github/api-call-flows")
        public List<ApiCallFlowResponse> analyzeApiCallFlows(
                @RequestParam String url
        ) {

        List<GitHubFileContentResponse> javaFiles =
                gitHubService.getAllJavaFilesWithContent(url);

        return codeAnalysisService
                .buildApiCallFlows(javaFiles);
        }
        @GetMapping("/github/call-flow-tree")
        public List<CallFlowNodeResponse> analyzeCallFlowTree(
                @RequestParam String url
        ) {

        List<GitHubFileContentResponse> javaFiles =
                gitHubService.getAllJavaFilesWithContent(url);

        List<MethodCallResponse> methodCalls =
        codeAnalysisService
                .analyzeRepositoryMethodCalls(javaFiles);

        return codeAnalysisService
                .buildCallFlowTree(
                        methodCalls,
                        javaFiles
                );
        }
        @GetMapping("/github/call-flow")
public List<CallFlowResponse> analyzeCallFlow(
        @RequestParam String url
) {

    List<GitHubFileContentResponse> javaFiles =
            gitHubService.getAllJavaFilesWithContent(url);

    List<MethodCallResponse> methodCalls =
            codeAnalysisService
                    .analyzeRepositoryMethodCalls(javaFiles);

    return codeAnalysisService
            .buildCallFlow(methodCalls);
}
        @GetMapping("/github/relationships")
        public List<FileRelationshipResponse> analyzeRepositoryRelationships(
                @RequestParam String url
        ) {

        List<GitHubFileContentResponse> javaFiles =
                gitHubService.getAllJavaFilesWithContent(url);

        return codeAnalysisService
                .analyzeRepositoryRelationships(javaFiles);
    }

        @GetMapping("/github/architecture")
        public ProjectArchitectureResponse analyzeProjectArchitecture(
                @RequestParam String url
        ) {

        List<GitHubFileContentResponse> javaFiles =
                gitHubService.getAllJavaFilesWithContent(url);

        return codeAnalysisService
                .analyzeProjectArchitecture(javaFiles);
        }
        @GetMapping("/github/api-endpoints")
        public List<ApiEndpointResponse> analyzeApiEndpoints(
                @RequestParam String url,
                @RequestParam String path
        ) {

        GitHubFileContentResponse file =
                gitHubService.getFileContentByPath(url, path);

        return codeAnalysisService.extractApiEndpoints(
                file.getContent()
        );
        }
        @GetMapping("/github/api-map")
        public List<ControllerApiResponse> analyzeRepositoryApiMap(
                @RequestParam String url
        ) {

        List<GitHubFileContentResponse> javaFiles =
                gitHubService.getAllJavaFilesWithContent(url);

        return codeAnalysisService
                .analyzeRepositoryApiEndpoints(javaFiles);
        }
        @GetMapping("/github/dependencies")
public List<ClassDependencyResponse> analyzeDependencies(
        @RequestParam String url
) {

    List<GitHubFileContentResponse> javaFiles =
            gitHubService.getAllJavaFilesWithContent(url);

    return codeAnalysisService
            .analyzeRepositoryDependencies(javaFiles);
}
        @GetMapping("/github/dependency-graph")
public List<DependencyGraphResponse> getDependencyGraph(
        @RequestParam String url
) {

    List<GitHubFileContentResponse> javaFiles =
            gitHubService.getAllJavaFilesWithContent(url);

    List<ClassDependencyResponse> dependencies =
            codeAnalysisService
                    .analyzeRepositoryDependencies(javaFiles);

    return codeAnalysisService
            .buildDependencyGraph(dependencies);
}
        @GetMapping("/github/method-calls")
public List<MethodCallResponse> analyzeMethodCalls(
        @RequestParam String url
) {

    List<GitHubFileContentResponse> javaFiles =
            gitHubService.getAllJavaFilesWithContent(url);

    return codeAnalysisService
            .analyzeRepositoryMethodCalls(javaFiles);
}
        @GetMapping("/github/impact-analysis")
        public ImpactAnalysisResponse analyzeMethodImpact(
                @RequestParam String url,
                @RequestParam String targetClass,
                @RequestParam String targetMethod
        ) {

        return codeAnalysisService.analyzeMethodImpact(
                url,
                targetClass,
                targetMethod
        );
        }
        @PostMapping("/github/architecture/explain")
        public String explainProjectArchitecture(
                @Valid @RequestBody ArchitectureExplanationRequest request
        ) {

        return codeAnalysisService
                .generateArchitectureExplanation(
                        request.getGithubUrl()
                );
        }
        @GetMapping("/github/file-analysis")
        public List<ProjectFileAnalysisResponse> analyzeRepositoryFiles(
                @RequestParam String url
        ) {

        return codeAnalysisService
                .analyzeRepositoryFiles(url);
        }
        }