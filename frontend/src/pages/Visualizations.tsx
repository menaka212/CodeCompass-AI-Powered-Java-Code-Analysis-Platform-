import { useEffect, useMemo, useState } from "react";
import dagre from "dagre";
import {
    ReactFlow,
    Background,
    Controls,
    MarkerType,
    type Node,
    type Edge,
} from "@xyflow/react";
import {
    ResponsiveContainer,
    BarChart,
    Bar,
    XAxis,
    YAxis,
    Tooltip,
    CartesianGrid,
} from "recharts";

import "@xyflow/react/dist/style.css";
import {
    getArchitecture,
    getDependencies,
    getApiCallFlows,
} from "../services/api";

interface ArchitectureData {
    applications: string[];
    restControllers: string[];
    controllers: string[];
    services: string[];
    repositories: string[];
    entities: string[];
    configurations: string[];
    components: string[];
    exceptionHandlers: string[];
    otherClasses: string[];
}

interface Dependency {
    sourceClass: string;
    targetClass: string;
}

interface CallFlowNode {
    className: string;
    methodName: string;
    classType: string;
    databaseOperation: string;
    nextCalls: CallFlowNode[];
}

interface ApiCallFlow {
    httpMethod: string;
    endpoint: string;
    callFlow: CallFlowNode;
}

type ViewType = "architecture" | "dependencies" | "calls" | "metrics";

const architectureGroups: {
    key: keyof ArchitectureData;
    label: string;
}[] = [
    { key: "applications", label: "Applications" },
    { key: "restControllers", label: "REST Controllers" },
    { key: "controllers", label: "Controllers" },
    { key: "services", label: "Services" },
    { key: "repositories", label: "Repositories" },
    { key: "entities", label: "Entities" },
    { key: "configurations", label: "Configurations" },
    { key: "components", label: "Components" },
    { key: "exceptionHandlers", label: "Exception Handlers" },
    { key: "otherClasses", label: "Other Classes" },
];

const emptyArchitecture: ArchitectureData = {
    applications: [],
    restControllers: [],
    controllers: [],
    services: [],
    repositories: [],
    entities: [],
    configurations: [],
    components: [],
    exceptionHandlers: [],
    otherClasses: [],
};


/**
 * Calculates positions for React Flow nodes using Dagre.
 * The returned positions are top-left coordinates, as expected by React Flow.
 */
const getLayoutedElements = (
    nodes: Node[],
    edges: Edge[],
    direction: "TB" | "LR" = "TB"
): { nodes: Node[]; edges: Edge[] } => {
    const graph = new dagre.graphlib.Graph();

    graph.setDefaultEdgeLabel(() => ({}));
    graph.setGraph({
        rankdir: direction,
        ranker: "network-simplex",
        // Give nodes and ranks more room so labels and connecting edges
        // are less crowded in larger dependency graphs.
        nodesep: 90,
        edgesep: 35,
        ranksep: 115,
        marginx: 45,
        marginy: 45,
    });

    nodes.forEach((node) => {
        const width = Number(node.style?.width) || 280;
        const height =
            Number(node.style?.height) ||
            Number(node.style?.minHeight) ||
            84;

        graph.setNode(node.id, { width, height });
    });

    edges.forEach((edge) => {
        graph.setEdge(edge.source, edge.target);
    });

    dagre.layout(graph);

    const layoutedNodes = nodes.map((node) => {
        const layout = graph.node(node.id);
        const width = Number(node.style?.width) || 280;
        const height =
            Number(node.style?.height) ||
            Number(node.style?.minHeight) ||
            84;

        return {
            ...node,
            position: {
                x: layout.x - width / 2,
                y: layout.y - height / 2,
            },
        };
    });

    return { nodes: layoutedNodes, edges };
};

function Visualizations() {
    const [view, setView] = useState<ViewType>("architecture");
    const [architecture, setArchitecture] =
        useState<ArchitectureData | null>(null);
    const [dependencies, setDependencies] = useState<Dependency[]>([]);
    const [callFlows, setCallFlows] = useState<ApiCallFlow[]>([]);
    const [selectedCallFlowIndex, setSelectedCallFlowIndex] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const githubUrl = localStorage.getItem("githubUrl");

        if (!githubUrl) {
            setError(
                "Please analyze a GitHub repository from the Dashboard first."
            );
            setLoading(false);
            return;
        }

        let cancelled = false;

        async function loadVisualizations() {
            try {
                setLoading(true);
                setError("");

                const [architectureData, dependencyData, callFlowData] =
                    await Promise.all([
                        getArchitecture(githubUrl!),
                        getDependencies(githubUrl!),
                        getApiCallFlows(githubUrl!),
                    ]);

                if (cancelled) return;

                setArchitecture({
                    ...emptyArchitecture,
                    ...architectureData,
                });
                setDependencies(dependencyData ?? []);
                setCallFlows(callFlowData ?? []);
            } catch (err) {
                console.error("Visualization loading failed:", err);
                if (!cancelled) {
                    setError(
                        "Unable to load visualizations. Check your repository URL and backend."
                    );
                }
            } finally {
                if (!cancelled) setLoading(false);
            }
        }

        loadVisualizations();

        return () => {
            cancelled = true;
        };
    }, []);

    const architectureNodes = useMemo<Node[]>(() => {
        if (!architecture) return [];

        const nodes: Node[] = [];
        // Use three readable category columns instead of spreading every
        // category across one very wide row. Each category gets a clear header.
        const columnWidth = 360;
        const categoryGap = 44;
        const headerHeight = 54;
        const nodeHeight = 76;
        const nodeGap = 18;
        const columnY = [30, 30, 30];

        architectureGroups.forEach((group, groupIndex) => {
            const column = groupIndex % 3;
            const classes = architecture[group.key] ?? [];
            const blockY = columnY[column];
            const headerId = `arch-group-${group.key}`;

            nodes.push({
                id: headerId,
                position: { x: column * columnWidth, y: blockY },
                data: {
                    label: (
                        <div style={{ textAlign: "left", fontWeight: 700 }}>
                            <span>{group.label}</span>
                            <span style={{ marginLeft: 8, opacity: 0.75 }}>
                                {classes.length}
                            </span>
                        </div>
                    ),
                },
                draggable: false,
                selectable: false,
                style: {
                    width: 300,
                    height: headerHeight,
                    padding: "14px 16px",
                    borderRadius: 10,
                    border: "1px solid #c7d2fe",
                    background: "#eef2ff",
                    color: "#3730a3",
                    fontSize: 15,
                    boxShadow: "0 2px 5px rgba(15, 23, 42, 0.04)",
                },
            });

            classes.forEach((className, row) => {
                const shortName = className.split(".").pop() || className;
                nodes.push({
                    id: `arch-${group.key}-${row}`,
                    position: {
                        x: column * columnWidth,
                        y: blockY + headerHeight + 14 + row * (nodeHeight + nodeGap),
                    },
                    data: {
                        label: (
                            <div title={className} style={{ textAlign: "left", overflowWrap: "anywhere" }}>
                                <strong>{shortName}</strong>
                                {shortName !== className && (
                                    <div className="viz-node-subtitle" title={className}>
                                        {className}
                                    </div>
                                )}
                            </div>
                        ),
                    },
                    draggable: false,
                    style: {
                        width: 300,
                        height: nodeHeight,
                        padding: "12px 16px",
                        borderRadius: 10,
                        border: "1px solid #dbe3ef",
                        background: "#ffffff",
                        color: "#172033",
                        fontSize: 13,
                        lineHeight: 1.4,
                        boxShadow: "0 2px 5px rgba(15, 23, 42, 0.04)",
                    },
                });
            });

            columnY[column] += headerHeight + 14 + classes.length * (nodeHeight + nodeGap) + categoryGap;
        });

        return nodes;
    }, [architecture]);

    const dependencyGraph = useMemo(
        () => {
            const classNames = Array.from(
                new Set(
                    dependencies.flatMap((dependency) => [
                        dependency.sourceClass,
                        dependency.targetClass,
                    ])
                )
            );

            const nodes: Node[] = classNames.map((className) => {
                // Keep the complete class name available in the tooltip,
                // but show only the short class name inside the node.
                const shortClassName =
                    className.split(".").pop() || className;

                return {
                    id: className,
                    position: { x: 0, y: 0 },
                    data: {
                        label: (
                            <div
                                title={className}
                                style={{
                                    overflow: "hidden",
                                    overflowWrap: "anywhere",
                                    textAlign: "center",
                                }}
                            >
                                <strong>{shortClassName}</strong>
                            </div>
                        ),
                    },
                    style: {
                        width: 230,
                        height: 72,
                        padding: 14,
                        borderRadius: 12,
                        background: "#eff6ff",
                        border: "1px solid #93c5fd",
                        color: "#1e3a8a",
                        fontSize: 14,
                        lineHeight: 1.4,
                        boxShadow: "0 2px 6px rgba(15, 23, 42, 0.06)",
                    },
                };
            });

            const edges: Edge[] = dependencies.map((dependency, index) => ({
                id: `dependency-${index}`,
                source: dependency.sourceClass,
                target: dependency.targetClass,
                type: "smoothstep",
                markerEnd: { type: MarkerType.ArrowClosed },
                style: { stroke: "#64748b", strokeWidth: 1.5 },
            }));

            // Use Dagre to position related classes in layers instead of a fixed grid.
            return getLayoutedElements(nodes, edges, "TB");
        },
        [dependencies]
    );

    const callGraph = useMemo(() => {
        const flow = callFlows[selectedCallFlowIndex];
        if (!flow) {
            return { nodes: [] as Node[], edges: [] as Edge[] };
        }

        const nodes: Node[] = [];
        const edges: Edge[] = [];
        const endpointId = `endpoint-${selectedCallFlowIndex}`;

        nodes.push({
            id: endpointId,
            position: { x: 0, y: 0 },
            data: {
                label: (
                    <div className="call-flow-node-label">
                        <strong>{flow.httpMethod}</strong>
                        <div>{flow.endpoint}</div>
                    </div>
                ),
            },
            style: {
                width: 300,
                minHeight: 76,
                padding: 14,
                borderRadius: 10,
                background: "#f3e8ff",
                border: "1px solid #c4b5fd",
                color: "#5b21b6",
                fontSize: 14,
                textAlign: "center" as const,
            },
        });

        const addCallNode = (
            call: CallFlowNode,
            parentId: string,
            path: string
        ) => {
            const id = `call-${selectedCallFlowIndex}-${path}`;
            const shortClassName =
                call.className.split(".").pop() || call.className;

            nodes.push({
                id,
                position: { x: 0, y: 0 },
                data: {
                    label: (
                        <div
                            className="call-flow-node-label"
                            title={`${call.className}.${call.methodName}`}
                        >
                            <strong>{shortClassName}</strong>
                            <div>{call.methodName}()</div>
                            <div className="viz-node-subtitle">
                                {call.classType}
                                {call.databaseOperation &&
                                call.databaseOperation !== "NONE"
                                    ? ` · ${call.databaseOperation}`
                                    : ""}
                            </div>
                        </div>
                    ),
                },
                style: {
                    width: 280,
                    minHeight: 84,
                    padding: 14,
                    borderRadius: 10,
                    background: "#ffffff",
                    border: "1px solid #cbd5e1",
                    color: "#172033",
                    fontSize: 13,
                    textAlign: "center" as const,
                },
            });

            edges.push({
                id: `edge-${parentId}-${id}`,
                source: parentId,
                target: id,
                type: "smoothstep",
                markerEnd: { type: MarkerType.ArrowClosed },
                style: { stroke: "#94a3b8", strokeWidth: 1.6 },
            });

            (call.nextCalls ?? []).forEach((nextCall, index) => {
                addCallNode(nextCall, id, `${path}-${index}`);
            });
        };

        if (flow.callFlow) {
            addCallNode(flow.callFlow, endpointId, "root");
        }

        // Arrange the selected API call tree vertically, preserving parent-child edges.
        return getLayoutedElements(nodes, edges, "TB");
    }, [callFlows, selectedCallFlowIndex]);

    const metricData = useMemo(() => {
        const classCount = architecture
            ? architectureGroups.reduce(
                  (total, group) =>
                      total + (architecture[group.key] ?? []).length,
                  0
              )
            : 0;

        const countCalls = (node: CallFlowNode): number =>
            1 + (node.nextCalls ?? []).reduce(
                (total, child) => total + countCalls(child),
                0
            );

        const methodCallCount = callFlows.reduce(
            (total, flow) =>
                total + (flow.callFlow ? countCalls(flow.callFlow) : 0),
            0
        );

        return [
            { name: "Classes", count: classCount },
            { name: "Dependencies", count: dependencies.length },
            { name: "API endpoints", count: callFlows.length },
            { name: "Call nodes", count: methodCallCount },
        ];
    }, [architecture, dependencies, callFlows]);

    const totalClasses = metricData[0].count;

    if (loading) {
        return (
            <div className="page-container">
                <div className="page-header">
                    <p className="eyebrow">CODE INSIGHTS</p>
                    <h1>Visualizations</h1>
                    <p>Analyzing your repository data...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="page-container">
                <div className="page-header">
                    <p className="eyebrow">CODE INSIGHTS</p>
                    <h1>Visualizations</h1>
                </div>
                <div className="error-message">{error}</div>
            </div>
        );
    }

    const views: { id: ViewType; label: string }[] = [
        { id: "architecture", label: "Architecture Map" },
        { id: "dependencies", label: "Dependency Graph" },
        { id: "calls", label: "Method Call Flow" },
        { id: "metrics", label: "Code Metrics" },
    ];

    return (
        <div className="page-container visualizations-page">
            <div className="page-header">
                <p className="eyebrow">CODE INSIGHTS</p>
                <h1>Repository Visualizations</h1>
                <p>
                    Explore your project's structure, class relationships,
                    request flows, and code metrics.
                </p>
            </div>

            <div className="viz-summary">
                {metricData.map((metric) => (
                    <div className="viz-summary-card" key={metric.name}>
                        <span>{metric.name}</span>
                        <strong>{metric.count}</strong>
                    </div>
                ))}
            </div>

            <div className="viz-tabs">
                {views.map((item) => (
                    <button
                        key={item.id}
                        className={
                            view === item.id
                                ? "viz-tab active"
                                : "viz-tab"
                        }
                        onClick={() => setView(item.id)}
                    >
                        {item.label}
                    </button>
                ))}
            </div>

            <section className="viz-panel">
                {view === "architecture" && (
                    <>
                        <h2>Architecture Map</h2>
                        <p className="viz-description">
                            Classes are grouped by the categories detected
                            by your backend. No dependency edges are inferred
                            in this view.
                        </p>

                        {totalClasses === 0 ? (
                            <p>No classes were returned for this repository.</p>
                        ) : (
                            <div className="viz-canvas" style={{ height: 720 }}>
                                <ReactFlow
                                    nodes={architectureNodes}
                                    edges={[]}
                                    fitView
                                    fitViewOptions={{ padding: 0.12, maxZoom: 1.0 }}
                                    minZoom={0.15}
                                    nodesDraggable={false}
                                >
                                    <Background />
                                    <Controls />
                                </ReactFlow>
                            </div>
                        )}
                    </>
                )}

                {view === "dependencies" && (
                    <>
                        <h2>Dependency Graph</h2>
                        <p className="viz-description">
                            Arrows show the source-to-target relationships
                            returned by your backend.
                        </p>

                        {dependencyGraph.nodes.length === 0 ? (
                            <p>No dependency relationships found.</p>
                        ) : (
                            <div className="viz-canvas">
                                <ReactFlow
                                    nodes={dependencyGraph.nodes}
                                    edges={dependencyGraph.edges}
                                    fitView
                                    fitViewOptions={{ padding: 0.18, maxZoom: 1.15 }}
                                    minZoom={0.1}
                                    maxZoom={2}
                                >
                                    <Background />
                                    <Controls />
                                </ReactFlow>
                            </div>
                        )}
                    </>
                )}

                {view === "calls" && (
                    <>
                        <h2>Method Call Flow</h2>
                        <p className="viz-description">
                            Select an API endpoint to inspect its call-flow tree.
                        </p>

                        {callFlows.length > 0 && (
                            <div className="viz-endpoint-selector">
                                <label htmlFor="call-flow-endpoint">
                                    API endpoint
                                </label>
                                <select
                                    id="call-flow-endpoint"
                                    value={selectedCallFlowIndex}
                                    onChange={(event) =>
                                        setSelectedCallFlowIndex(Number(event.target.value))
                                    }
                                >
                                    {callFlows.map((flow, index) => (
                                        <option
                                            key={`${flow.httpMethod}-${flow.endpoint}-${index}`}
                                            value={index}
                                        >
                                            {flow.httpMethod} {flow.endpoint}
                                        </option>
                                    ))}
                                </select>
                            </div>
                        )}

                        {callGraph.nodes.length === 0 ? (
                            <p>No API call flows found.</p>
                        ) : (
                            <div className="viz-canvas">
                                <ReactFlow
                                    key={`call-flow-${selectedCallFlowIndex}`}
                                    nodes={callGraph.nodes}
                                    edges={callGraph.edges}
                                    fitView
                                    fitViewOptions={{ padding: 0.12, maxZoom: 1.35 }}
                                    minZoom={0.15}
                                    maxZoom={2.5}
                                >
                                    <Background />
                                    <Controls />
                                </ReactFlow>
                            </div>
                        )}
                    </>
                )}

                {view === "metrics" && (
                    <>
                        <h2>Codebase Metrics</h2>
                        <p className="viz-description">
                            Counts are calculated from the architecture,
                            dependency, and call-flow API responses.
                        </p>

                        <div className="viz-chart">
                            <ResponsiveContainer width="100%" height="100%">
                                <BarChart
                                    data={metricData}
                                    margin={{
                                        top: 20,
                                        right: 20,
                                        left: 5,
                                        bottom: 15,
                                    }}
                                >
                                    <CartesianGrid strokeDasharray="3 3" />
                                    <XAxis
                                        dataKey="name"
                                        interval={0}
                                        angle={-10}
                                        textAnchor="end"
                                        height={60}
                                    />
                                    <YAxis allowDecimals={false} />
                                    <Tooltip />
                                    <Bar
                                        dataKey="count"
                                        fill="#647de8"
                                        radius={[6, 6, 0, 0]}
                                    />
                                </BarChart>
                            </ResponsiveContainer>
                        </div>
                    </>
                )}
            </section>
        </div>
    );
}

export default Visualizations;
