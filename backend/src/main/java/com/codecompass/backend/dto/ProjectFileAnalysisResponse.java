package com.codecompass.backend.dto;

import java.util.List;

public class ProjectFileAnalysisResponse {

    private String fileName;
    private String filePath;

    private String packageName;
    private String className;

    private String componentType;

    private List<String> annotations;
    private List<String> imports;

    private String parentClass;

    private List<String> implementedInterfaces;

    private List<String> dependencies;

    public ProjectFileAnalysisResponse(
            String fileName,
            String filePath,
            String packageName,
            String className,
            String componentType,
            List<String> annotations,
            List<String> imports,
            String parentClass,
            List<String> implementedInterfaces,
            List<String> dependencies
    ) {

        this.fileName = fileName;
        this.filePath = filePath;

        this.packageName = packageName;
        this.className = className;

        this.componentType = componentType;

        this.annotations = annotations;
        this.imports = imports;

        this.parentClass = parentClass;

        this.implementedInterfaces = implementedInterfaces;

        this.dependencies = dependencies;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public String getComponentType() {
        return componentType;
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public List<String> getImports() {
        return imports;
    }

    public String getParentClass() {
        return parentClass;
    }

    public List<String> getImplementedInterfaces() {
        return implementedInterfaces;
    }

    public List<String> getDependencies() {
        return dependencies;
    }
}