package com.codecompass.backend.service;

import com.codecompass.backend.dto.JavaFileAnalysisResponse;
import com.codecompass.backend.dto.ProjectArchitectureResponse;
import com.codecompass.backend.dto.ProjectFileAnalysisResponse;
import com.codecompass.backend.enums.DatabaseOperation;
import org.springframework.stereotype.Service;

import com.codecompass.backend.dto.ApiCallFlowResponse;
import com.codecompass.backend.dto.ApiEndpointResponse;
import com.codecompass.backend.dto.ApiParameterResponse;
import com.codecompass.backend.dto.ClassDependencyResponse;
import com.codecompass.backend.dto.ClassType;
import com.codecompass.backend.dto.DependencyGraphResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import com.codecompass.backend.dto.CallFlowNodeResponse;
import java.util.Map;
import java.util.Set;
import com.codecompass.backend.dto.CallFlowResponse;
import com.codecompass.backend.dto.MethodCallResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.codecompass.backend.dto.ControllerApiResponse;
import com.codecompass.backend.dto.FileRelationshipResponse;
import com.codecompass.backend.dto.GitHubFileContentResponse;
import com.codecompass.backend.dto.GitHubFileResponse;
import com.codecompass.backend.dto.ImpactAnalysisResponse;
import com.codecompass.backend.dto.ImpactCallerResponse;
@Service
public class CodeAnalysisService {
      
    private final GitHubService gitHubService;
private final OllamaService ollamaService;
    public CodeAnalysisService(GitHubService gitHubService, OllamaService ollamaService) {
    this.gitHubService = gitHubService;
    this.ollamaService = ollamaService;
}
    public JavaFileAnalysisResponse analyzeJavaFile(String content) {

        String packageName = extractPackageName(content);
        String className = extractClassName(content);
        List<String> imports = extractImports(content);
        List<String> methods = extractMethods(content);
        String componentType = extractComponentType(content);
        List<String> dependencies = extractDependencies(content);
        return new JavaFileAnalysisResponse(
                packageName,
                className,
                imports,
                methods,
                componentType,
                dependencies
        );
    }

    private String extractPackageName(
        String content
) {

    Pattern packagePattern =
            Pattern.compile(
                    "package\\s+([\\w.]+);"
            );

    Matcher matcher =
            packagePattern.matcher(content);

    if (matcher.find()) {

        return matcher.group(1);
    }

    return null;
}

    private String extractClassName(String content) {

    Pattern classPattern = Pattern.compile(
            "(?m)^\\s*"
                    + "(?:public\\s+|protected\\s+|private\\s+)?"
                    + "(?:abstract\\s+|final\\s+)?"
                    + "(?:static\\s+)?"
                    + "(?:class|interface|enum|record)\\s+"
                    + "(\\w+)"
    );

    Matcher matcher = classPattern.matcher(content);

    if (matcher.find()) {
        return matcher.group(1);
    }

    return null;
}

private List<String> extractImports(
        String content
) {

    List<String> imports =
            new ArrayList<>();

    Pattern importPattern =
            Pattern.compile(
                    "import\\s+([\\w.]+)\\s*;"
            );

    Matcher matcher =
            importPattern.matcher(content);

    while (matcher.find()) {

        String fullClassName =
                matcher.group(1);

        imports.add(fullClassName);
    }

    return imports;
}
    private List<String> extractMethods(String content) {

        List<String> methods = new ArrayList<>();

        Pattern pattern = Pattern.compile(
                "(public|private|protected)\\s+" +
                "(static\\s+)?" +
                "[\\w<>\\[\\]]+\\s+" +
                "(\\w+)\\s*\\("
        );

        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            methods.add(matcher.group(3));
        }

        return methods;
    }
    
private String extractComponentType(String content) {

    // Spring Boot Application
    if (content.matches(
            "(?s).*@SpringBootApplication\\b.*")) {

        return "APPLICATION";
    }

    // REST Controller
    if (content.matches(
            "(?s).*@RestController\\b.*")) {

        return "REST_CONTROLLER";
    }

    // Normal Spring Controller
    if (content.matches(
            "(?s).*@Controller\\b.*")) {

        return "CONTROLLER";
    }

    // Exception Handlers
    if (content.matches(
            "(?s).*@(ControllerAdvice|RestControllerAdvice)\\b.*")) {

        return "EXCEPTION_HANDLER";
    }

    // Spring Service
    if (content.matches(
            "(?s).*@Service\\b.*")) {

        return "SERVICE";
    }
    if (content.contains("@Repository")) {
                return "REPOSITORY";
                }
    // Spring Repository
    if (content.matches(
            "(?s).*@Repository\\b.*")) {

        return "REPOSITORY";
    }

    // Spring Data Repository
    if (content.matches(
            "(?s).*\\bextends\\s+.*\\bJpaRepository\\b.*")
            || content.matches(
            "(?s).*\\bextends\\s+.*\\bCrudRepository\\b.*")
            || content.matches(
            "(?s).*\\bextends\\s+.*\\bPagingAndSortingRepository\\b.*")
            || content.matches(
            "(?s).*\\bextends\\s+.*\\bRepository\\b.*")) {

        return "REPOSITORY";
    }

    // JPA Entity
    if (content.matches(
            "(?s).*@Entity\\b.*")) {

        return "ENTITY";
    }

    // Spring Configuration
    if (content.matches(
            "(?s).*@Configuration\\b.*")) {

        return "CONFIGURATION";
    }

    // Generic Spring Component
    if (content.matches(
            "(?s).*@Component\\b.*")) {

        return "COMPONENT";
    }

    return "NORMAL_CLASS";
}
private List<String> extractDependencies(
        String content
) {

    Set<String> dependencies =
            new LinkedHashSet<>();

    String className =
            extractClassName(content);

    /*
     * =====================================
     * 1. DETECT IMPORT DEPENDENCIES
     * =====================================
     */

    Pattern importPattern =
            Pattern.compile(
                    "import\\s+([\\w.]+);"
            );

    Matcher importMatcher =
            importPattern.matcher(content);

    while (importMatcher.find()) {

        String fullImport =
                importMatcher.group(1);

        String[] parts =
                fullImport.split("\\.");

        String importedClass =
                parts[parts.length - 1];

        /*
         * Ignore wildcard imports
         */

        if (!importedClass.equals("*")) {

            dependencies.add(importedClass);
        }
    }


    /*
     * =====================================
     * 2. DETECT FIELD DEPENDENCIES
     * =====================================
     *
     * Example:
     *
     * private final OwnerRepository owners;
     *
     * private ProductService productService;
     */

    Pattern fieldPattern =
            Pattern.compile(

                    "(?:private|protected|public)?\\s*"
                            + "(?:static\\s+)?"
                            + "(?:final\\s+)?"
                            + "([A-Z]\\w*(?:<[^;>]+>)?)\\s+"
                            + "(\\w+)\\s*;"

            );


    Matcher fieldMatcher =
            fieldPattern.matcher(content);


    while (fieldMatcher.find()) {

        String dependencyType =
                fieldMatcher.group(1);


        dependencyType =
                dependencyType.replaceAll(
                        "<.*>",
                        ""
                );


        if (!dependencyType.equals(className)
                && !isPrimitiveType(dependencyType)) {

            dependencies.add(
                    dependencyType
            );
        }
    }


    /*
     * =====================================
     * 3. DETECT CONSTRUCTOR DEPENDENCIES
     * =====================================
     *
     * Example:
     *
     * OwnerController(
     *     OwnerRepository owners
     * )
     */

    if (className != null
            && !className.isBlank()) {

        Pattern constructorPattern =
                Pattern.compile(

                        "(?:public|protected|private)?\\s*"
                                + Pattern.quote(className)
                                + "\\s*"
                                + "\\((.*?)\\)",

                        Pattern.DOTALL
                );


        Matcher constructorMatcher =
                constructorPattern.matcher(content);


        while (constructorMatcher.find()) {

            String parameters =
                    constructorMatcher.group(1);


            extractConstructorDependencies(
                    parameters,
                    dependencies
            );
        }
    }


    /*
     * =====================================
     * 4. DETECT IMPLEMENTS
     * =====================================
     */

    Pattern implementsPattern =
            Pattern.compile(
                    "implements\\s+([\\w\\s,]+)"
            );


    Matcher implementsMatcher =
            implementsPattern.matcher(content);


    while (implementsMatcher.find()) {

        String interfaces =
                implementsMatcher.group(1);


        String[] interfaceList =
                interfaces.split(",");


        for (String interfaceName
                : interfaceList) {

            interfaceName =
                    interfaceName.trim();


            if (!interfaceName.isBlank()) {

                dependencies.add(
                        interfaceName
                );
            }
        }
    }


    /*
     * =====================================
     * 5. REMOVE SELF DEPENDENCY
     * =====================================
     */

    dependencies.remove(className);


    return new ArrayList<>(
            dependencies
    );
}
private boolean isValidProjectDependency(
        String dependency,
        String sourceClass
) {
  
    if (dependency == null
            || dependency.isBlank()) {

        return false;
    }


    // Ignore self dependency
    if (dependency.equals(sourceClass)) {

        return false;
    }


    /*
     * Ignore primitive types
     */

    if (isPrimitiveType(dependency)) {

        return false;
    }


    /*
     * Ignore Java built-in
     * and common collection classes
     */

    Set<String> ignoredClasses = Set.of(

            "String",

            "Integer",
            "Long",
            "Double",
            "Float",
            "Boolean",
            "Character",
            "Byte",
            "Short",

            "Object",
            "Void",

            "List",
            "Set",
            "Map",
            "Collection",

            "Optional",

            "ArrayList",
            "HashSet",
            "HashMap",

            "LocalDate",
            "LocalDateTime",
            "HttpStatus"
    );


    if (ignoredClasses.contains(dependency)) {

        return false;
    }


    /*
     * Project classes normally
     * start with an uppercase letter
     */

    return Character.isUpperCase(
            dependency.charAt(0)
    );
}
private void extractConstructorDependencies(
        String parameters,
        Set<String> dependencies
) {

    if (parameters == null
            || parameters.isBlank()) {

        return;
    }


    String[] parameterList =
            parameters.split(",");


    for (String parameter
            : parameterList) {

        parameter =
                parameter.trim();


        if (parameter.isBlank()) {

            continue;
        }


        /*
         * Remove annotations
         *
         * Example:
         *
         * @Qualifier("ownerRepository")
         * OwnerRepository owners
         */

        parameter =
                parameter.replaceAll(
                        "@\\w+(?:\\([^)]*\\))?\\s*",
                        ""
                );


        /*
         * Remove final
         */

        parameter =
                parameter.replaceAll(
                        "\\bfinal\\b\\s*",
                        ""
                );


        String[] parts =
                parameter.trim()
                        .split("\\s+");


        /*
         * Expected:
         *
         * OwnerRepository owners
         */

        if (parts.length < 2) {

            continue;
        }


        String dependencyType =
                parts[parts.length - 2];


        /*
         * Remove generics
         */

        dependencyType =
                dependencyType.replaceAll(
                        "<.*?>",
                        ""
                );


        /*
         * Ignore primitive types
         */

        if (dependencyType.isBlank()
                || isPrimitiveType(
                        dependencyType
                )) {

            continue;
        }


        dependencies.add(
                dependencyType
        );
    }
}
private boolean isPrimitiveType(
        String type
) {

    return switch (type) {

        case "int",
             "long",
             "double",
             "float",
             "boolean",
             "char",
             "byte",
             "short" -> true;

        default -> false;
    };
}
public List<FileRelationshipResponse> analyzeRepositoryRelationships(
        List<GitHubFileContentResponse> files
) {

    List<JavaFileAnalysisResponse> analyses = new ArrayList<>();

    // Step 1: Analyze every Java file
    for (GitHubFileContentResponse file : files) {

        JavaFileAnalysisResponse analysis =
                analyzeJavaFile(file.getContent());

        analyses.add(analysis);
    }

    // Step 2: Collect all class names that exist in this repository
    List<String> projectClassNames = analyses.stream()
            .map(JavaFileAnalysisResponse::getClassName)
            .filter(className -> className != null)
            .toList();

    List<FileRelationshipResponse> relationships = new ArrayList<>();

    // Step 3: Keep only dependencies that belong to this project
    for (JavaFileAnalysisResponse analysis : analyses) {

        List<String> projectDependencies =
                analysis.getDependencies()
                        .stream()
                        .filter(projectClassNames::contains)
                        .toList();

        FileRelationshipResponse relationship =
                new FileRelationshipResponse(
                        analysis.getClassName(),
                        projectDependencies
                );

        relationships.add(relationship);
    }

    return relationships;
}


public ProjectArchitectureResponse analyzeProjectArchitecture(
        List<GitHubFileContentResponse> files
) {

    List<String> applications = new ArrayList<>();

    List<String> restControllers = new ArrayList<>();

    List<String> controllers = new ArrayList<>();

    List<String> services = new ArrayList<>();

    List<String> repositories = new ArrayList<>();

    List<String> entities = new ArrayList<>();

    List<String> configurations = new ArrayList<>();

    List<String> components = new ArrayList<>();

    List<String> exceptionHandlers = new ArrayList<>();

    List<String> otherClasses = new ArrayList<>();


    for (GitHubFileContentResponse file : files) {

        // Ignore test files
        if (file.getPath().contains("/test/")
                || file.getPath().startsWith("test/")) {

            continue;
        }


        JavaFileAnalysisResponse analysis =
                analyzeJavaFile(file.getContent());

        String className = analysis.getClassName();

        String componentType = analysis.getComponentType();


        if (className == null) {
            continue;
        }


        switch (componentType) {

            case "APPLICATION":

                applications.add(className);
                break;


            case "REST_CONTROLLER":

                restControllers.add(className);
                break;


            case "CONTROLLER":

                controllers.add(className);
                break;


            case "SERVICE":

                services.add(className);
                break;


            case "REPOSITORY":

                repositories.add(className);
                break;


            case "ENTITY":

                entities.add(className);
                break;


            case "CONFIGURATION":

                configurations.add(className);
                break;


            case "COMPONENT":

                components.add(className);
                break;


            case "EXCEPTION_HANDLER":

                exceptionHandlers.add(className);
                break;


            default:

                otherClasses.add(className);
        }
    }


    return new ProjectArchitectureResponse(

        new ArrayList<>(new LinkedHashSet<>(applications)),

        new ArrayList<>(new LinkedHashSet<>(restControllers)),

        new ArrayList<>(new LinkedHashSet<>(controllers)),

        new ArrayList<>(new LinkedHashSet<>(services)),

        new ArrayList<>(new LinkedHashSet<>(repositories)),

        new ArrayList<>(new LinkedHashSet<>(entities)),

        new ArrayList<>(new LinkedHashSet<>(configurations)),

        new ArrayList<>(new LinkedHashSet<>(components)),

        new ArrayList<>(new LinkedHashSet<>(exceptionHandlers)),

        new ArrayList<>(new LinkedHashSet<>(otherClasses))
);
}
private String extractParentClass(String content) {

    Pattern pattern = Pattern.compile(
            "\\b(?:class|interface)\\s+\\w+"
                    + "(?:\\s+extends\\s+([\\w<>]+))?"
    );

    Matcher matcher = pattern.matcher(content);

    if (matcher.find()) {

        String parentClass = matcher.group(1);

        if (parentClass != null) {

            // Remove generic types
            return parentClass.replaceAll("<.*>", "");
        }
    }

    return null;
}
public List<ApiEndpointResponse> extractApiEndpoints(String content) {

    List<ApiEndpointResponse> endpoints = new ArrayList<>();

    String basePath = extractBasePath(content);

    Pattern endpointPattern = Pattern.compile(
            "@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping)"
                    + "\\s*"
                    + "(?:\\((.*?)\\))?"
                    + "\\s*"
                    + "(?:@[\\w.]+(?:\\([^)]*\\))?\\s*)*"
                    + "(?:public|private|protected)?\\s*"
                    + "(?:static\\s+)?"
                    + "(?:final\\s+)?"
                    + "([\\w<>\\[\\], ?]+?)\\s+"
                    + "(\\w+)\\s*"
                    + "\\(([^)]*)\\)",
            Pattern.MULTILINE | Pattern.DOTALL
    );

    Matcher matcher = endpointPattern.matcher(content);

    while (matcher.find()) {

        // Group 1 → Mapping type
        String mappingType = matcher.group(1);

        // Group 2 → Mapping annotation content
        String annotationContent = matcher.group(2);

        // Group 3 → Return type
        String returnType = matcher.group(3).trim();

        // Group 4 → Method name
        String handler = matcher.group(4);

        // Group 5 → Method parameters
        String parametersContent = matcher.group(5);

        List<ApiParameterResponse> parameters =
                extractApiParameters(parametersContent);

        String endpointPath =
                extractPathFromAnnotation(annotationContent);

        String httpMethod = switch (mappingType) {
            case "GetMapping" -> "GET";
            case "PostMapping" -> "POST";
            case "PutMapping" -> "PUT";
            case "DeleteMapping" -> "DELETE";
            case "PatchMapping" -> "PATCH";
            default -> "UNKNOWN";
        };

        String fullPath =
                combinePaths(basePath, endpointPath);

        endpoints.add(
                new ApiEndpointResponse(
                        httpMethod,
                        fullPath,
                        handler,
                        returnType,
                        null,
                        parameters
                )
        );
    }

    List<ApiEndpointResponse> requestMappingEndpoints =
            extractRequestMappingEndpoints(
                    content,
                    basePath
            );

    endpoints.addAll(requestMappingEndpoints);

    return endpoints;
}
public List<ControllerApiResponse> analyzeRepositoryApiEndpoints(
        List<GitHubFileContentResponse> files
) {

    List<ControllerApiResponse> controllerApis =
            new ArrayList<>();

    for (GitHubFileContentResponse file : files) {

        String content =
                file.getContent();

        JavaFileAnalysisResponse analysis =
                analyzeJavaFile(content);

        /*
         * Only analyze controller classes
         */
        if (
                "REST_CONTROLLER".equals(
                        analysis.getComponentType()
                )
                ||
                "CONTROLLER".equals(
                        analysis.getComponentType()
                )
        ) {

            /*
             * Use fully qualified class key
             *
             * Example:
             *
             * com.example.controller.ProductController
             */
            String controllerClassKey =
                    buildClassKey(content);

            List<ApiEndpointResponse> endpoints =
                    extractApiEndpoints(content);

            controllerApis.add(

                    new ControllerApiResponse(

                            controllerClassKey,

                            endpoints
                    )
            );
        }
    }

    return controllerApis;
}
private String extractBasePath(String content) {

    Pattern pattern = Pattern.compile(
            "@RequestMapping\\s*\\(([^)]*)\\)"
    );

    Matcher matcher = pattern.matcher(content);

    if (matcher.find()) {

        return extractPathFromAnnotation(
                matcher.group(1)
        );
    }

    return "";
}
private String extractPathFromAnnotation(
        String annotationContent
) {

    if (annotationContent == null
            || annotationContent.isBlank()) {

        return "";
    }

    Pattern directPathPattern =
            Pattern.compile("^\\s*\"([^\"]*)\"\\s*$");

    Matcher directMatcher =
            directPathPattern.matcher(annotationContent);

    if (directMatcher.find()) {
        return directMatcher.group(1);
    }

    Pattern namedPathPattern = Pattern.compile(
            "(?:value|path)\\s*=\\s*\"([^\"]*)\""
    );

    Matcher namedMatcher =
            namedPathPattern.matcher(annotationContent);

    if (namedMatcher.find()) {
        return namedMatcher.group(1);
    }

    return "";
}
private String combinePaths(
        String basePath,
        String endpointPath
) {

    if (basePath == null) {
        basePath = "";
    }

    if (endpointPath == null) {
        endpointPath = "";
    }

    if (basePath.isEmpty() && endpointPath.isEmpty()) {
        return "/";
    }

    if (basePath.isEmpty()) {
        return endpointPath.startsWith("/")
                ? endpointPath
                : "/" + endpointPath;
    }

    if (endpointPath.isEmpty()) {
        return basePath.startsWith("/")
                ? basePath
                : "/" + basePath;
    }

    String cleanBase =
            basePath.endsWith("/")
                    ? basePath.substring(0, basePath.length() - 1)
                    : basePath;

    String cleanEndpoint =
            endpointPath.startsWith("/")
                    ? endpointPath
                    : "/" + endpointPath;

    return cleanBase + cleanEndpoint;
}
private List<ApiEndpointResponse> extractRequestMappingEndpoints(
        String content,
        String basePath
) {

    List<ApiEndpointResponse> endpoints = new ArrayList<>();

    Pattern pattern = Pattern.compile(
            "@RequestMapping\\s*\\((.*?)\\)"
                    + "\\s*"
                    + "(?:@ResponseStatus\\s*\\(([^)]*)\\)\\s*)?"
                    + "(?:public|private|protected)\\s+"
                    + "([\\w<>\\[\\], ?]+)\\s+"
                    + "(\\w+)\\s*"
                    + "\\(([^)]*)\\)",
            Pattern.MULTILINE | Pattern.DOTALL
    );

    Matcher matcher = pattern.matcher(content);

    while (matcher.find()) {

        String annotationContent = matcher.group(1);
        String responseStatusContent = matcher.group(2);
        String returnType = matcher.group(3).trim();
        String handler = matcher.group(4);
        String parametersContent = matcher.group(5);

        // Skip class-level @RequestMapping
        if (!annotationContent.contains("RequestMethod.")) {
            continue;
        }

        String endpointPath =
                extractPathFromAnnotation(annotationContent);

        String fullPath =
                combinePaths(basePath, endpointPath);

        List<ApiParameterResponse> parameters =
                extractApiParameters(parametersContent);
        String responseStatus =
                extractResponseStatus(responseStatusContent);

        Pattern methodPattern = Pattern.compile(
                "RequestMethod\\.(GET|POST|PUT|DELETE|PATCH)"
        );

        Matcher methodMatcher =
                methodPattern.matcher(annotationContent);

        while (methodMatcher.find()) {

            String httpMethod = methodMatcher.group(1);

            endpoints.add(
                    new ApiEndpointResponse(
                            httpMethod,
                            fullPath,
                            handler,
                            returnType,
                            responseStatus,
                            parameters
                    )
            );
        }
    }

    return endpoints;
}
private List<ApiParameterResponse> extractApiParameters(
        String parametersContent
) {

    List<ApiParameterResponse> parameters = new ArrayList<>();

    if (parametersContent == null || parametersContent.isBlank()) {
        return parameters;
    }

    Pattern parameterPattern = Pattern.compile(
            "@(PathVariable|RequestParam|RequestBody|RequestHeader)"
                    + "(?:\\s*\\([^)]*\\))?"
                    + "\\s+"
                    + "([\\w<>\\[\\]?]+)"
                    + "\\s+"
                    + "(\\w+)"
    );

    Matcher matcher = parameterPattern.matcher(parametersContent);

    while (matcher.find()) {

        String annotation = matcher.group(1);
        String type = matcher.group(2);
        String name = matcher.group(3);

        String source = switch (annotation) {
            case "PathVariable" -> "PATH_VARIABLE";
            case "RequestParam" -> "REQUEST_PARAM";
            case "RequestBody" -> "REQUEST_BODY";
            case "RequestHeader" -> "REQUEST_HEADER";
            default -> "UNKNOWN";
        };

        parameters.add(
                new ApiParameterResponse(
                        name,
                        type,
                        source
                )
        );
    }

    return parameters;
}
private String extractResponseStatus(String annotationContent) {

    if (annotationContent == null || annotationContent.isBlank()) {
        return "OK";
    }

    Pattern pattern = Pattern.compile(
            "HttpStatus\\.([A-Z_]+)"
    );

    Matcher matcher = pattern.matcher(annotationContent);

    if (matcher.find()) {
        return matcher.group(1);
    }

    return "OK";
}
private List<MethodCallResponse> extractMethodCalls(

        String content,

        String sourceClassKey,

        Map<String, String> projectClasses
) {

    String sourceClass =
            extractClassName(content);


    Map<String, String> importedClasses =
            extractImportsMap(content);


    List<MethodCallResponse> methodCalls =
            new ArrayList<>();


    /*
     * =====================================
     * 1. DETECT PROJECT FIELD DEPENDENCIES
     * =====================================
     */

    Map<String, String> dependencies =
            new LinkedHashMap<>();


    Pattern dependencyPattern =
            Pattern.compile(

                    "(?:@\\w+(?:\\([^)]*\\))?\\s*)*"

                            + "(?:(?:private|protected|public)\\s+)?"

                            + "(?:static\\s+)?"

                            + "(?:final\\s+)?"

                            + "(\\w+)\\s+"

                            + "(\\w+)\\s*;",

                    Pattern.MULTILINE
            );


    Matcher dependencyMatcher =
            dependencyPattern.matcher(content);


    while (dependencyMatcher.find()) {

        String targetClass =
                dependencyMatcher.group(1);


        String variableName =
                dependencyMatcher.group(2);


        String targetClassKey =
                resolveProjectClass(

                        targetClass,

                        sourceClassKey,

                        importedClasses,

                        projectClasses
                );


        if (targetClassKey != null) {

            dependencies.put(

                    variableName,

                    targetClassKey
            );
        }
    }


    /*
     * =====================================
     * 2. DETECT CONSTRUCTOR DEPENDENCIES
     * =====================================
     */

    if (sourceClass != null
            && !sourceClass.isBlank()) {


        Pattern constructorPattern =
                Pattern.compile(

                        "(?:public|protected|private)?\\s*"

                                + Pattern.quote(sourceClass)

                                + "\\s*"

                                + "\\((.*?)\\)",

                        Pattern.DOTALL
                );


        Matcher constructorMatcher =
                constructorPattern.matcher(content);


        while (constructorMatcher.find()) {

            String parameters =
                    constructorMatcher.group(1);


            if (parameters == null
                    || parameters.isBlank()) {

                continue;
            }


            String[] parameterList =
                    parameters.split(",");


            for (String parameter : parameterList) {

                parameter =
                        parameter.trim();


                /*
                 * Remove annotations
                 */

                parameter =
                        parameter.replaceAll(

                                "@\\w+(?:\\([^)]*\\))?\\s*",

                                ""
                        );


                /*
                 * Remove final keyword
                 */

                parameter =
                        parameter.replaceAll(

                                "\\bfinal\\b\\s*",

                                ""
                        );


                String[] parts =
                        parameter
                                .trim()
                                .split("\\s+");


                if (parts.length < 2) {

                    continue;
                }


                String targetClass =
                        parts[parts.length - 2];


                String variableName =
                        parts[parts.length - 1];


                /*
                 * Remove generic type
                 *
                 * Example:
                 *
                 * List<Owner>
                 *
                 * becomes:
                 *
                 * List
                 */

                targetClass =
                        targetClass.replaceAll(

                                "<.*>",

                                ""
                        );


                String targetClassKey =
                        resolveProjectClass(

                                targetClass,

                                sourceClassKey,

                                importedClasses,

                                projectClasses
                        );


                if (targetClassKey != null) {

                    dependencies.put(

                            variableName,

                            targetClassKey
                    );
                }
            }
        }
    }


    /*
     * =====================================
     * DEBUG
     * =====================================
     */

    System.out.println(
            "================================="
    );

    System.out.println(
            "SOURCE CLASS: "
                    + sourceClassKey
    );

    System.out.println(
            "PROJECT DEPENDENCIES FOUND: "
                    + dependencies
    );

    System.out.println(
            "================================="
    );


    /*
     * =====================================
     * 3. FIND METHODS IN CURRENT CLASS
     * =====================================
     */

    Pattern methodPattern =
            Pattern.compile(

                    "(?:public|private|protected)\\s+"

                            + "(?:static\\s+)?"

                            + "(?:final\\s+)?"

                            + "[\\w<>?,\\[\\].\\s]+\\s+"

                            + "(\\w+)\\s*"

                            + "\\([^{}]*?\\)\\s*"

                            + "(?:throws\\s+[\\w.,\\s]+)?"

                            + "\\{",

                    Pattern.MULTILINE
            );


    Set<String> classMethods =
            new HashSet<>();


    Matcher classMethodMatcher =
            methodPattern.matcher(content);


    while (classMethodMatcher.find()) {

        classMethods.add(
                classMethodMatcher.group(1)
        );
    }


    /*
     * =====================================
     * 4. ANALYZE EACH METHOD
     * =====================================
     */

    Matcher methodMatcher =
            methodPattern.matcher(content);


    while (methodMatcher.find()) {

        String sourceMethod =
                methodMatcher.group(1);


        int methodBodyStart =
                methodMatcher.end();


        int methodBodyEnd =
                findMatchingBrace(

                        content,

                        methodBodyStart - 1
                );


        if (methodBodyEnd == -1) {

            continue;
        }


        String methodBody =
                content.substring(

                        methodBodyStart,

                        methodBodyEnd
                );


        /*
         * =====================================
         * 5. CROSS-CLASS METHOD CALLS
         * =====================================
         */

        Pattern dependencyMethodCallPattern =
                Pattern.compile(

                        "(?:this\\.)?"

                                + "(\\w+)"

                                + "\\."

                                + "(\\w+)"

                                + "\\s*\\("
                );


        Matcher dependencyMethodCallMatcher =
                dependencyMethodCallPattern.matcher(

                        methodBody
                );


        while (dependencyMethodCallMatcher.find()) {

            String variableName =
                    dependencyMethodCallMatcher.group(1);


            String targetMethod =
                    dependencyMethodCallMatcher.group(2);


            if (!dependencies.containsKey(
                    variableName
            )) {

                continue;
            }


            String targetClassKey =
                    dependencies.get(
                            variableName
                    );


            MethodCallResponse response =
                    new MethodCallResponse(

                            sourceClassKey,

                            sourceMethod,

                            targetClassKey,

                            targetMethod
                    );


            methodCalls.add(response);


            System.out.println(

                    "CROSS CLASS CALL FOUND: "

                            + sourceClassKey

                            + "."

                            + sourceMethod

                            + " -> "

                            + targetClassKey

                            + "."

                            + targetMethod
            );
        }


        /*
         * =====================================
         * 6. INTERNAL METHOD CALLS
         * =====================================
         */

        Pattern internalMethodCallPattern =
                Pattern.compile(

                        "(?:this\\.)?"

                                + "(\\w+)"

                                + "\\s*\\("
                );


        Matcher internalMethodCallMatcher =
                internalMethodCallPattern.matcher(

                        methodBody
                );


        while (internalMethodCallMatcher.find()) {

            String targetMethod =
                    internalMethodCallMatcher.group(1);


            /*
             * Ignore recursion
             */

            if (targetMethod.equals(
                    sourceMethod
            )) {

                continue;
            }


            /*
             * Only methods belonging
             * to this class
             */

            if (!classMethods.contains(
                    targetMethod
            )) {

                continue;
            }


            MethodCallResponse response =
                    new MethodCallResponse(

                            sourceClassKey,

                            sourceMethod,

                            sourceClassKey,

                            targetMethod
                    );


            methodCalls.add(response);


            System.out.println(

                    "INTERNAL CALL FOUND: "

                            + sourceClassKey

                            + "."

                            + sourceMethod

                            + " -> "

                            + sourceClassKey

                            + "."

                            + targetMethod
            );
        }
    }


    return methodCalls;
}
public List<DependencyGraphResponse> buildDependencyGraph(
        List<ClassDependencyResponse> dependencies
) {

    Map<String, List<String>> dependencyMap =
            new LinkedHashMap<>();

    for (ClassDependencyResponse dependency : dependencies) {

        String sourceClass =
                dependency.getSourceClass();

        String targetClass =
                dependency.getTargetClass();

        dependencyMap
                .computeIfAbsent(
                        sourceClass,
                        key -> new ArrayList<>()
                )
                .add(targetClass);
    }

    List<DependencyGraphResponse> graph =
            new ArrayList<>();

    for (Map.Entry<String, List<String>> entry
            : dependencyMap.entrySet()) {

        graph.add(
                new DependencyGraphResponse(
                        entry.getKey(),
                        entry.getValue()
                )
        );
    }

    return graph;
}
public List<MethodCallResponse> analyzeRepositoryMethodCalls(
        List<GitHubFileContentResponse> javaFiles
) {

    List<MethodCallResponse> allMethodCalls =
            new ArrayList<>();

    Set<String> uniqueCalls =
            new HashSet<>();


    /*
     * =====================================
     * 1. BUILD PROJECT CLASS MAP
     * =====================================
     *
     * Example:
     *
     * OwnerRepository
     *
     * ->
     *
     * org.springframework.samples.petclinic.owner.OwnerRepository
     */

    Map<String, String> projectClasses =
            new HashMap<>();


    for (GitHubFileContentResponse file : javaFiles) {

        String content =
                file.getContent();


        String classKey =
                extractClassKey(content);


        String className =
                extractClassName(content);


        if (classKey != null
                && !classKey.isBlank()
                && className != null
                && !className.isBlank()) {

            projectClasses.put(
                    className,
                    classKey
            );
        }
    }


    System.out.println(
            "================================="
    );

    System.out.println(
            "PROJECT CLASS MAP:"
    );

    System.out.println(
            projectClasses
    );

    System.out.println(
            "================================="
    );


    /*
     * =====================================
     * 2. ANALYZE EACH JAVA FILE
     * =====================================
     */

    for (GitHubFileContentResponse file : javaFiles) {

        String content =
                file.getContent();


        String sourceClassKey =
                extractClassKey(content);


        String simpleClassName =
                extractClassName(content);


        /*
         * Skip invalid classes
         */

        if (sourceClassKey == null
                || sourceClassKey.isBlank()
                || simpleClassName == null
                || simpleClassName.isBlank()
                || simpleClassName.endsWith("Test")
                || simpleClassName.endsWith("Tests")) {

            continue;
        }


        /*
         * =====================================
         * 3. EXTRACT METHOD CALLS
         * =====================================
         */

        List<MethodCallResponse> fileMethodCalls =
                extractMethodCalls(

                        content,

                        sourceClassKey,

                        projectClasses
                );


        /*
         * =====================================
         * 4. REMOVE DUPLICATES
         * =====================================
         */

        for (MethodCallResponse methodCall
                : fileMethodCalls) {


            String callKey =

                    methodCall.getSourceClass()

                            + "::"

                            + methodCall.getSourceMethod()

                            + "->"

                            + methodCall.getTargetClass()

                            + "::"

                            + methodCall.getTargetMethod();


            if (uniqueCalls.add(callKey)) {

                allMethodCalls.add(
                        methodCall
                );
            }
        }
    }


    System.out.println(
            "================================="
    );

    System.out.println(
            "TOTAL UNIQUE METHOD CALLS: "
                    + allMethodCalls.size()
    );

    System.out.println(
            "================================="
    );


    return allMethodCalls;
}
public List<CallFlowResponse> buildCallFlow(
        List<MethodCallResponse> methodCalls
) {

    Map<String, List<MethodCallResponse>> groupedCalls =
            new LinkedHashMap<>();

    for (MethodCallResponse methodCall : methodCalls) {

        String key =
                methodCall.getSourceClass()
                        + "#"
                        + methodCall.getSourceMethod();

        groupedCalls
                .computeIfAbsent(
                        key,
                        k -> new ArrayList<>()
                )
                .add(methodCall);
    }

    List<CallFlowResponse> callFlows =
            new ArrayList<>();

    for (List<MethodCallResponse> calls
            : groupedCalls.values()) {

        MethodCallResponse firstCall =
                calls.get(0);

        callFlows.add(
                new CallFlowResponse(
                        firstCall.getSourceClass(),
                        firstCall.getSourceMethod(),
                        calls
                )
        );
    }

    return callFlows;
}
private int findMatchingBrace(
        String content,
        int openingBraceIndex
) {

    int braceCount = 0;

    for (int i = openingBraceIndex;
            i < content.length();
            i++) {

        char currentChar = content.charAt(i);

        if (currentChar == '{') {
            braceCount++;
        }

        if (currentChar == '}') {
            braceCount--;

            if (braceCount == 0) {
                return i;
            }
        }
    }

    return -1;
}
private List<ClassDependencyResponse> extractClassDependencies(
        String content,
        String sourceClass,
        Map<String, String> classNameMap
) {

    List<ClassDependencyResponse> classDependencies =
            new ArrayList<>();

    List<String> dependencies =
            extractDependencies(content);

    Set<String> uniqueDependencies =
            new HashSet<>();


    for (String dependency : dependencies) {

        if (dependency == null
                || dependency.isBlank()) {

            continue;
        }


        /*
         * Convert simple class name
         * to fully qualified class name
         */

        String targetClass =
                classNameMap.get(dependency);


        if (targetClass == null) {

            continue;
        }


        if (targetClass.equals(sourceClass)) {

            continue;
        }


        if (uniqueDependencies.add(targetClass)) {

            classDependencies.add(

                    new ClassDependencyResponse(
                            sourceClass,
                            targetClass
                    )
            );
        }
    }


    return classDependencies;
}
private String buildClassKey(
        String content
) {

    String packageName =
            extractPackageName(content);

    String className =
            extractClassName(content);

    if (className == null
            || className.isBlank()) {

        return null;
    }

    if (packageName == null
            || packageName.isBlank()) {

        return className;
    }

    return packageName
            + "."
            + className;
}
public List<CallFlowNodeResponse> buildCallFlowTree(
        List<MethodCallResponse> methodCalls,
        List<GitHubFileContentResponse> javaFiles
) {

    Map<String, String> classContentMap =
            buildClassContentMap(javaFiles);


    /*
     * Build interface → implementation map
     */
    Map<String, String> interfaceImplementationMap =
            buildInterfaceImplementationMap(javaFiles);


    List<CallFlowNodeResponse> rootNodes =
            new ArrayList<>();


    Set<String> targetNodes =
            new HashSet<>();


    for (MethodCallResponse methodCall : methodCalls) {

        String targetKey =
                methodCall.getTargetClass()
                        + "#"
                        + methodCall.getTargetMethod();

        targetNodes.add(targetKey);
    }


    Set<String> processedRoots =
            new HashSet<>();


    for (MethodCallResponse methodCall : methodCalls) {

        String sourceKey =
                methodCall.getSourceClass()
                        + "#"
                        + methodCall.getSourceMethod();


        if (!targetNodes.contains(sourceKey)
                && processedRoots.add(sourceKey)) {


            CallFlowNodeResponse rootNode =
                    buildCallFlowNode(

                            methodCall.getSourceClass(),

                            methodCall.getSourceMethod(),

                            methodCalls,

                            classContentMap,

                            new HashMap<>(),

                            interfaceImplementationMap,

                            new HashSet<>()
                    );


            rootNodes.add(rootNode);
        }
    }


    return rootNodes;
}
private CallFlowNodeResponse buildCallFlowNode(
        String className,
        String methodName,
        List<MethodCallResponse> methodCalls,
        Map<String, String> classContentMap,
        Map<String, String> classTypes,
        Map<String, String> interfaceImplementationMap,
        Set<String> visited
) {

    /*
     * =====================================
     * 1. CREATE CURRENT NODE KEY
     * =====================================
     */

    String currentNode =
            className
                    + "#"
                    + methodName;


    /*
     * =====================================
     * 2. PREVENT INFINITE LOOPS
     * =====================================
     *
     * Example:
     *
     * A.method1()
     *      ↓
     * B.method2()
     *      ↓
     * A.method1()
     */

    if (visited.contains(currentNode)) {

        String content =
                classContentMap.get(className);


        ClassType classType =
                getClassType(
                        className,
                        content,
                        classTypes
                );


        DatabaseOperation databaseOperation =
                detectDatabaseOperation(
                        methodName,
                        classType
                );


        return new CallFlowNodeResponse(

                className,

                methodName,

                classType,

                databaseOperation,

                new ArrayList<>()
        );
    }


    /*
     * Add current node to
     * the current call path.
     */

    visited.add(currentNode);


    /*
     * =====================================
     * 3. DETECT CLASS TYPE
     * =====================================
     */

    String content =
            classContentMap.get(className);


    ClassType classType =
            getClassType(

                    className,

                    content,

                    classTypes
            );


    /*
     * =====================================
     * 4. FIND DIRECT METHOD CALLS
     * =====================================
     */

    List<CallFlowNodeResponse> nextCalls =
            new ArrayList<>();


    /*
     * Prevent duplicate child nodes.
     */

    Set<String> addedCalls =
            new HashSet<>();


    boolean directCallFound =
            false;


    for (MethodCallResponse methodCall
            : methodCalls) {


        /*
         * Check whether this call belongs
         * to the current method.
         */

        boolean isMatchingSource =

                className.equals(
                        methodCall.getSourceClass()
                )

                        &&

                        methodName.equals(
                                methodCall.getSourceMethod()
                        );


        if (!isMatchingSource) {

            continue;
        }


        directCallFound =
                true;


        String targetClass =
                methodCall.getTargetClass();


        String targetMethod =
                methodCall.getTargetMethod();


        /*
         * Create unique child identifier.
         */

        String targetNode =
                targetClass
                        + "#"
                        + targetMethod;


        /*
         * Prevent duplicate child nodes.
         */

        if (!addedCalls.add(targetNode)) {

            continue;
        }


        /*
         * Build child node recursively.
         */

        CallFlowNodeResponse nextNode =
                buildCallFlowNode(

                        targetClass,

                        targetMethod,

                        methodCalls,

                        classContentMap,

                        classTypes,

                        interfaceImplementationMap,

                        new HashSet<>(visited)
                );


        nextCalls.add(nextNode);
    }


    /*
     * =====================================
     * 5. RESOLVE INTERFACE IMPLEMENTATION
     * =====================================
     *
     * Only resolve an interface when
     * no direct method calls were found.
     *
     * Example:
     *
     * UserService
     *
     * maps to
     *
     * UserServiceImpl
     */

    if (!directCallFound
            && interfaceImplementationMap.containsKey(className)) {


        String implementationClass =
                interfaceImplementationMap.get(className);


        /*
         * Prevent resolving the same class.
         */

        if (implementationClass != null
                && !implementationClass.isBlank()
                && !implementationClass.equals(className)) {


            System.out.println(

                    "INTERFACE RESOLVED: "

                            + className

                            + "."

                            + methodName

                            + " -> "

                            + implementationClass

                            + "."

                            + methodName
            );


            CallFlowNodeResponse implementationNode =
                    buildCallFlowNode(

                            implementationClass,

                            methodName,

                            methodCalls,

                            classContentMap,

                            classTypes,

                            interfaceImplementationMap,

                            new HashSet<>(visited)
                    );


            nextCalls.add(
                    implementationNode
            );
        }
    }


    /*
     * =====================================
     * 6. DETECT DATABASE OPERATION
     * =====================================
     */

    DatabaseOperation databaseOperation =
            detectDatabaseOperation(

                    methodName,

                    classType
            );


    /*
     * =====================================
     * 7. CREATE CALL FLOW NODE
     * =====================================
     */

    return new CallFlowNodeResponse(

            className,

            methodName,

            classType,

            databaseOperation,

            nextCalls
    );
}
private ClassType getClassType(
        String className,
        String content,
        Map<String, String> classTypes
) {

    String componentType =
            classTypes.get(className);

    if (componentType != null) {

        return switch (componentType) {

            case "REST_CONTROLLER",
                 "CONTROLLER" ->
                    ClassType.CONTROLLER;

            case "SERVICE" ->
                    ClassType.SERVICE;

            case "REPOSITORY" ->
                    ClassType.REPOSITORY;

            default ->
                    ClassType.OTHER;
        };
    }

    // Fallback to existing detection
    return detectClassType(
            className,
            content
    );
}
private ClassType detectClassType(
        String className,
        String content
) {

    /*
     * =====================================
     * 1. ANNOTATION BASED DETECTION
     * =====================================
     */

    if (content != null && !content.isBlank()) {

        /*
         * Spring Boot Application
         */

        if (content.contains("@SpringBootApplication")) {

            return ClassType.APPLICATION;
        }


        /*
         * Controller
         */

        if (content.contains("@RestController")
                || content.contains("@Controller")) {

            return ClassType.CONTROLLER;
        }


        /*
         * Service
         */

        if (content.contains("@Service")) {

            return ClassType.SERVICE;
        }


        /*
         * Repository
         */

        if (content.contains("@Repository")
                || content.contains("extends JpaRepository")
                || content.contains("extends CrudRepository")
                || content.contains("extends PagingAndSortingRepository")
                || content.contains("extends Repository")) {

            return ClassType.REPOSITORY;
        }


        /*
         * Configuration
         */

        if (content.contains("@Configuration")) {

            return ClassType.CONFIGURATION;
        }


        /*
         * Component
         */

        if (content.contains("@Component")) {

            return ClassType.COMPONENT;
        }


        /*
         * Entity
         */

        if (content.contains("@Entity")) {

            return ClassType.ENTITY;
        }
    }


    /*
     * =====================================
     * 2. NAMING FALLBACK
     * =====================================
     */

    if (className != null && !className.isBlank()) {

        if (className.endsWith("Application")) {

            return ClassType.APPLICATION;
        }


        if (className.endsWith("Controller")) {

            return ClassType.CONTROLLER;
        }


        if (className.endsWith("Service")
                || className.endsWith("ServiceImpl")) {

            return ClassType.SERVICE;
        }


        if (className.endsWith("Repository")) {

            return ClassType.REPOSITORY;
        }


        if (className.endsWith("Configuration")
                || className.endsWith("Config")) {

            return ClassType.CONFIGURATION;
        }


        if (className.endsWith("Component")) {

            return ClassType.COMPONENT;
        }
    }


    /*
     * =====================================
     * 3. DEFAULT
     * =====================================
     */

    return ClassType.OTHER;
}
private String extractClassKey(String content) {

    String packageName =
            extractPackageName(content);

    String className =
            extractClassName(content);

    if (className == null || className.isBlank()) {
        return null;
    }

    if (packageName == null || packageName.isBlank()) {
        return className;
    }

    return packageName + "." + className;
}
private Map<String, String> buildClassContentMap(
        List<GitHubFileContentResponse> javaFiles
) {

    Map<String, String> classContentMap =
            new HashMap<>();

    for (GitHubFileContentResponse file : javaFiles) {

        String content = file.getContent();

        String className = extractClassName(content);

        if (className != null) {
            classContentMap.put(
                    className,
                    content
            );
        }
    }

    return classContentMap;
}
private Map<String, String> buildInterfaceImplementationMap(
        List<GitHubFileContentResponse> javaFiles
) {

    Map<String, String> interfaceImplementationMap =
            new HashMap<>();


    for (GitHubFileContentResponse file : javaFiles) {

        String content = file.getContent();


        if (content == null || content.isBlank()) {
            continue;
        }


        /*
         * =====================================
         * GET IMPLEMENTATION CLASS
         * =====================================
         */

        String implementationClassKey =
                buildClassKey(content);


        if (implementationClassKey == null
                || implementationClassKey.isBlank()) {

            continue;
        }


        /*
         * =====================================
         * EXTRACT IMPORTS
         * =====================================
         *
         * Example:
         *
         * import com.example.ProductService;
         *
         * Map:
         *
         * ProductService
         *      →
         * com.example.ProductService
         */

        Map<String, String> importedClasses =
                extractImportsMap(content);


        /*
         * =====================================
         * FIND IMPLEMENTS CLAUSE
         * =====================================
         *
         * Example:
         *
         * public class ProductServiceImpl
         * implements ProductService
         *
         *
         * Multiple interfaces:
         *
         * implements InterfaceA, InterfaceB
         */

        Pattern implementsPattern =
                Pattern.compile(

                        "\\bimplements\\s+([^\\{]+)"

                );


        Matcher matcher =
                implementsPattern.matcher(content);


        if (!matcher.find()) {
            continue;
        }


        String interfaces =
                matcher.group(1)
                        .trim();


        /*
         * Remove unwanted spaces
         */

        interfaces =
                interfaces.replaceAll(
                        "\\s+",
                        " "
                );


        /*
         * Split interfaces
         */

        String[] interfaceNames =
                interfaces.split(",");


        /*
         * =====================================
         * PROCESS EACH INTERFACE
         * =====================================
         */

        for (String interfaceName
                : interfaceNames) {


            interfaceName =
                    interfaceName.trim();


            if (interfaceName.isBlank()) {
                continue;
            }


            /*
             * Remove generic types.
             *
             * Example:
             *
             * SomeInterface<String>
             *
             * becomes:
             *
             * SomeInterface
             */

            interfaceName =
                    interfaceName.replaceAll(
                            "<.*?>",
                            ""
                    );


            interfaceName =
                    interfaceName.trim();


            /*
             * =====================================
             * RESOLVE FULL INTERFACE NAME
             * =====================================
             */

            String interfaceClassKey = null;


            /*
             * Case 1:
             *
             * Fully qualified interface
             *
             * Example:
             *
             * implements
             * com.example.MyInterface
             */

            if (interfaceName.contains(".")) {

                interfaceClassKey =
                        interfaceName;

            }


            /*
             * Case 2:
             *
             * Imported interface
             */

            else if (importedClasses
                    .containsKey(interfaceName)) {

                interfaceClassKey =
                        importedClasses.get(
                                interfaceName
                        );

            }


            /*
             * Case 3:
             *
             * Same package interface
             */

            else {

                String packageName =
                        extractPackageName(content);


                if (packageName != null
                        && !packageName.isBlank()) {

                    interfaceClassKey =
                            packageName
                                    + "."
                                    + interfaceName;

                } else {

                    interfaceClassKey =
                            interfaceName;
                }
            }


            /*
             * =====================================
             * STORE MAPPING
             * =====================================
             *
             * Interface
             *      →
             * Implementation
             */

            if (interfaceClassKey != null
                    && !interfaceClassKey.isBlank()) {


                interfaceImplementationMap.put(

                        interfaceClassKey,

                        implementationClassKey

                );


                System.out.println(

                        "INTERFACE IMPLEMENTATION FOUND: "

                                + interfaceClassKey

                                + " -> "

                                + implementationClassKey

                );
            }
        }
    }


    /*
     * =====================================
     * DEBUG OUTPUT
     * =====================================
     */

    System.out.println(
            "================================="
    );


    System.out.println(
            "PROJECT INTERFACE IMPLEMENTATION MAP:"
    );


    System.out.println(
            interfaceImplementationMap
    );


    System.out.println(
            "================================="
    );


    return interfaceImplementationMap;
}
public List<ApiCallFlowResponse> buildApiCallFlows(
        List<GitHubFileContentResponse> javaFiles
) {

    /*
     * =====================================
     * 1. BUILD CLASS TYPE MAP
     * =====================================
     */
    System.out.println(
        "\n===== CLASS IDENTIFICATION TEST ====="
);

for (GitHubFileContentResponse file : javaFiles) {

    String content =
            file.getContent();

    String packageName =
            extractPackageName(content);

    String className =
            extractClassName(content);

    String classKey =
            buildClassKey(content);

    System.out.println(
            "PACKAGE: " + packageName
    );

    System.out.println(
            "CLASS: " + className
    );

    System.out.println(
            "CLASS KEY: " + classKey
    );

    System.out.println(
            "--------------------------------"
    );
}
    Map<String, String> classTypes =
            new HashMap<>();


    Map<String, String> classContentMap =
            new HashMap<>();

    
    for (GitHubFileContentResponse file : javaFiles) {

        String content =
                file.getContent();


        String classKey =
                buildClassKey(content);


        if (classKey == null
                || classKey.isBlank()) {

            continue;
        }


        /*
         * Store class content
         */

        classContentMap.put(
                classKey,
                content
        );


        /*
         * Analyze Java file
         */

        JavaFileAnalysisResponse analysis =
                analyzeJavaFile(content);


        if (analysis != null
                && analysis.getComponentType() != null) {

            classTypes.put(
                    classKey,
                    analysis.getComponentType()
            );
        }
    }

    /*
 * =====================================
 * 2. BUILD INTERFACE IMPLEMENTATION MAP
 * =====================================
 */

Map<String, String> interfaceImplementationMap =
        buildInterfaceImplementationMap(javaFiles);

System.out.println(
        "================================="
);

System.out.println(
        "INTERFACE IMPLEMENTATION MAP:"
);

System.out.println(
        interfaceImplementationMap
);

System.out.println(
        "================================="
);
    /*
     * =====================================
     * 2. DETECT SERVICE INTERFACES
     * =====================================
     *
     * Example:
     *
     * ProductServiceImpl
     * implements ProductService
     *
     * Therefore:
     *
     * ProductService → SERVICE
     */

    for (GitHubFileContentResponse file : javaFiles) {

        String content =
                file.getContent();


        String classKey =
                buildClassKey(content);


        if (classKey == null) {
            continue;
        }


        String componentType =
                classTypes.get(classKey);


        /*
         * Check whether this class
         * is a SERVICE implementation.
         */

        if ("SERVICE".equals(componentType)) {

            Pattern implementsPattern =
                    Pattern.compile(

                            "implements\\s+([\\w\\s,]+)"

                    );


            Matcher implementsMatcher =
                    implementsPattern.matcher(content);


            if (implementsMatcher.find()) {

                String interfaces =
                        implementsMatcher.group(1);


                String[] interfaceList =
                        interfaces.split(",");

                Map<String, String> importedClasses =
                    extractImportsMap(content);
                for (String interfaceName
                        : interfaceList) {

                    interfaceName =
                            interfaceName.trim();


                    if (interfaceName.isBlank()) {

                        continue;
                    }
                    String interfaceClassKey =
                        importedClasses.get(interfaceName);
                    if (interfaceClassKey == null) {

                    String packageName =
                            extractPackageName(content);

                    if (packageName != null) {

                        interfaceClassKey =
                                packageName
                                        + "."
                                        + interfaceName;
                    }
                }


                /*
                 * Mark interface as SERVICE
                 */

                if (interfaceClassKey != null) {

                    classTypes.put(
                            interfaceClassKey,
                            "SERVICE"
                    );
                }   
                }
            }
        }
    }


    /*
     * =====================================
     * DEBUG
     * =====================================
     */

    System.out.println(
            "================================="
    );

    System.out.println(
            "CLASS TYPES: "
                    + classTypes
    );

    System.out.println(
            "================================="
    );


    /*
     * =====================================
     * 3. EXTRACT API CONTROLLERS
     * =====================================
     */

    List<ControllerApiResponse> apiControllers =
            analyzeRepositoryApiEndpoints(
                    javaFiles
            );


    /*
     * =====================================
     * 4. ANALYZE METHOD CALLS
     * =====================================
     */

    List<MethodCallResponse> methodCalls =
            analyzeRepositoryMethodCalls(
                    javaFiles
            );


    /*
     * =====================================
     * 5. BUILD API CALL FLOWS
     * =====================================
     */

    List<ApiCallFlowResponse> apiCallFlows =
            new ArrayList<>();


    for (ControllerApiResponse controller
            : apiControllers) {


        for (ApiEndpointResponse endpoint
                : controller.getEndpoints()) {


            CallFlowNodeResponse callFlow =
                    buildCallFlowNode(

                            controller.getControllerName(),

                            endpoint.getHandler(),

                            methodCalls,

                            classContentMap,

                            classTypes,

                            interfaceImplementationMap,

                            new HashSet<>()
                    );


            apiCallFlows.add(

                    new ApiCallFlowResponse(

                            endpoint.getMethod(),

                            endpoint.getPath(),

                            callFlow
                    )
            );
        }
    }


    return apiCallFlows;
}
private DatabaseOperation detectDatabaseOperation(
        String methodName,
        ClassType classType
) {

    if (classType != ClassType.REPOSITORY) {
        return DatabaseOperation.NONE;
    }

    String method = methodName.toLowerCase();

    // DELETE operations
    if (method.startsWith("delete")
            || method.startsWith("remove")) {

        return DatabaseOperation.DELETE;
    }

    // UPDATE operations
    if (method.startsWith("update")
            || method.startsWith("patch")) {

        return DatabaseOperation.UPDATE;
    }

    // WRITE operations
    if (method.startsWith("save")
            || method.startsWith("insert")
            || method.startsWith("persist")
            || method.startsWith("create")
            || method.startsWith("merge")) {

        return DatabaseOperation.WRITE;
    }

    // READ operations
    if (method.startsWith("find")
            || method.startsWith("get")
            || method.startsWith("read")
            || method.startsWith("fetch")
            || method.startsWith("exists")
            || method.startsWith("count")
            || method.startsWith("query")
            || method.startsWith("search")
            || method.startsWith("lookup")
            || method.startsWith("list")
            || method.startsWith("stream")) {

        return DatabaseOperation.READ;
    }

    return DatabaseOperation.NONE;
}
public ImpactAnalysisResponse analyzeMethodImpact(
        String githubUrl,
        String targetClass,
        String targetMethod
) {

    List<GitHubFileContentResponse> javaFiles =
            gitHubService.getAllJavaFilesWithContent(githubUrl);

    List<MethodCallResponse> methodCalls =
            analyzeRepositoryMethodCalls(javaFiles);

    List<ImpactCallerResponse> callers =
            new ArrayList<>();

    Set<String> addedCallers =
            new HashSet<>();

    for (MethodCallResponse methodCall : methodCalls) {

        boolean sameClass =
                isSameClassName(
                        methodCall.getTargetClass(),
                        targetClass
                );

        boolean sameMethod =
                methodCall.getTargetMethod() != null
                        && methodCall.getTargetMethod()
                        .equals(targetMethod);

        if (!sameClass || !sameMethod) {
            continue;
        }

        String callerKey =
                methodCall.getSourceClass()
                        + "#"
                        + methodCall.getSourceMethod();

        if (addedCallers.add(callerKey)) {

            callers.add(
                    new ImpactCallerResponse(
                            methodCall.getSourceClass(),
                            methodCall.getSourceMethod()
                    )
            );
        }
    }

    Map<String, String> classTypes =
            new HashMap<>();

    String targetContent = null;

    for (GitHubFileContentResponse file : javaFiles) {

        JavaFileAnalysisResponse analysis =
                analyzeJavaFile(
                        file.getContent()
                );

        String className =
                analysis.getClassName();

        if (className != null
                && !className.isBlank()) {

            classTypes.put(
                    className,
                    analysis.getComponentType()
            );
        }

        if (isSameClassName(
                className,
                targetClass
        )) {

            targetContent =
                    file.getContent();
        }
    }

    ClassType classType =
            getClassType(
                    targetClass,
                    targetContent,
                    classTypes
            );

    DatabaseOperation databaseOperation =
            detectDatabaseOperation(
                    targetMethod,
                    classType
            );

    return new ImpactAnalysisResponse(
            targetClass,
            targetMethod,
            classType,
            databaseOperation,
            callers
    );
}
private boolean isSameClassName(
        String actualClassName,
        String requestedClassName
) {
    if (actualClassName == null
            || requestedClassName == null) {
        return false;
    }

    String actual = actualClassName.trim();
    String requested = requestedClassName.trim();

    if (actual.isBlank() || requested.isBlank()) {
        return false;
    }

    return actual.equals(requested)
            || actual.endsWith("." + requested);
}
public String generateCodeExplanation(String content) {

    JavaFileAnalysisResponse analysis =
            analyzeJavaFile(content);

    String prompt = """
            You are an AI assistant for a Java code analysis platform called CodeCompass.

            Analyze the following Java code.

            JAVA CODE:
            --------------------
            %s
            --------------------

            Extracted Code Information:

            Class Name: %s
            Package: %s
            Component Type: %s

            Imports:
            %s

            Methods:
            %s

            Dependencies:
            %s

            Provide:

            1. A short overview of the class

            2. What this class is responsible for

            3. Explain each method and what it does

            4. Explain the dependencies and why they are used

            5. Explain the overall flow of the code

            Keep the explanation beginner-friendly.
            Use clear headings.
            Do not make assumptions about code that is not present.
            """
            .formatted(
                    content,
                    analysis.getClassName(),
                    analysis.getPackageName(),
                    analysis.getComponentType(),
                    analysis.getImports(),
                    analysis.getMethods(),
                    analysis.getDependencies()
            );

    return ollamaService.generateResponse(prompt);
}
public List<ClassDependencyResponse>
analyzeRepositoryDependencies(
        List<GitHubFileContentResponse> javaFiles
) {

    List<ClassDependencyResponse> dependencies =
            new ArrayList<>();


    /*
     * =====================================
     * STEP 1:
     * BUILD SIMPLE NAME → FULL NAME MAP
     * =====================================
     */

    Map<String, String> classNameMap =
            buildClassNameMap(javaFiles);


    System.out.println(
            "================================="
    );

    System.out.println(
            "CLASS NAME MAP:"
    );

    System.out.println(
            classNameMap
    );

    System.out.println(
            "================================="
    );


    Set<String> uniqueDependencies =
            new HashSet<>();


    /*
     * =====================================
     * STEP 2:
     * ANALYZE EACH JAVA FILE
     * =====================================
     */

    for (GitHubFileContentResponse file :
            javaFiles) {

        String content =
                file.getContent();


        String packageName =
                extractPackageName(content);


        String className =
                extractClassName(content);


        if (packageName == null
                || className == null
                || className.isBlank()) {

            continue;
        }


        /*
         * Skip test classes
         */

        if (className.endsWith("Test")
                || className.endsWith("Tests")) {

            continue;
        }


        String sourceClass =
                packageName
                        + "."
                        + className;


        System.out.println(
                "================================="
        );

        System.out.println(
                "SOURCE CLASS: "
                        + sourceClass
        );


        /*
         * Extract dependencies
         */

        List<String> detectedDependencies =
                extractDependencies(content);


        System.out.println(
                "DETECTED DEPENDENCIES: "
                        + detectedDependencies
        );


        List<ClassDependencyResponse>
                fileDependencies =
                extractClassDependencies(

                        content,

                        sourceClass,

                        classNameMap
                );


        for (ClassDependencyResponse dependency :
                fileDependencies) {


            String dependencyKey =

                    dependency.getSourceClass()

                            + "->"

                            + dependency.getTargetClass();


            if (uniqueDependencies.add(
                    dependencyKey
            )) {

                dependencies.add(
                        dependency
                );


                System.out.println(
                        "PROJECT DEPENDENCY FOUND: "

                                + dependency.getSourceClass()

                                + " -> "

                                + dependency.getTargetClass()
                );
            }
        }
    }


    return dependencies;
}
public String generateRepositoryAnalysis(
        String githubUrl
) {

    // Fetch repository only once

    List<GitHubFileContentResponse> javaFiles =
            gitHubService.getAllJavaFilesWithContent(
                    githubUrl
            );


    // Analyze architecture

    ProjectArchitectureResponse architecture =
            analyzeProjectArchitecture(javaFiles);


    // Analyze API endpoints

    List<ControllerApiResponse> apiMap =
            analyzeRepositoryApiEndpoints(javaFiles);


    // Analyze dependencies

    List<ClassDependencyResponse> dependencies =
            analyzeRepositoryDependencies(javaFiles);


    // Analyze method calls

    List<MethodCallResponse> methodCalls =
            analyzeRepositoryMethodCalls(javaFiles);


    String prompt = """
            You are CodeCompass, an AI assistant that analyzes Java projects.

            Analyze the following Java project based only on
            the provided static analysis results.

            ==============================
            PROJECT ARCHITECTURE
            ==============================

            Applications:
            %s

            REST Controllers:
            %s

            Controllers:
            %s

            Services:
            %s

            Repositories:
            %s

            Entities:
            %s

            Configurations:
            %s

            Components:
            %s

            Exception Handlers:
            %s

            Other Classes:
            %s


            ==============================
            API ENDPOINTS
            ==============================

            %s


            ==============================
            CLASS DEPENDENCIES
            ==============================

            %s


            ==============================
            METHOD CALLS
            ==============================

            %s


            IMPORTANT RULES:

            1. Use only the provided analysis information.

            2. Do not invent classes, methods,
               APIs, databases, or functionality.

            3. Do not claim a relationship unless
               it is present in the dependency or
               method call analysis.

            4. If information cannot be determined,
               clearly say so.

            5. Keep the explanation beginner-friendly.

            Provide:

            ## 1. Project Overview

            ## 2. Architecture

            ## 3. Main Components

            ## 4. API Layer

            ## 5. Dependency Flow

            ## 6. Execution Flow

            ## 7. Database Interaction
            """.formatted(

                    architecture.getApplications(),

                    architecture.getRestControllers(),

                    architecture.getControllers(),

                    architecture.getServices(),

                    architecture.getRepositories(),

                    architecture.getEntities(),

                    architecture.getConfigurations(),

                    architecture.getComponents(),

                    architecture.getExceptionHandlers(),

                    architecture.getOtherClasses(),

                    apiMap,

                    dependencies,

                    methodCalls
            );


    return ollamaService.generateResponse(prompt);
}
public String generateArchitectureExplanation(
        String githubUrl
) {

    List<GitHubFileContentResponse> javaFiles =
            gitHubService.getAllJavaFilesWithContent(
                    githubUrl
            );

    ProjectArchitectureResponse architecture =
            analyzeProjectArchitecture(javaFiles);


    String prompt = """
            You are CodeCompass, an AI assistant for
            analyzing Java project architecture.

            You MUST analyze ONLY the information
            provided below.

            DETECTED PROJECT STRUCTURE:

            Applications:
            %s

            REST Controllers:
            %s

            Controllers:
            %s

            Services:
            %s

            Repositories:
            %s

            Entities:
            %s

            Configurations:
            %s

            Components:
            %s

            Exception Handlers:
            %s

            Other Classes:
            %s


            STRICT RULES:

            1. Do NOT invent classes.

            2. Do NOT invent methods.

            3. Do NOT generate example Java code.

            4. Do NOT assume specific APIs,
               endpoints, databases, or functionality.

            5. Do NOT claim that one specific class
               calls another specific class unless that
               relationship is explicitly provided.

            6. Only discuss the classes listed in
               the detected project structure.

            7. If information cannot be determined
               from the project structure, say:

               "This cannot be determined from the
               available project structure."

            8. Controllers are NOT Views.

            9. Services are NOT Controllers.

            10. Describe relationships only as
                general architectural relationships.

            11. Do not assume that every project
                contains all layers.

            12. If a component category is empty,
                clearly mention that no components
                of that type were detected.
         
           13. Do NOT describe relationships involving a
                component category that is empty.

        14. Do NOT mention Services, Repositories,
        Entities, Exception Handlers, Configurations,
        or Components in the relationship or request
        flow if no components of that type were detected.

        15. Do NOT claim that a client sends an HTTP request
        directly to an Application class.

        The Application class should only be described
        as the application entry point or bootstrap class.

        16. Only include detected component categories
        when describing component relationships.

        17. If only REST Controllers are detected,
        describe the request flow generally as:

        Client → REST Controller → Response

        Do not add layers that were not detected.
        18. Do NOT label the project as monolithic,
        microservices, MVC, clean architecture,
        hexagonal architecture, or any other
        architecture style unless the detected
        structure provides sufficient evidence.


            Provide the response in the following format:


            ## 1. Project Architecture Overview

            Briefly describe the architecture based
            only on the detected components.


            ## 2. Detected Components

            Clearly describe the detected:

            Applications
            REST Controllers
            Controllers
            Services
            Repositories
            Entities
            Configurations
            Components
            Exception Handlers
            Other Classes


            ## 3. Component Responsibilities

            Explain responsibilities ONLY for component
            types that contain at least one detected class.

            Do NOT explain responsibilities for empty
            component categories.

            Do not describe specific functionality
            that cannot be determined from the
            project structure.

        ## 4. General Component Relationships

Describe ONLY general architectural relationships
that are supported by the detected component types.

Do NOT claim that one detected layer directly
calls another layer unless that relationship is
explicitly provided.

Do NOT claim that Controllers directly call
Repositories.

If Services are not detected, do NOT insert
a Service layer.

If the exact relationships between the detected
components cannot be determined from the project
structure, say:

"This cannot be determined from the available
project structure."

You may describe the components as belonging to
the same application architecture without claiming
direct interactions.

## 5. General Request Flow

Construct the request flow dynamically using ONLY
component types that were actually detected.

Do NOT use a predefined request flow example.

Rules:

- If REST Controllers are detected, use
  "REST Controller" in the flow.

- Else if Controllers are detected, use
  "Controller" in the flow.

- Do NOT include Services unless Services were
  detected.

- Do NOT include Repositories unless Repositories
  were detected.

- Do NOT include Entities as a request-processing
  layer.

- Do NOT include Configurations or Components
  unless their role in request processing is
  explicitly known.

- Do NOT say that a Client sends a request directly
  to the Application class.

IMPORTANT:

The exact request flow cannot be determined from
component detection alone.

Therefore, clearly label the flow as a POSSIBLE
GENERAL FLOW based only on the detected structure.

If no reliable flow can be constructed from the
detected component types, say:

"This cannot be determined from the available
project structure."
          
            ## 6. Architecture Pattern

            Identify the likely architecture pattern
            based ONLY on the detected structure.

            If the architecture pattern cannot be
            confidently determined, say so.


            Keep the response beginner-friendly,
            accurate, concise and structured.
            """
            .formatted(

                    architecture.getApplications(),

                    architecture.getRestControllers(),

                    architecture.getControllers(),

                    architecture.getServices(),

                    architecture.getRepositories(),

                    architecture.getEntities(),

                    architecture.getConfigurations(),

                    architecture.getComponents(),

                    architecture.getExceptionHandlers(),

                    architecture.getOtherClasses()
            );


    return ollamaService.generateResponse(prompt);
}

public ProjectFileAnalysisResponse analyzeProjectFile( 
        GitHubFileContentResponse file 
) { 
 
    String content = file.getContent(); 
 
    String packageName = 
            extractPackageName(content); 
 
    String className = 
            extractClassName(content); 
 
    List<String> annotations = 
            extractAnnotations(content); 
 
    List<String> imports = 
            extractImports(content); 
 
    String parentClass = 
            extractParentClass(content); 
 
    List<String> implementedInterfaces = 
            extractImplementedInterfaces(content); 
 
    List<String> dependencies = 
            extractDependencies(content); 
 
    String componentType = 
            extractComponentType(content); 
 
    return new ProjectFileAnalysisResponse( 
            file.getName(), 
            file.getPath(), 
            packageName, 
            className, 
            componentType, 
            annotations, 
            imports, 
            parentClass, 
            implementedInterfaces, 
            dependencies 
    ); 
}
private Map<String, String> extractImportsMap(
        String content
) {

    Map<String, String> imports =
            new HashMap<>();

    Pattern importPattern =
            Pattern.compile(
                    "import\\s+([\\w.]+)\\s*;"
            );

    Matcher matcher =
            importPattern.matcher(content);

    while (matcher.find()) {

        String fullClassName =
                matcher.group(1);

        int lastDot =
                fullClassName.lastIndexOf(".");

        if (lastDot == -1) {
            continue;
        }

        String simpleClassName =
                fullClassName.substring(lastDot + 1);

        imports.put(
                simpleClassName,
                fullClassName
        );
    }

    return imports;
}
private List<String> extractAnnotations(String content) {

    List<String> annotations = new ArrayList<>();

    Pattern pattern = Pattern.compile(
            "@([A-Za-z_][A-Za-z0-9_]*)"
    );

    Matcher matcher = pattern.matcher(content);

    while (matcher.find()) {

        String annotation = matcher.group(1);

        if (!annotations.contains(annotation)) {
            annotations.add(annotation);
        }
    }

    return annotations;
}
private List<String> extractImplementedInterfaces(
        String content
) {

    List<String> interfaces = new ArrayList<>();

    Pattern pattern = Pattern.compile(
            "\\b(?:class|interface)\\s+\\w+"
                    + "(?:\\s+extends\\s+[\\w<>.,\\s]+)?"
                    + "\\s+implements\\s+([^\\{]+)"
    );

    Matcher matcher = pattern.matcher(content);

    if (matcher.find()) {

        String interfacePart = matcher.group(1);

        String[] interfaceArray =
                interfacePart.split(",");

        for (String interfaceName : interfaceArray) {

            String cleanedName =
                    interfaceName
                            .trim()
                            .replaceAll("<.*>", "");

            if (!cleanedName.isBlank()) {
                interfaces.add(cleanedName);
            }
        }
    }

    return interfaces;
}
public List<ProjectFileAnalysisResponse> analyzeRepositoryFiles(
        String githubUrl
) {

    List<GitHubFileContentResponse> javaFiles =
            gitHubService.getAllJavaFilesWithContent(
                    githubUrl
            );

    return javaFiles.stream()
            .map(this::analyzeProjectFile)
            .toList();
}
private Map<String, String> buildClassNameMap(
        List<GitHubFileContentResponse> javaFiles
) {

    Map<String, String> classNameMap =
            new HashMap<>();

    for (GitHubFileContentResponse file : javaFiles) {

        String content = file.getContent();

        String packageName =
                extractPackageName(content);

        String className =
                extractClassName(content);

        if (packageName != null
                && className != null
                && !className.isBlank()) {

            String fullClassName =
                    packageName + "." + className;

            classNameMap.put(
                    className,
                    fullClassName
            );
        }
    }

    return classNameMap;
}
private String resolveProjectClass(

        String simpleClassName,

        String currentClassKey,

        Map<String, String> importedClasses,

        Map<String, String> projectClasses
) {


    /*
     * =====================================
     * 1. CHECK PROJECT CLASS MAP
     * =====================================
     */

    String projectClassKey =
            projectClasses.get(simpleClassName);


    if (projectClassKey == null) {

        return null;
    }


    /*
     * Do not allow self dependency
     */

    if (projectClassKey.equals(currentClassKey)) {

        return null;
    }


    /*
     * =====================================
     * 2. CHECK IMPORT
     * =====================================
     */

    String importedClassKey =
            importedClasses.get(simpleClassName);


    /*
     * If explicitly imported,
     * make sure it matches project class.
     */

    if (importedClassKey != null) {

        if (projectClassKey.equals(importedClassKey)) {

            return projectClassKey;
        }

        return null;
    }


    /*
     * =====================================
     * 3. SAME PACKAGE DEPENDENCY
     * =====================================
     *
     * Example:
     *
     * OwnerController
     * OwnerRepository
     *
     * Both belong to:
     *
     * org.springframework.samples.petclinic.owner
     */

    int lastDot =
            currentClassKey.lastIndexOf(".");


    if (lastDot == -1) {

        return null;
    }


    String currentPackage =
            currentClassKey.substring(
                    0,
                    lastDot
            );


    if (projectClassKey.startsWith(
            currentPackage + "."
    )) {

        return projectClassKey;
    }


    /*
     * =====================================
     * 4. PROJECT CLASS EXISTS
     * =====================================
     *
     * Allow imported/project dependency.
     */

    return projectClassKey;
}
}