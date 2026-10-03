import { useState } from "react";
import { getArchitecture } from "../services/api";


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

function Architecture() {

    const [githubUrl, setGithubUrl] = useState(
    localStorage.getItem("githubUrl") || ""
);
    const [architecture, setArchitecture] =
        useState<ArchitectureData | null>(null);

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const handleAnalyze = async () => {

        if (!githubUrl.trim()) {
            setError("Please enter a GitHub repository URL.");
            return;
        }

        try {

            setLoading(true);
            setError("");

            const data = await getArchitecture(githubUrl);

            console.log("Architecture result:", data);

            setArchitecture(data);

        } catch (err) {

            console.error(err);

            setError("Unable to analyze the architecture.");

        } finally {

            setLoading(false);

        }

    };

    return (

        <div className="architecture-page">

            {/* PAGE HEADER */}

            <section className="architecture-hero">

                <p className="architecture-eyebrow">
                    CODE STRUCTURE
                </p>

                <h1>
                    Project <span>Architecture</span>
                </h1>

                <p>
                    Analyze the structure of your repository and
                    understand how different components are organized.
                </p>

            </section>


            {/* ANALYZE CARD */}

            <section className="architecture-analyze-card">

                <h2>Analyze Architecture</h2>

                <p>
                    Enter a GitHub repository URL to visualize
                    its application structure.
                </p>


                <div className="architecture-input-group">

                    <input
                        type="text"
                        value={githubUrl}
                        onChange={(event) =>
                            setGithubUrl(event.target.value)
                        }
                        placeholder="https://github.com/username/repository"
                    />

                    <button
                        onClick={handleAnalyze}
                        disabled={loading}
                    >

                        {loading
                            ? "Analyzing..."
                            : "Analyze Architecture"}

                    </button>

                </div>


                {error && (

                    <p className="architecture-error">
                        {error}
                    </p>

                )}

            </section>


            {/* RESULTS */}

            {architecture && (

                <section className="architecture-results">

                    <ArchitectureCard
                        title="Applications"
                        icon="⬡"
                        description="Spring Boot application entry points and bootstrap classes."
                        items={architecture.applications}
                    />

                    <ArchitectureCard
                        title="REST Controllers"
                        icon="⇄"
                        description="Handles REST API requests using @RestController."
                        items={architecture.restControllers}
                    />

                    <ArchitectureCard
                        title="Controllers"
                        icon="◈"
                        description="Handles MVC HTTP requests using @Controller."
                        items={architecture.controllers}
                    />

                    <ArchitectureCard
                        title="Services"
                        icon="⚙"
                        description="Contains business logic and application services."
                        items={architecture.services}
                    />

                    <ArchitectureCard
                        title="Repositories"
                        icon="▣"
                        description="Handles database access and data operations."
                        items={architecture.repositories}
                    />

                    <ArchitectureCard
                        title="Entities"
                        icon="◇"
                        description="Represents application domain models and database entities."
                        items={architecture.entities}
                    />

                    <ArchitectureCard
                        title="Configurations"
                        icon="⚙"
                        description="Spring configuration classes that set up application beans."
                        items={architecture.configurations}
                    />

                    <ArchitectureCard
                        title="Components"
                        icon="▣"
                        description="Generic Spring components used across the application."
                        items={architecture.components}
                    />

                    <ArchitectureCard
                        title="Exception Handlers"
                        icon="⚠"
                        description="Global exception handling and error response classes."
                        items={architecture.exceptionHandlers}
                    />

                    <ArchitectureCard
                        title="Other Classes"
                        icon="◫"
                        description="Additional application classes and supporting components."
                        items={architecture.otherClasses}
                        large
                    />

                </section>

            )}

        </div>

    );

}


/* =====================================
   REUSABLE ARCHITECTURE CARD
===================================== */

interface ArchitectureCardProps {

    title: string;
    icon: string;
    description: string;
    items: string[];
    large?: boolean;

}

function ArchitectureCard({

    title,
    icon,
    description,
    items,
    large = false

}: ArchitectureCardProps) {

    return (

        <div
            className={
                large
                    ? "architecture-card architecture-card-large"
                    : "architecture-card"
            }
        >

            <div className="architecture-card-header">

                <div className="architecture-card-icon">

                    {icon}

                </div>


                <div>

                    <h3>{title}</h3>

                    <p>{description}</p>

                </div>

            </div>


            <div className="architecture-count">

                {(items ?? []).length} Classes

            </div>


            <div className="architecture-items">

                {(items ?? []).map((item) => (

                    <div
                        className="architecture-item"
                        key={item}
                    >

                        {item}

                    </div>

                ))}

            </div>

        </div>

    );

}

export default Architecture;