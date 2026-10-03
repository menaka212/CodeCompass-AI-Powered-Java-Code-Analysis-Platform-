package com.codecompass.backend.dto;

import java.util.List;
import com.codecompass.backend.enums.DatabaseOperation;
public class CallFlowNodeResponse {
    private String nodeId;
    private String className;
    private String methodName;
    private ClassType classType;
    private List<CallFlowNodeResponse> nextCalls;
    private DatabaseOperation databaseOperation;

    public CallFlowNodeResponse(
            String className,
            String methodName,
            ClassType classType,
            DatabaseOperation databaseOperation,
            List<CallFlowNodeResponse> nextCalls
    ) {
        this.nodeId = className + "#" + methodName;
        this.className = className;
        this.methodName = methodName;
        this.classType = classType;
        this.databaseOperation = databaseOperation;
        this.nextCalls = nextCalls;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public DatabaseOperation getDatabaseOperation() {
        return databaseOperation;
    }

    public ClassType getClassType() {
        return classType;
    }

    public List<CallFlowNodeResponse> getNextCalls() {
        return nextCalls;
    }
}