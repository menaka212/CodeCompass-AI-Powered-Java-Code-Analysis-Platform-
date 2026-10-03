import { useEffect, useState } from "react";
import { getDependencies } from "../services/api";

interface Dependency {
    sourceClass: string;
    targetClass: string;
}

function Dependencies() {

    const [dependencies, setDependencies] = useState<Dependency[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    useEffect(() => {

        const loadDependencies = async () => {

            const githubUrl = localStorage.getItem("githubUrl");

            if (!githubUrl) {
                setError("Please analyze a GitHub repository first.");
                return;
            }

            try {

                setLoading(true);
                setError("");

                const data = await getDependencies(githubUrl);

                setDependencies(data);

            } catch (err) {

                setError("Unable to load dependencies.");

            } finally {

                setLoading(false);

            }

        };

        loadDependencies();

    }, []);

    return (

        <div className="dependencies-page">

            <section className="page-header">

                <p className="eyebrow">
                    CODE ANALYSIS PLATFORM
                </p>

                <h1>
                    Dependencies
                </h1>

                <p>
                    Explore relationships between classes
                    and project components.
                </p>

            </section>


            {loading && (

                <div className="loading-message">
                    Loading dependencies...
                </div>

            )}


            {error && (

                <div className="error-message">
                    {error}
                </div>

            )}


            {!loading && !error && (

                <section className="dependencies-container">

                    <div className="dependencies-summary">

                        <h2>
                            Dependency Relationships
                        </h2>

                        <p>
                            {dependencies.length} dependencies found
                        </p>

                    </div>


                    <div className="dependencies-list">

                        {dependencies.map((dependency, index) => (

                            <div
                                className="dependency-card"
                                key={index}
                            >

                                <div className="dependency-source">

                                    {dependency.sourceClass}

                                </div>


                                <div className="dependency-arrow">

                                    →

                                </div>


                                <div className="dependency-target">

                                    {dependency.targetClass}

                                </div>

                            </div>

                        ))}

                    </div>

                </section>

            )}

        </div>

    );
}

export default Dependencies;