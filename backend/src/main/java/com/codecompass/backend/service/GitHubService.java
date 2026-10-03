package com.codecompass.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import com.codecompass.backend.dto.GitHubRepositoryResponse;
import java.util.Map;
import com.codecompass.backend.dto.GitHubFileResponse;
import com.codecompass.backend.dto.GitHubFileContentResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
@Service
public class GitHubService {
  
    private final RestClient restClient;
    @Value("${github.token}")
    private String githubToken;
    public GitHubService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public String[] extractOwnerAndRepo(String githubUrl) {

        String cleanedUrl = githubUrl.replaceAll("/+$", "");

        String[] parts = cleanedUrl.split("/");

        String owner = parts[3];
        String repo = parts[4];

        return new String[]{owner, repo};
    }

   public GitHubRepositoryResponse getRepositoryDetails(String githubUrl) {

    String[] ownerAndRepo = extractOwnerAndRepo(githubUrl);

    String owner = ownerAndRepo[0];
    String repo = ownerAndRepo[1];

    String apiUrl =
            "https://api.github.com/repos/" + owner + "/" + repo;

    Map<String, Object> response = restClient.get()
            .uri(apiUrl)
            .header("Accept", "application/vnd.github+json")
            .header("Authorization", "Bearer " + githubToken)
            .retrieve()
            .body(Map.class);

    return new GitHubRepositoryResponse(
            (String) response.get("name"),
            (String) response.get("description"),
            (String) response.get("language"),
            (String) response.get("default_branch"),
            (String) response.get("html_url")
    );
}

public List<GitHubFileResponse> getRepositoryContents(String githubUrl) {

    String[] ownerAndRepo = extractOwnerAndRepo(githubUrl);

    String owner = ownerAndRepo[0];
    String repo = ownerAndRepo[1];

    String apiUrl =
            "https://api.github.com/repos/" + owner + "/" + repo + "/contents";

    List<Map<String, Object>> response = restClient.get()
            .uri(apiUrl)
            .header("Accept", "application/vnd.github+json")
            .header("Authorization", "Bearer " + githubToken)
            .retrieve()
            .body(List.class);

    List<GitHubFileResponse> files = new ArrayList<>();

    for (Map<String, Object> item : response) {

        files.add(
                new GitHubFileResponse(
                        (String) item.get("name"),
                        (String) item.get("path"),
                        (String) item.get("type"),
                        (String) item.get("download_url")
                )
        );
    }

    return files;
}
public List<GitHubFileResponse> getContentsRecursively(
        String owner,
        String repo,
        String path
) {

    String apiUrl =
            "https://api.github.com/repos/"
                    + owner + "/"
                    + repo + "/contents";

    if (path != null && !path.isBlank()) {
        apiUrl += "/" + path;
    }

    try {

        List<Map<String, Object>> response = restClient.get()
                .uri(apiUrl)
                .header("Accept", "application/vnd.github+json")
                .header("Authorization", "Bearer " + githubToken)
                .retrieve()
                .body(List.class);

        List<GitHubFileResponse> allFiles =
                new ArrayList<>();

        if (response == null) {
            return allFiles;
        }

        for (Map<String, Object> item : response) {

            String name = (String) item.get("name");
            String itemPath = (String) item.get("path");
            String type = (String) item.get("type");
            String downloadUrl =
                    (String) item.get("download_url");

            GitHubFileResponse fileResponse =
                    new GitHubFileResponse(
                            name,
                            itemPath,
                            type,
                            downloadUrl
                    );

            allFiles.add(fileResponse);

            if ("dir".equals(type)) {

                try {

                    List<GitHubFileResponse> nestedFiles =
                            getContentsRecursively(
                                    owner,
                                    repo,
                                    itemPath
                            );

                    allFiles.addAll(nestedFiles);

                } catch (Exception e) {

                    System.out.println(
                            "Skipping inaccessible path: "
                                    + itemPath
                    );
                }
            }
        }

        return allFiles;

    } catch (Exception e) {

        System.out.println(
                "Failed to fetch path: " + path
        );

        return new ArrayList<>();
    }
}
public List<GitHubFileResponse> getCompleteRepositoryStructure(
        String githubUrl
) {

    String[] ownerAndRepo = extractOwnerAndRepo(githubUrl);

    String owner = ownerAndRepo[0];
    String repo = ownerAndRepo[1];

    return getContentsRecursively(
            owner,
            repo,
            ""
    );
}
public List<GitHubFileResponse> filterRelevantFiles(
        List<GitHubFileResponse> files
) {

    List<String> allowedExtensions = List.of(
            ".java",
            ".js",
            ".ts",
            ".jsx",
            ".tsx",
            ".py",
            ".html",
            ".css",
            ".json",
            ".xml",
            ".md",
            ".properties",
            ".yml",
            ".yaml"
    );

    List<String> ignoredDirectories = List.of(
            "node_modules",
            "target",
            ".git",
            "build",
            "dist",
            ".idea"
    );

    return files.stream()
            // Keep only actual files, not directories
            .filter(file -> "file".equals(file.getType()))

            // Ignore files inside unnecessary directories
            .filter(file ->
                    ignoredDirectories.stream()
                            .noneMatch(dir ->
                                    file.getPath().contains("/" + dir + "/")
                                    || file.getPath().startsWith(dir + "/")
                            )
            )

            // Keep only useful file types
            .filter(file ->
                    allowedExtensions.stream()
                            .anyMatch(extension ->
                                    file.getName().endsWith(extension)
                            )
            )

            .toList();
}
public List<GitHubFileResponse> getRelevantRepositoryFiles(
        String githubUrl
) {

    List<GitHubFileResponse> allFiles =
            getCompleteRepositoryStructure(githubUrl);

    return filterRelevantFiles(allFiles);
}
public String getFileContent(String downloadUrl) {

    try {
        return restClient.get()
                .uri(downloadUrl)
                .header("Accept", "text/plain")
                .header("Authorization", "Bearer " + githubToken)
                .retrieve()
                .body(String.class);

    } catch (Exception e) {
        throw new RuntimeException(
                "Failed to fetch file content: " + e.getMessage(),
                e
        );
    }
}
public GitHubFileContentResponse getFileWithContent(
        GitHubFileResponse file
) {

    String content = getFileContent(file.getDownloadUrl());

    return new GitHubFileContentResponse(
            file.getName(),
            file.getPath(),
            content
    );
}
public GitHubFileContentResponse getFileContentByPath(
        String githubUrl,
        String filePath
) {

    String[] ownerAndRepo = extractOwnerAndRepo(githubUrl);

    String owner = ownerAndRepo[0];
    String repo = ownerAndRepo[1];

    String apiUrl =
            "https://api.github.com/repos/"
                    + owner + "/"
                    + repo + "/contents/"
                    + filePath;

    Map<String, Object> response = restClient.get()
            .uri(apiUrl)
            .header("Accept", "application/vnd.github+json")
            .header("Authorization", "Bearer " + githubToken)
            .retrieve()
            .body(Map.class);

    String encodedContent = (String) response.get("content");

    String content = new String(
            java.util.Base64.getDecoder().decode(
                    encodedContent.replaceAll("\\s", "")
            )
    );

    return new GitHubFileContentResponse(
            (String) response.get("name"),
            (String) response.get("path"),
            content
    );
}
public List<GitHubFileContentResponse> getAllJavaFilesWithContent(
        String githubUrl
) {

    List<GitHubFileResponse> files =
            getCompleteRepositoryStructure(githubUrl);

    return files.stream()

            // Only actual files
            .filter(file -> "file".equals(file.getType()))

            // Only Java files
            .filter(file ->
                    file.getName().endsWith(".java")
            )

            // Only application source code
            .filter(file ->
                    isApplicationJavaFile(file.getPath())
            )

            .map(file ->
                    getFileContentByPath(
                            githubUrl,
                            file.getPath()
                    )
            )

            .toList();
}
private boolean isApplicationJavaFile(String path) {

    String normalizedPath =
            path.replace("\\", "/");

    // Ignore test source files
    if (normalizedPath.contains("/src/test/")
            || normalizedPath.startsWith("src/test/")) {

        return false;
    }

    // Ignore Maven wrapper
    if (normalizedPath.contains(".mvn/")
            || normalizedPath.contains("wrapper/")) {

        return false;
    }

    // Ignore build output
    if (normalizedPath.contains("/target/")
            || normalizedPath.startsWith("target/")) {

        return false;
    }

    // Ignore Gradle build output
    if (normalizedPath.contains("/build/")
            || normalizedPath.startsWith("build/")) {

        return false;
    }

    // Ignore hidden Git files
    if (normalizedPath.contains("/.git/")
            || normalizedPath.startsWith(".git/")) {

        return false;
    }

    // Prefer main application source
    return normalizedPath.contains("/src/main/java/")
            || normalizedPath.startsWith("src/main/java/");
}
public List<GitHubFileResponse> getAllJavaFiles(
        String githubUrl
) {

    List<GitHubFileResponse> files =
            getCompleteRepositoryStructure(githubUrl);

    return files.stream()

            .filter(file ->
                    "file".equals(file.getType())
            )

            .filter(file ->
                    file.getName().endsWith(".java")
            )

            .filter(file ->
                    isApplicationJavaFile(
                            file.getPath()
                    )
            )

            .toList();
}
}