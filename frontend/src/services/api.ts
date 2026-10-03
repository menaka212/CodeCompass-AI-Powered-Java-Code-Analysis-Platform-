
const API_BASE_URL = (
    import.meta.env.VITE_API_BASE_URL || "http://localhost:8080"
).replace(/\/+$/, "");

const PROJECTS_API_URL = `${API_BASE_URL}/api/projects`;

// ======================================================
// Authentication
// ======================================================

function getAuthHeaders(): HeadersInit {
    const token = localStorage.getItem("token");

    if (!token) {
        throw new Error("Please login first");
    }

    return {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
    };
}

// ======================================================
// Common API request helpers
// ======================================================

async function apiFetch(
    url: string,
    options: RequestInit = {}
): Promise<Response> {
    const headers = {
        ...getAuthHeaders(),
        ...(options.headers as Record<string, string> | undefined),
    };

    const response = await fetch(url, {
        ...options,
        headers,
    });

    if (!response.ok) {
        const errorText = await response.text();

        throw new Error(
            errorText || `Request failed: ${response.status}`
        );
    }

    return response;
}

async function requestJson<T = any>(
    url: string,
    options: RequestInit = {}
): Promise<T> {
    const response = await apiFetch(url, options);
    return response.json();
}

async function requestText(
    url: string,
    options: RequestInit = {}
): Promise<string> {
    const response = await apiFetch(url, options);
    return response.text();
}

// ======================================================
// Repository Information
// GET /api/projects/github
// ======================================================

export async function getGitHubRepository(url: string) {
    return requestJson(
        `${PROJECTS_API_URL}/github?url=${encodeURIComponent(url)}`
    );
}

// ======================================================
// Repository Structure
// GET /api/projects/github/structure
// ======================================================

export async function getRepositoryStructure(url: string) {
    return requestJson(
        `${PROJECTS_API_URL}/github/structure?url=${encodeURIComponent(url)}`
    );
}

// ======================================================
// Architecture
// GET /api/projects/github/architecture
// ======================================================

export async function getArchitecture(url: string) {
    return requestJson(
        `${PROJECTS_API_URL}/github/architecture?url=${encodeURIComponent(url)}`
    );
}

// ======================================================
// File Relationships
// GET /api/projects/github/relationships
// ======================================================

export async function getFileRelationships(url: string) {
    return requestJson(
        `${PROJECTS_API_URL}/github/relationships?url=${encodeURIComponent(url)}`
    );
}

// ======================================================
// Dependencies
// GET /api/projects/github/dependencies
// ======================================================

export async function getDependencies(url: string) {
    return requestJson(
        `${PROJECTS_API_URL}/github/dependencies?url=${encodeURIComponent(url)}`
    );
}

// ======================================================
// Dependency Graph
// GET /api/projects/github/dependency-graph
// ======================================================

export async function getDependencyGraph(url: string) {
    return requestJson(
        `${PROJECTS_API_URL}/github/dependency-graph?url=${encodeURIComponent(url)}`
    );
}

// ======================================================
// Method Calls
// GET /api/projects/github/method-calls
// ======================================================

export async function getMethodCalls(url: string) {
    return requestJson(
        `${PROJECTS_API_URL}/github/method-calls?url=${encodeURIComponent(url)}`
    );
}

// ======================================================
// API Map
// GET /api/projects/github/api-map
// ======================================================

export async function getApiMap(url: string) {
    return requestJson(
        `${PROJECTS_API_URL}/github/api-map?url=${encodeURIComponent(url)}`
    );
}

// ======================================================
// API Call Flows
// GET /api/projects/github/api-call-flows
// ======================================================

export async function getApiCallFlows(url: string) {
    return requestJson(
        `${PROJECTS_API_URL}/github/api-call-flows?url=${encodeURIComponent(url)}`
    );
}

// ======================================================
// Call Flow Tree
// GET /api/projects/github/call-flow-tree
// ======================================================

export async function getCallFlowTree(url: string) {
    return requestJson(
        `${PROJECTS_API_URL}/github/call-flow-tree?url=${encodeURIComponent(url)}`
    );
}

// ======================================================
// Call Flow
// GET /api/projects/github/call-flow
// ======================================================

export async function getCallFlow(url: string) {
    return requestJson(
        `${PROJECTS_API_URL}/github/call-flow?url=${encodeURIComponent(url)}`
    );
}

// ======================================================
// Impact Analysis
// GET /api/projects/github/impact-analysis
// ======================================================

export async function getImpactAnalysis(
    url: string,
    targetClass: string,
    targetMethod: string
) {
    const params = new URLSearchParams({
        url,
        targetClass,
        targetMethod,
    });

    return requestJson(
        `${PROJECTS_API_URL}/github/impact-analysis?${params.toString()}`
    );
}

// ======================================================
// Project Interfaces
// ======================================================

export interface Project {
    id: number;
    name: string;
    description: string;
    sourceType: string;
    githubUrl: string;
    createdAt: string;
}

export interface CreateProjectRequest {
    name: string;
    description: string;
    sourceType: string;
    githubUrl: string;
}

export interface UpdateProjectRequest {
    name: string;
    description: string;
    sourceType: string;
    githubUrl: string;
}

// ======================================================
// Project Management
// ======================================================

// GET /api/projects
export async function getMyProjects(): Promise<Project[]> {
    return requestJson<Project[]>(PROJECTS_API_URL);
}

// POST /api/projects
export async function createProject(
    project: CreateProjectRequest
): Promise<Project> {
    return requestJson<Project>(PROJECTS_API_URL, {
        method: "POST",
        body: JSON.stringify(project),
    });
}

// GET /api/projects/{projectId}
export async function getProjectById(
    projectId: number
): Promise<Project> {
    return requestJson<Project>(
        `${PROJECTS_API_URL}/${projectId}`
    );
}

// PUT /api/projects/{projectId}
export async function updateProject(
    projectId: number,
    project: UpdateProjectRequest
): Promise<Project> {
    return requestJson<Project>(
        `${PROJECTS_API_URL}/${projectId}`,
        {
            method: "PUT",
            body: JSON.stringify(project),
        }
    );
}

// DELETE /api/projects/{projectId}
export async function deleteProject(
    projectId: number
): Promise<void> {
    await apiFetch(`${PROJECTS_API_URL}/${projectId}`, {
        method: "DELETE",
    });
}

// ======================================================
// General Ollama AI Assistant
// GET /api/ai/test?prompt=...
// ======================================================

export async function askAi(prompt: string): Promise<string> {
    const params = new URLSearchParams({ prompt });

    return requestText(
        `${API_BASE_URL}/api/ai/test?${params.toString()}`
    );
}

// ======================================================
// Repository AI Assistant
// POST /api/ai/repository-ask
// ======================================================

export async function askRepositoryAi(
    githubUrl: string,
    question: string
): Promise<string> {
    return requestText(
        `${API_BASE_URL}/api/ai/repository-ask`,
        {
            method: "POST",
            body: JSON.stringify({
                githubUrl,
                question,
            }),
        }
    );
}
