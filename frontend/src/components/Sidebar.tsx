interface SidebarProps {
    activePage: string;
    setActivePage: (page: string) => void;
}

function Sidebar({
    activePage,
    setActivePage,
}: SidebarProps) {

    return (
        <aside className="sidebar">

            <div className="logo">
                <span className="logo-icon">⚡</span>
                <span>CodeCompass</span>
            </div>

            <div className="sidebar-section">

                <p className="sidebar-title">
                    ANALYSIS
                </p>

                <button
                    className={
                        activePage === "dashboard"
                            ? "nav-item active"
                            : "nav-item"
                    }
                    onClick={() =>
                        setActivePage("dashboard")
                    }
                >
                    <span className="nav-icon">▣</span>
                    <span>Dashboard</span>
                </button>

                <button
                    className={
                        activePage === "architecture"
                            ? "nav-item active"
                            : "nav-item"
                    }
                    onClick={() =>
                        setActivePage("architecture")
                    }
                >
                    <span className="nav-icon">◇</span>
                    <span>Architecture</span>
                </button>

                <button
    className={
        activePage === "dependencies"
            ? "nav-item active"
            : "nav-item"
    }
    onClick={() =>
        setActivePage("dependencies")
    }
>
    <span className="nav-icon">⌘</span>
    <span>Dependencies</span>
</button>

            </div>


            <div className="sidebar-section">

                <p className="sidebar-title">
                    CODE INSIGHTS
                </p>

                <button
    className={
        activePage === "api-explorer"
            ? "nav-item active"
            : "nav-item"
    }
    onClick={() =>
        setActivePage("api-explorer")
    }
>
    <span className="nav-icon">⇄</span>

    <span>
        API Explorer
    </span>
</button>

                <button

    className={
        activePage === "call-flows"
            ? "nav-item active"
            : "nav-item"
    }

    onClick={() =>
        setActivePage("call-flows")
    }

>

    <span className="nav-icon">↗</span>

    <span>Call Flows</span>

</button>

                <button
    className={
        activePage === "impact-analysis"
            ? "nav-item active"
            : "nav-item"
    }
    onClick={() =>
        setActivePage("impact-analysis")
    }
>
    <span className="nav-icon">⚠</span>
    <span>Impact Analysis</span>
</button>

<button
    className={
        activePage === "projects"
            ? "nav-item active"
            : "nav-item"
    }
    onClick={() => setActivePage("projects")}
>
    <span className="nav-icon">▣</span>
    <span>Projects</span>
</button>


<button
    className={
        activePage === "ai-assistant"
            ? "nav-item active"
            : "nav-item"
    }
    onClick={() => setActivePage("ai-assistant")}
>
    <span className="nav-icon">✦</span>
    <span>AI Assistant</span>
</button>
<button
    className={
        activePage === "visualizations"
            ? "nav-item active"
            : "nav-item"
    }
    onClick={() => setActivePage("visualizations")}
>
    <span className="nav-icon">◉</span>
    <span>Visualizations</span>
</button>

            </div>

        </aside>
    );
}

export default Sidebar;