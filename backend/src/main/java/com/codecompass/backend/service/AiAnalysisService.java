package com.codecompass.backend.service;

import com.codecompass.backend.dto.GitHubFileContentResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiAnalysisService {

    private final RestTemplate restTemplate;
    private final GitHubService gitHubService;

    @Value("${ollama.base-url}")
    private String ollamaBaseUrl;

    @Value("${ollama.model}")
    private String model;

    /*
     * Keep the repository context reasonably small because
     * Ollama is running locally.
     */
    private static final int MAX_CONTEXT_CHARS = 6500;

    /*
     * Avoid sending extremely large individual files.
     */
    private static final int MAX_FILE_CHARS = 3500;
    private static final int MAX_METHOD_CHARS = 2500;

    public AiAnalysisService(GitHubService gitHubService) {
        this.restTemplate = new RestTemplate();
        this.gitHubService = gitHubService;
    }

    // ============================================================
    // GENERAL AI CHAT
    // ============================================================

    public String askAi(String prompt) {
        return generateAiResponse(prompt);
    }

    // ============================================================
    // REPOSITORY-AWARE AI CHAT
    // ============================================================

    public String askRepositoryAi(
            String githubUrl,
            String question
    ) {

        if (githubUrl == null || githubUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "GitHub repository URL is required."
            );
        }

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "Question is required."
            );
        }

        // --------------------------------------------------------
        // 1. Fetch repository Java files
        // --------------------------------------------------------

        List<GitHubFileContentResponse> javaFiles =
                gitHubService.getAllJavaFilesWithContent(githubUrl);

        if (javaFiles == null || javaFiles.isEmpty()) {
            return "No Java application source files were found "
                    + "in this repository.";
        }

        // --------------------------------------------------------
        // 2. Remove invalid files
        // --------------------------------------------------------

        List<GitHubFileContentResponse> validFiles =
                javaFiles.stream()
                        .filter(Objects::nonNull)
                        .filter(file ->
                                file.getContent() != null
                                        && !file.getContent().isBlank()
                        )
                        .collect(Collectors.toList());

        if (validFiles.isEmpty()) {
            return "No readable Java source files were found "
                    + "in this repository.";
        }

        // --------------------------------------------------------
        // 3. Select files based on the question
        // --------------------------------------------------------

        List<GitHubFileContentResponse> selectedFiles =
                selectRelevantFiles(validFiles, question);

        // --------------------------------------------------------
        // 4. Build context
        // --------------------------------------------------------

        StringBuilder context = new StringBuilder();

        List<String> includedFiles = new ArrayList<>();
for (GitHubFileContentResponse file : selectedFiles) {

    String path = getFilePath(file);

    String content = file.getContent();

    if (content == null || content.isBlank()) {
        continue;
    }

    String contentToInclude = content;

    /*
     * If the complete file is small enough,
     * include the complete source.
     */
    if (content.length() > MAX_FILE_CHARS) {

        /*
         * For flow questions, try to extract only
         * the relevant method instead of throwing
         * the entire file away.
         */
        if (isFlowQuestion(question.toLowerCase(Locale.ROOT))) {

            contentToInclude =
                    extractRelevantMethods(
                            content,
                            question
                    );

            if (contentToInclude == null
                    || contentToInclude.isBlank()) {

                continue;
            }
        } else {
            continue;
        }
    }

    String fileSection =
            "\n\n--- FILE: " + path + " ---\n"
                    + contentToInclude;

    if (context.length() + fileSection.length()
            > MAX_CONTEXT_CHARS) {
        continue;
    }

    context.append(fileSection);
    includedFiles.add(path);
}

        if (context.isEmpty()) {
            return "No readable Java source files fit within "
                    + "the current AI context limit.";
        }

        // --------------------------------------------------------
        // 5. Build strict repository-aware prompt
        // --------------------------------------------------------

        String prompt = """
                You are CodeCompass, a repository-aware coding assistant.

                Your job is to explain the actual source code of the
                repository provided below.

                IMPORTANT:
                You must use ONLY the source code included in this prompt.

                STRICT RULES:

                1. Do not invent classes, interfaces, methods,
                   variables, endpoints, annotations, configurations,
                   database operations, or file paths.

                2. Do not assume code exists just because it is common
                   in Spring Boot, Java, JWT, REST APIs, or databases.

                3. Every repository-specific statement must be supported
                   by the supplied source code.

                4. When explaining a flow, follow the actual method calls
                   visible in the source.

                5. If a controller calls a service method, identify the
                   service method only if it exists in the supplied files.

                6. If an interface method is implemented by a class,
                   identify the implementation only if that class is
                   present in the supplied source.

                7. If a repository is used, explain the repository call
                   only when the actual call is visible in the supplied
                   implementation.

                8. Do not create hypothetical code to fill missing
                   information.

                9. If the supplied source does not contain enough
                   information to answer part of the question, explicitly
                   say:
                   "This cannot be verified from the supplied source."

                10. Do not use generic programming knowledge as a
                    replacement for missing repository code.

                11. Mention exact file paths and class/method names
                    whenever they are available.

                12. If you find a contradiction between the question
                    and the source code, follow the source code.

                FILES INCLUDED IN THIS CONTEXT:
                %s

                REPOSITORY SOURCE CODE:
                %s

                USER QUESTION:
                %s

                Answer using the repository source above.
                Keep the explanation clear and concise.
                """.formatted(
                String.join("\n", includedFiles),
                context,
                question
        );

        // --------------------------------------------------------
        // 6. Debug information
        // --------------------------------------------------------

        System.out.println("========== AI DEBUG ==========");

System.out.println("Repository: " + githubUrl);
System.out.println("Question: " + question);

System.out.println("\n--- FETCHED FILES ---");
System.out.println("Java files fetched: " + javaFiles.size());

for (GitHubFileContentResponse file : javaFiles) {

    if (file == null) {
        continue;
    }

    String path = getFilePath(file);
    String content = file.getContent();

    int chars = content == null ? 0 : content.length();

    System.out.println(
            "Fetched: " + path
                    + " | chars=" + chars
    );
}

System.out.println("\n--- SELECTED FILES ---");
System.out.println("Files selected: " + selectedFiles.size());

for (GitHubFileContentResponse file : selectedFiles) {

    String path = getFilePath(file);
    String content = file.getContent();

    int chars = content == null ? 0 : content.length();

    System.out.println(
            "Selected: " + path
                    + " | chars=" + chars
    );
}

System.out.println("\n--- INCLUDED FILES ---");
System.out.println("Files included in context: "
        + includedFiles.size());

includedFiles.forEach(file ->
        System.out.println("Included: " + file)
);

System.out.println("\nContext characters: "
        + context.length());

System.out.println("================================");

        // --------------------------------------------------------
        // 7. Send to Ollama
        // --------------------------------------------------------

        return generateAiResponse(prompt);
    }

    // ============================================================
    // FILE SELECTION
    // ============================================================

    private List<GitHubFileContentResponse> selectRelevantFiles(
            List<GitHubFileContentResponse> files,
            String question
    ) {

        String questionLower =
                question.toLowerCase(Locale.ROOT);

        /*
         * First calculate a normal relevance score.
         */
        Map<GitHubFileContentResponse, Integer> scores =
                new HashMap<>();

        for (GitHubFileContentResponse file : files) {

            int score = getRelevanceScore(file, question);

            scores.put(file, score);
        }

        /*
         * Sort by relevance first.
         */
        List<GitHubFileContentResponse> ranked =
                files.stream()
                        .sorted(
                                Comparator.comparingInt(
                                        scores::get
                                ).reversed()
                        )
                        .collect(Collectors.toList());

        /*
         * Files explicitly identified as important for the question.
         */
        LinkedHashSet<GitHubFileContentResponse> selected =
                new LinkedHashSet<>();

        // --------------------------------------------------------
        // Security / JWT questions
        // --------------------------------------------------------

        if (isSecurityQuestion(questionLower)) {

            addFilesMatching(
                    selected,
                    files,
                    "security"
            );

            addFilesMatching(
                    selected,
                    files,
                    "jwt"
            );

            addFilesMatching(
                    selected,
                    files,
                    "auth"
            );

            addFilesMatching(
                    selected,
                    files,
                    "login"
            );

            addFilesMatching(
                    selected,
                    files,
                    "filter"
            );

            addFilesMatching(
                    selected,
                    files,
                    "user"
            );

            addFilesMatching(
                    selected,
                    files,
                    "config"
            );
        }

        // --------------------------------------------------------
        // CRUD / method-flow questions
        // --------------------------------------------------------

        if (isFlowQuestion(questionLower)) {

            /*
             * Controllers are usually the entry point.
             */
            addFilesMatching(
                    selected,
                    files,
                    "controller"
            );

            /*
             * Include service interfaces.
             */
            addFilesMatching(
                    selected,
                    files,
                    "service"
            );

            /*
             * Explicitly prioritize ServiceImpl.
             *
             * This is important because a question such as
             * "Trace getAllEmployees()" may mention only the
             * controller/service method, while the implementation
             * is in EmployeeServiceImpl.java.
             */
            addFilesMatching(
                    selected,
                    files,
                    "serviceimpl"
            );

            /*
             * Include repositories because they are often the
             * final database-access layer.
             */
            addFilesMatching(
                    selected,
                    files,
                    "repository"
            );

            /*
             * Entities help explain database/domain objects.
             */
            addFilesMatching(
                    selected,
                    files,
                    "entity"
            );

            /*
             * DTOs may be needed to explain request/response flow.
             */
            addFilesMatching(
                    selected,
                    files,
                    "dto"
            );
        }

        // --------------------------------------------------------
        // Explicit class names / method names from question
        // --------------------------------------------------------

        for (GitHubFileContentResponse file : ranked) {

            String path =
                    getFilePath(file).toLowerCase(Locale.ROOT);

            String name =
                    file.getName() == null
                            ? ""
                            : file.getName()
                            .toLowerCase(Locale.ROOT);

            String content =
                    file.getContent() == null
                            ? ""
                            : file.getContent()
                            .toLowerCase(Locale.ROOT);

            /*
             * Extract words from the question.
             */
            Set<String> questionWords =
                    Arrays.stream(
                                    questionLower.split(
                                            "[^a-zA-Z0-9]+"
                                    )
                            )
                            .filter(word -> word.length() >= 4)
                            .collect(Collectors.toSet());

            boolean matches = false;

            for (String word : questionWords) {

                if (name.contains(word)
                        || path.contains(word)
                        || content.contains(word)) {

                    matches = true;
                    break;
                }
            }

            if (matches) {
                selected.add(file);
            }
        }

        // --------------------------------------------------------
        // Add highest-ranked files as fallback
        // --------------------------------------------------------

        for (GitHubFileContentResponse file : ranked) {

            if (selected.size() >= 12) {
                break;
            }

            selected.add(file);
        }

        return new ArrayList<>(selected);
    }

    // ============================================================
    // FILE MATCHING
    // ============================================================
    // ============================================================
// RELEVANT METHOD EXTRACTION
// ============================================================

private String extractRelevantMethods(
        String content,
        String question
) {

    /*
     * Find method names mentioned in the question.
     *
     * Example:
     * "Trace the getAllEmployees() method"
     *
     * extracts:
     * getAllEmployees
     */
    Set<String> methodNames =
            Arrays.stream(
                            question.split(
                                    "[^a-zA-Z0-9_]+"
                            )
                    )
                    .filter(word ->
                            word.matches(
                                    "[a-zA-Z_$][a-zA-Z0-9_$]*"
                            )
                    )
                    .filter(word ->
                            word.length() >= 4
                    )
                    .collect(Collectors.toCollection(
                            LinkedHashSet::new
                    ));

    StringBuilder result =
            new StringBuilder();

    for (String methodName : methodNames) {

        int searchFrom = 0;

        while (searchFrom < content.length()) {

            /*
             * Look for the method name followed by "(".
             */
            String pattern =
                    methodName + "(";

            int methodIndex =
                    content.indexOf(
                            pattern,
                            searchFrom
                    );

            if (methodIndex == -1) {
                break;
            }

            /*
             * Make sure this is actually a method-like
             * occurrence and not just a normal variable/string.
             */
            int braceStart =
                    content.indexOf(
                            "{",
                            methodIndex
                    );

            if (braceStart == -1) {
                break;
            }

            /*
             * Find the matching closing brace.
             */
            int braceCount = 0;
            int endIndex = -1;

            for (
                    int i = braceStart;
                    i < content.length();
                    i++
            ) {

                char current =
                        content.charAt(i);

                if (current == '{') {
                    braceCount++;
                }

                if (current == '}') {
                    braceCount--;

                    if (braceCount == 0) {
                        endIndex = i + 1;
                        break;
                    }
                }
            }

            if (endIndex == -1) {
                break;
            }

            /*
             * Include annotations / method declaration
             * immediately before the method.
             */
            int startIndex =
                    findMethodStart(
                            content,
                            methodIndex
                    );

            String methodBlock =
                    content.substring(
                            startIndex,
                            endIndex
                    );

            /*
             * Avoid sending an excessively large method.
             */
            if (methodBlock.length()
                    <= MAX_METHOD_CHARS) {

                result.append(
                        "\n\n"
                                + methodBlock
                );
            }

            /*
             * Continue searching in case the method name
             * appears more than once.
             */
            searchFrom = endIndex;
        }
    }

    return result.toString().trim();
}
private int findMethodStart(
        String content,
        int methodIndex
) {

    /*
     * Search backwards for the beginning of the
     * method declaration.
     */
    int start = methodIndex;

    while (start > 0) {

        char current =
                content.charAt(start - 1);

        /*
         * Stop after reaching the previous method,
         * class declaration, or annotation block.
         */
        if (current == '}') {
            break;
        }

        start--;

        /*
         * Prevent accidentally taking a huge amount
         * of source code.
         */
        if (methodIndex - start > 800) {
            break;
        }
    }

    /*
     * Move forward past unnecessary whitespace.
     */
    while (
            start < methodIndex
                    && Character.isWhitespace(
                    content.charAt(start)
            )
    ) {
        start++;
    }

    return start;
}
    private void addFilesMatching(
            Set<GitHubFileContentResponse> selected,
            List<GitHubFileContentResponse> files,
            String keyword
    ) {

        String lowerKeyword =
                keyword.toLowerCase(Locale.ROOT);

        for (GitHubFileContentResponse file : files) {

            String path =
                    getFilePath(file)
                            .toLowerCase(Locale.ROOT);

            String name =
                    file.getName() == null
                            ? ""
                            : file.getName()
                            .toLowerCase(Locale.ROOT);

            if (name.contains(lowerKeyword)
                    || path.contains(lowerKeyword)) {

                selected.add(file);
            }
        }
    }

    // ============================================================
    // QUESTION TYPE DETECTION
    // ============================================================

    private boolean isSecurityQuestion(String question) {

        return question.contains("jwt")
                || question.contains("token")
                || question.contains("authentication")
                || question.contains("authorization")
                || question.contains("login")
                || question.contains("security")
                || question.contains("password")
                || question.contains("bearer");
    }

    private boolean isFlowQuestion(String question) {

        return question.contains("trace")
                || question.contains("flow")
                || question.contains("how does")
                || question.contains("how is")
                || question.contains("retrieve")
                || question.contains("save")
                || question.contains("create")
                || question.contains("update")
                || question.contains("delete")
                || question.contains("database")
                || question.contains("calls")
                || question.contains("method");
    }

    // ============================================================
    // RELEVANCE SCORING
    // ============================================================

    private int getRelevanceScore(
            GitHubFileContentResponse file,
            String question
    ) {

        String path =
                file.getPath() == null
                        ? ""
                        : file.getPath()
                        .toLowerCase(Locale.ROOT);

        String name =
                file.getName() == null
                        ? ""
                        : file.getName()
                        .toLowerCase(Locale.ROOT);

        String content =
                file.getContent() == null
                        ? ""
                        : file.getContent()
                        .toLowerCase(Locale.ROOT);

        String questionLower =
                question.toLowerCase(Locale.ROOT);

        int score = 0;

        // --------------------------------------------------------
        // Question word matching
        // --------------------------------------------------------

        Set<String> questionWords =
                Arrays.stream(
                                questionLower.split(
                                        "[^a-zA-Z0-9]+"
                                )
                        )
                        .filter(word -> word.length() >= 3)
                        .collect(Collectors.toSet());

        for (String word : questionWords) {

            if (name.contains(word)) {
                score += 15;
            }

            if (path.contains(word)) {
                score += 8;
            }

            if (content.contains(word)) {
                score += 2;
            }
        }

        // --------------------------------------------------------
        // Security relevance
        // --------------------------------------------------------

        if (isSecurityQuestion(questionLower)) {

            if (name.contains("security")
                    || name.contains("jwt")
                    || name.contains("auth")
                    || name.contains("login")
                    || name.contains("filter")) {

                score += 50;
            }

            if (content.contains("securityfilterchain")
                    || content.contains("jwt")
                    || content.contains("bearer")
                    || content.contains("passwordencoder")
                    || content.contains("authorization")) {

                score += 20;
            }
        }

        // --------------------------------------------------------
        // Flow relevance
        // --------------------------------------------------------

        if (isFlowQuestion(questionLower)) {

            if (name.contains("controller")) {
                score += 25;
            }

            if (name.equals("employeeservice.java")
                    || name.endsWith("service.java")) {

                score += 25;
            }

            if (name.contains("serviceimpl")) {
                score += 40;
            }

            if (name.contains("repository")) {
                score += 35;
            }

            if (name.contains("entity")) {
                score += 15;
            }

            if (name.contains("dto")) {
                score += 10;
            }
        }

        return score;
    }

    // ============================================================
    // FILE PATH
    // ============================================================

    private String getFilePath(
            GitHubFileContentResponse file
    ) {

        if (file.getPath() != null
                && !file.getPath().isBlank()) {

            return file.getPath();
        }

        if (file.getName() != null
                && !file.getName().isBlank()) {

            return file.getName();
        }

        return "unknown-file";
    }

    // ============================================================
    // OLLAMA
    // ============================================================

    private String generateAiResponse(String prompt) {

        String url =
                ollamaBaseUrl + "/api/generate";

        Map<String, Object> request =
                new HashMap<>();

        request.put("model", model);
        request.put("prompt", prompt);
        request.put("stream", false);

        Map<String, Object> options =
                new HashMap<>();

        /*
         * Keep this conservative for local Ollama.
         */
        options.put("num_ctx", 2048);
        options.put("num_predict", 400);
        options.put("temperature", 0.1);
        request.put("options", options);

        /*
         * Unload model after response to reduce RAM usage.
         */
        request.put("keep_alive", 0);

        try {

            Map<String, Object> response =
                    restTemplate.postForObject(
                            url,
                            request,
                            Map.class
                    );

            if (response == null
                    || response.get("response") == null) {

                return "No response received from AI.";
            }

            return response
                    .get("response")
                    .toString();

        } catch (Exception e) {

            return "Error communicating with Ollama: "
                    + e.getMessage();
        }
    }
}