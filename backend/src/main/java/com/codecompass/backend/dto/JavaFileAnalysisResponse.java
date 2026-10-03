package com.codecompass.backend.dto;

import java.util.List;

public class JavaFileAnalysisResponse {

    private String packageName;
    private String className;
    private List<String> imports;
    private List<String> methods;
    private String componentType;
    private List<String> dependencies;
 
    public JavaFileAnalysisResponse(
            String packageName,
            String className,
            List<String> imports,
            List<String> methods,
            String componentType,
            List<String> dependencies
    ) {
        this.packageName = packageName;
        this.className = className;
        this.imports = imports;
        this.methods = methods;
        this.componentType = componentType;
        this.dependencies = dependencies;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public List<String> getImports() {
        return imports;
    }

    public List<String> getMethods() {
        return methods;
    }

    public String getComponentType() {
        return componentType;
    }

    public List<String> getDependencies() {
        return dependencies;
    }
}