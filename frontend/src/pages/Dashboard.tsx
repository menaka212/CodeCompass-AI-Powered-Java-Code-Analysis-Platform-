import { useEffect, useState } from "react";
import { getGitHubRepository } from "../services/api";

interface RepositoryDetails {
    name?: string;
    description?: string;
    language?: string;
    url?: string;
}

interface RecentRepository {
    name: string;
    url: string;
    analyzedAt: string;
}

function Dashboard() {

    const [githubUrl, setGithubUrl] = useState("");
    useEffect(() => {
    const savedUrl = localStorage.getItem("githubUrl");

    if (savedUrl) {
        setGithubUrl(savedUrl);
    }
    }, []);
    const [loading, setLoading] = useState(false);

    const [error, setError] = useState("");

    const [analysisResult, setAnalysisResult] =
        useState<any>(null);

    const [repository, setRepository] =
        useState<RepositoryDetails | null>(null);


    // ==========================================
    // LOAD RECENT REPOSITORIES
    // ==========================================

    const [recentRepositories, setRecentRepositories] =
        useState<RecentRepository[]>(() => {

            const savedRepositories =
                localStorage.getItem("recentRepositories");

            if (!savedRepositories) {
                return [];
            }

            try {
                return JSON.parse(savedRepositories);
            } catch {
                return [];
            }

        });


    // ==========================================
    // GET REPOSITORY NAME FROM URL
    // ==========================================

    const getRepositoryName = (url: string) => {

        const cleanUrl = url.replace(/\/$/, "");

        const parts = cleanUrl.split("/");

        return parts[parts.length - 1];

    };


    // ==========================================
    // SAVE RECENT REPOSITORY
    // ==========================================

    const saveRecentRepository = (url: string) => {

        const repositoryName =
            getRepositoryName(url);


        const newRepository: RecentRepository = {

            name: repositoryName,

            url: url,

            analyzedAt:
                new Date().toLocaleString(),

        };


        setRecentRepositories(
            (previousRepositories) => {

                // Remove duplicate repository

                const filteredRepositories =
                    previousRepositories.filter(
                        (repository) =>
                            repository.url !== url
                    );


                // Add newest repository at the top

                const updatedRepositories = [

                    newRepository,

                    ...filteredRepositories,

                ].slice(0, 5);


                // Save to localStorage

                localStorage.setItem(

                    "recentRepositories",

                    JSON.stringify(updatedRepositories)

                );


                return updatedRepositories;

            }

        );

    };


    // ==========================================
    // ANALYZE REPOSITORY
    // ==========================================

    const analyzeRepository = async (
        repositoryUrl: string
    ) => {

        if (!repositoryUrl.trim()) {

            setError(
                "Please enter a GitHub repository URL."
            );

            return;

        }


        try {

            setLoading(true);

            setError("");

            setRepository(null);

            setAnalysisResult(null);


            const data =
                await getGitHubRepository(
                    repositoryUrl
                );


            console.log(
                "Repository analysis result:",
                data
            );


            setAnalysisResult(data);


            // If backend returns repository details

            if (
                data?.repository ||
                data?.name
            ) {

                setRepository(
                    data.repository || data
                );

            }


            // Save current repository URL

            localStorage.setItem(
                "githubUrl",
                repositoryUrl
            );


            // Save recent repository

            saveRecentRepository(
                repositoryUrl
            );


        } catch (err) {

            console.error(err);

            setError(
                "Unable to analyze the repository."
            );

        } finally {

            setLoading(false);

        }

    };


    // ==========================================
    // ANALYZE BUTTON
    // ==========================================

    const handleAnalyze = async () => {

        await analyzeRepository(
            githubUrl
        );

    };


    // ==========================================
    // USE RECENT REPOSITORY
    // ==========================================

    const handleRecentRepository = async (
        repositoryUrl: string
    ) => {

        setGithubUrl(repositoryUrl);

        await analyzeRepository(
            repositoryUrl
        );

    };


    return (

        <div className="dashboard">


            {/* ======================================
                WELCOME SECTION
            ====================================== */}

            <section className="welcome-section">

                <div>

                    <p className="eyebrow">
                        CODE ANALYSIS PLATFORM
                    </p>


                    <h1>
                        Welcome to{" "}

                        <span>
                            CodeCompass
                        </span>
                    </h1>


                    <p className="welcome-text">

                        Analyze your GitHub repository and visualize
                        its architecture, dependencies, API flows,
                        and potential impact of code changes.

                    </p>

                </div>

            </section>


            {/* ======================================
                ANALYZE REPOSITORY
            ====================================== */}

            <section className="analyze-card">

                <div className="card-header">

                    <div>

                        <h2>
                            Analyze Repository
                        </h2>


                        <p>

                            Enter a GitHub repository URL to begin
                            analyzing the codebase.

                        </p>

                    </div>

                </div>


                <div className="repository-input">

                    <label htmlFor="github-url">

                        GitHub Repository

                    </label>


                    <input

                        id="github-url"

                        type="text"

                        value={githubUrl}

                        onChange={(event) =>
                            setGithubUrl(
                                event.target.value
                            )
                        }

                        placeholder="https://github.com/username/repository"

                    />


                    <button

                        className="analyze-button"

                        onClick={handleAnalyze}

                        disabled={loading}

                    >

                        {loading
                            ? "Analyzing..."
                            : "Analyze Repository"
                        }

                    </button>


                    {error && (

                        <p className="error-message">

                            {error}

                        </p>

                    )}

                </div>

            </section>


            {/* ======================================
                ANALYSIS RESULT
            ====================================== */}

            {analysisResult && (

                <section className="analysis-result">

                    <h2>
                        Repository Analysis Complete 🎉
                    </h2>


                    <p>
                        Your GitHub repository has been
                        successfully analyzed.
                    </p>


                    <div className="result-grid">

                        <div className="result-card">

                            <h3>
                                Repository
                            </h3>


                            <p>
                                {githubUrl}
                            </p>

                        </div>

                    </div>

                </section>

            )}


            {/* ======================================
                REPOSITORY RESULT
            ====================================== */}

            {repository && (

                <section className="repository-result">

                    <h2>
                        Repository Details
                    </h2>


                    <div className="result-grid">


                        <div className="result-item">

                            <span>
                                Repository Name
                            </span>


                            <strong>

                                {repository.name ||
                                    "Not available"
                                }

                            </strong>

                        </div>


                        <div className="result-item">

                            <span>
                                Language
                            </span>


                            <strong>

                                {repository.language ||
                                    "Not available"
                                }

                            </strong>

                        </div>


                        <div className="result-item">

                            <span>
                                Description
                            </span>


                            <strong>

                                {repository.description ||
                                    "No description"
                                }

                            </strong>

                        </div>


                        <div className="result-item">

                            <span>
                                Repository URL
                            </span>


                            <strong>

                                {repository.url ||
                                    githubUrl
                                }

                            </strong>

                        </div>


                    </div>

                </section>

            )}


            {/* ======================================
                RECENT ANALYSES
            ====================================== */}

            {recentRepositories.length > 0 && (

                <section className="recent-repositories">


                    <div className="recent-header">

                        <div>

                            <p className="eyebrow">
                                RECENT ACTIVITY
                            </p>


                            <h2>
                                Recent Analyses
                            </h2>

                        </div>

                    </div>


                    <div className="recent-list">

                        {recentRepositories.map(
                            (repository) => (

                                <div

                                    className="recent-repository-card"

                                    key={repository.url}

                                >


                                    <div className="recent-repository-info">


                                        <div className="recent-icon">

                                            📦

                                        </div>


                                        <div>

                                            <h3>

                                                {repository.name}

                                            </h3>


                                            <p className="recent-url">

                                                {repository.url}

                                            </p>


                                            <p className="recent-time">

                                                Last analyzed:{" "}

                                                {repository.analyzedAt}

                                            </p>

                                        </div>

                                    </div>


                                    <button

                                        className="view-analysis-button"

                                        onClick={() =>
                                            handleRecentRepository(
                                                repository.url
                                            )
                                        }

                                        disabled={loading}

                                    >

                                        {loading
                                            ? "Analyzing..."
                                            : "Use Repository"
                                        }

                                    </button>

                                </div>

                            )

                        )}

                    </div>

                </section>

            )}


            {/* ======================================
                FEATURE CARDS
            ====================================== */}

            <section className="feature-grid">


                <div className="feature-card">

                    <div className="feature-icon">
                        ◇
                    </div>


                    <h3>
                        Architecture
                    </h3>


                    <p>

                        Understand controllers, repositories,
                        entities and project structure.

                    </p>

                </div>


                <div className="feature-card">

                    <div className="feature-icon">
                        ⌘
                    </div>


                    <h3>
                        Dependencies
                    </h3>


                    <p>

                        Explore relationships between classes
                        and project components.

                    </p>

                </div>


                <div className="feature-card">

                    <div className="feature-icon">
                        ⇄
                    </div>


                    <h3>
                        API Call Flows
                    </h3>


                    <p>

                        Follow an API from its controller to
                        repository operations.

                    </p>

                </div>


                <div className="feature-card">

                    <div className="feature-icon">
                        ⚠
                    </div>


                    <h3>
                        Impact Analysis
                    </h3>


                    <p>

                        Discover which methods may be affected
                        by a code change.

                    </p>

                </div>


            </section>


        </div>

    );

}


export default Dashboard;