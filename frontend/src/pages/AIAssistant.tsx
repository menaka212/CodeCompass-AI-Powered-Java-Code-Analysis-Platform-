
import { useState } from "react";
import { askRepositoryAi } from "../services/api";

const suggestedQuestions = [
    "Explain the project architecture",
    "How does JWT authentication work?",
    "Trace getAllEmployees() to the database",
    "Explain the controller-to-service flow",
];

function AIAssistant() {
    const [prompt, setPrompt] = useState("");
    const [answer, setAnswer] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const githubUrl = localStorage.getItem("githubUrl") || "";

    const handleAskAi = async (question = prompt) => {
        if (!question.trim()) {
            setError("Please enter a question.");
            return;
        }

        if (!githubUrl.trim()) {
            setError(
                "No repository selected. Please analyze a repository from the Dashboard first."
            );
            return;
        }

        setPrompt(question);
        setLoading(true);
        setError("");
        setAnswer("");

        try {
            const result = await askRepositoryAi(
                githubUrl,
                question.trim()
            );

            setAnswer(result);
        } catch (err) {
            setError(
                err instanceof Error
                    ? err.message
                    : "Unable to get an AI response. Please try again."
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="ai-assistant">
            <div className="ai-page-header">
                <div>
                    <p className="ai-eyebrow">CODE INSIGHTS</p>
                    <h1>AI Code Assistant</h1>
                    <p className="ai-description">
                        Ask questions about your repository and
                        understand how your code works.
                    </p>
                </div>

                <span className="ai-status">
                    <span className="ai-status-dot" />
                    Ollama AI
                </span>
            </div>

            <section className="ai-repository-card">
                <div className="ai-repository-icon">⌘</div>

                <div className="ai-repository-details">
                    <span>SELECTED REPOSITORY</span>
                    <strong>
                        {githubUrl
                            ? githubUrl.replace(/\/$/, "").split("/").slice(-2).join("/")
                            : "No repository selected"}
                    </strong>
                    <a
                        href={githubUrl || undefined}
                        target="_blank"
                        rel="noreferrer"
                    >
                        {githubUrl || "Go to Dashboard to select a repository"}
                    </a>
                </div>

                <span className="ai-repository-badge">
                    {githubUrl ? "Connected" : "Not selected"}
                </span>
            </section>

            <section className="ai-question-card">
                <h2>What would you like to understand?</h2>
                <p>
                    Choose a suggested question or ask your own.
                </p>

                <div className="ai-suggestions">
                    {suggestedQuestions.map((question) => (
                        <button
                            key={question}
                            type="button"
                            className="ai-suggestion"
                            onClick={() => {
                                setPrompt(question);
                                setError("");
                            }}
                            disabled={loading}
                        >
                            <span>✦</span>
                            {question}
                        </button>
                    ))}
                </div>

                <label htmlFor="ai-question">Your question</label>

                <textarea
                    id="ai-question"
                    value={prompt}
                    onChange={(event) =>
                        setPrompt(event.target.value)
                    }
                    placeholder="For example: Explain how a request moves from the controller to the database..."
                    rows={5}
                    disabled={loading}
                />

                {error && (
                    <div className="ai-error" role="alert">
                        {error}
                    </div>
                )}

                <div className="ai-question-actions">
                    <span className="ai-hint">
                        Answers are generated from your selected repository.
                    </span>

                    <button
                        type="button"
                        className="ai-ask-button"
                        onClick={() => handleAskAi()}
                        disabled={loading || !prompt.trim()}
                    >
                        {loading ? (
                            <>
                                <span className="ai-spinner" />
                                Analyzing...
                            </>
                        ) : (
                            <>✦ Ask AI</>
                        )}
                    </button>
                </div>
            </section>

            {loading && (
                <section className="ai-loading-card">
                    <span className="ai-spinner ai-spinner-large" />
                    <div>
                        <strong>Analyzing your code...</strong>
                        <p>
                            The AI is preparing an answer. This may
                            take a little while on your local machine.
                        </p>
                    </div>
                </section>
            )}

            {answer && !loading && (
                <section className="ai-response-card">
                    <div className="ai-response-header">
                        <div>
                            <span className="ai-eyebrow">
                                GENERATED RESPONSE
                            </span>
                            <h2>AI Explanation</h2>
                        </div>

                        <button
                            type="button"
                            className="ai-clear-button"
                            onClick={() => {
                                setAnswer("");
                                setPrompt("");
                                setError("");
                            }}
                        >
                            Clear answer
                        </button>
                    </div>

                    <div className="ai-response-content">
                        <pre>{answer}</pre>
                    </div>

                    <p className="ai-response-note">
                        Verify important method names and execution
                        paths against the source code.
                    </p>
                </section>
            )}
        </div>
    );
}

export default AIAssistant;
