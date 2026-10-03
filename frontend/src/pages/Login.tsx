
import { useState, type FormEvent } from "react";

interface LoginProps {
    onLogin: () => void;
}

const API_BASE_URL = (
    import.meta.env.VITE_API_BASE_URL || "http://localhost:8080"
).replace(/\/+$/, "");

function Login({ onLogin }: LoginProps) {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleLogin = async (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        setError("");

        if (!email.trim() || !password) {
            setError("Please enter your email and password.");
            return;
        }

        setLoading(true);

        try {
            const response = await fetch(
                `${API_BASE_URL}/api/auth/login`,
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({
                        email: email.trim(),
                        password,
                    }),
                }
            );

            if (!response.ok) {
                if (response.status === 401 || response.status === 403) {
                    throw new Error("Invalid email or password.");
                }

                throw new Error(
                    "Unable to log in right now. Please try again."
                );
            }

            const data = await response.json();

            if (!data.token) {
                throw new Error(
                    "Login response did not contain an authentication token."
                );
            }

            localStorage.setItem("token", data.token);
            onLogin();
        } catch (err) {
            setError(
                err instanceof Error
                    ? err.message
                    : "Login failed. Please try again."
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <main className="login-page">
            <div className="login-glow login-glow-one" />
            <div className="login-glow login-glow-two" />

            <section className="login-card">
                <div className="login-brand">
                    <div className="login-brand-icon">⚡</div>
                    <span>CodeCompass</span>
                </div>

                <div className="login-heading">
                    <span className="login-eyebrow">
                        YOUR CODE. UNDERSTOOD.
                    </span>
                    <h1>Welcome back</h1>
                    <p>
                        Sign in to explore your codebase and uncover
                        how your code works.
                    </p>
                </div>

                <form onSubmit={handleLogin}>
                    <div className="login-form-group">
                        <label htmlFor="login-email">Email address</label>
                        <div className="login-input-wrapper">
                            <span className="login-input-icon" aria-hidden="true">
                                <svg viewBox="0 0 24 24" fill="none">
                                    <rect
                                        x="3"
                                        y="5"
                                        width="18"
                                        height="14"
                                        rx="3"
                                        stroke="currentColor"
                                        strokeWidth="1.7"
                                    />
                                    <path
                                        d="m4 7 8 6 8-6"
                                        stroke="currentColor"
                                        strokeWidth="1.7"
                                        strokeLinecap="round"
                                        strokeLinejoin="round"
                                    />
                                </svg>
                            </span>

                            <input
                                id="login-email"
                                type="email"
                                placeholder="you@example.com"
                                value={email}
                                onChange={(event) =>
                                    setEmail(event.target.value)
                                }
                                autoComplete="email"
                                required
                                disabled={loading}
                            />
                        </div>
                    </div>

                    <div className="login-form-group">
                        <label htmlFor="login-password">Password</label>
                        <div className="login-input-wrapper">
                            <span className="login-input-icon" aria-hidden="true">
                                <svg viewBox="0 0 24 24" fill="none">
                                    <rect
                                        x="4"
                                        y="10"
                                        width="16"
                                        height="11"
                                        rx="3"
                                        stroke="currentColor"
                                        strokeWidth="1.7"
                                    />
                                    <path
                                        d="M8 10V7a4 4 0 0 1 8 0v3"
                                        stroke="currentColor"
                                        strokeWidth="1.7"
                                        strokeLinecap="round"
                                    />
                                    <circle
                                        cx="12"
                                        cy="15"
                                        r="1.2"
                                        fill="currentColor"
                                    />
                                </svg>
                            </span>

                            <input
                                id="login-password"
                                type={showPassword ? "text" : "password"}
                                placeholder="Enter your password"
                                value={password}
                                onChange={(event) =>
                                    setPassword(event.target.value)
                                }
                                autoComplete="current-password"
                                required
                                disabled={loading}
                            />

                            <button
                                type="button"
                                className="password-toggle"
                                onClick={() =>
                                    setShowPassword((current) => !current)
                                }
                                aria-label={
                                    showPassword
                                        ? "Hide password"
                                        : "Show password"
                                }
                                aria-pressed={showPassword}
                            >
                                {showPassword ? (
                                    <svg viewBox="0 0 24 24" fill="none">
                                        <path
                                            d="M3 3 21 21M10.6 10.7a2 2 0 0 0 2.7 2.7"
                                            stroke="currentColor"
                                            strokeWidth="1.7"
                                            strokeLinecap="round"
                                        />
                                        <path
                                            d="M9.9 5.2A10.8 10.8 0 0 1 12 5c5.2 0 8.5 5 9 7-.2.8-1 2-2.3 3.2M6.2 6.2C3.8 7.8 2.4 10.3 3 12c.5 1.9 3.8 7 9 7 1.2 0 2.3-.3 3.3-.7"
                                            stroke="currentColor"
                                            strokeWidth="1.7"
                                            strokeLinecap="round"
                                            strokeLinejoin="round"
                                        />
                                    </svg>
                                ) : (
                                    <svg viewBox="0 0 24 24" fill="none">
                                        <path
                                            d="M2.8 12s3.2-7 9.2-7 9.2 7 9.2 7-3.2 7-9.2 7-9.2-7-9.2-7Z"
                                            stroke="currentColor"
                                            strokeWidth="1.7"
                                            strokeLinejoin="round"
                                        />
                                        <circle
                                            cx="12"
                                            cy="12"
                                            r="3"
                                            stroke="currentColor"
                                            strokeWidth="1.7"
                                        />
                                    </svg>
                                )}
                            </button>
                        </div>
                    </div>

                    {error && (
                        <div className="login-error" role="alert">
                            <span aria-hidden="true">!</span>
                            <p>{error}</p>
                        </div>
                    )}

                    <button
                        type="submit"
                        className="login-submit"
                        disabled={loading}
                    >
                        {loading ? (
                            <>
                                <span className="login-spinner" />
                                Signing in...
                            </>
                        ) : (
                            <>
                                Sign in
                                <span aria-hidden="true">→</span>
                            </>
                        )}
                    </button>
                </form>

                <div className="login-footer">
                    <span className="login-status-dot" />
                    Secure authentication
                </div>
            </section>

            <p className="login-bottom-text">
                Understand your codebase. Navigate with confidence.
            </p>
        </main>
    );
}

export default Login;
