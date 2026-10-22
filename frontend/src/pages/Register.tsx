
import { useState, type FormEvent } from "react";

interface RegisterProps {
    onBackToLogin: () => void;
}

const API_BASE_URL = (
    import.meta.env.VITE_API_BASE_URL || "http://localhost:8080"
).replace(/\/+$/, "");

function Register({ onBackToLogin }: RegisterProps) {
    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [loading, setLoading] = useState(false);

    const handleRegister = async (
        event: FormEvent<HTMLFormElement>
    ) => {
        event.preventDefault();
        setError("");
        setSuccess("");

        if (!username.trim() || !email.trim() || !password) {
            setError("Please fill in all required fields.");
            return;
        }

        if (password !== confirmPassword) {
            setError("Passwords do not match.");
            return;
        }

        setLoading(true);

        try {
            const response = await fetch(
                `${API_BASE_URL}/api/auth/register`,
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({
                        username: username.trim(),
                        email: email.trim(),
                        password,
                    }),
                }
            );

            const message = await response.text();

            if (!response.ok) {
                throw new Error(
                    message || "Registration failed. Please try again."
                );
            }

            setSuccess(
                message || "Registration successful! You can now sign in."
            );
            setUsername("");
            setEmail("");
            setPassword("");
            setConfirmPassword("");
        } catch (err) {
            setError(
                err instanceof Error
                    ? err.message
                    : "Registration failed. Please try again."
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
                        START YOUR JOURNEY
                    </span>
                    <h1>Create account</h1>
                    <p>
                        Join CodeCompass to explore your codebase
                        and understand how your code works.
                    </p>
                </div>

                <form onSubmit={handleRegister}>
                    <div className="login-form-group">
                        <label htmlFor="register-username">
                            Username
                        </label>
                        <div className="login-input-wrapper">
                            <input
                                id="register-username"
                                type="text"
                                placeholder="Choose a username"
                                value={username}
                                onChange={(event) =>
                                    setUsername(event.target.value)
                                }
                                autoComplete="username"
                                required
                                disabled={loading}
                                minLength={3}
                            />
                        </div>
                    </div>

                    <div className="login-form-group">
                        <label htmlFor="register-email">
                            Email address
                        </label>
                        <div className="login-input-wrapper">
                            <input
                                id="register-email"
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
                        <label htmlFor="register-password">
                            Password
                        </label>
                        <div className="login-input-wrapper">
                            <input
                                id="register-password"
                                type={showPassword ? "text" : "password"}
                                placeholder="Create a password"
                                value={password}
                                onChange={(event) =>
                                    setPassword(event.target.value)
                                }
                                autoComplete="new-password"
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
                            >
                                {showPassword ? "Hide" : "Show"}
                            </button>
                        </div>
                    </div>

                    <div className="login-form-group">
                        <label htmlFor="register-confirm-password">
                            Confirm password
                        </label>
                        <div className="login-input-wrapper">
                            <input
                                id="register-confirm-password"
                                type={showPassword ? "text" : "password"}
                                placeholder="Enter your password again"
                                value={confirmPassword}
                                onChange={(event) =>
                                    setConfirmPassword(event.target.value)
                                }
                                autoComplete="new-password"
                                required
                                disabled={loading}
                            />
                        </div>
                    </div>

                    {error && (
                        <div className="login-error" role="alert">
                            <span aria-hidden="true">!</span>
                            <p>{error}</p>
                        </div>
                    )}

                    {success && (
                        <div
                            className="login-success"
                            role="status"
                        >
                            <p>{success}</p>
                            <button
                                type="button"
                                className="login-submit"
                                onClick={onBackToLogin}
                            >
                                Go to sign in →
                            </button>
                        </div>
                    )}

                    {!success && (
                        <button
                            type="submit"
                            className="login-submit"
                            disabled={loading}
                        >
                            {loading
                                ? "Creating account..."
                                : "Create account →"}
                        </button>
                    )}
                </form>

                <div className="login-footer">
                    <span className="login-status-dot" />
                    Secure authentication
                </div>

                <p style={{ textAlign: "center", marginTop: "20px" }}>
                    Already have an account?{" "}
                    <button
                        type="button"
                        onClick={onBackToLogin}
                        disabled={loading}
                        style={{
                            background: "none",
                            border: "none",
                            color: "inherit",
                            cursor: "pointer",
                            fontWeight: 600,
                            textDecoration: "underline",
                        }}
                    >
                        Sign in
                    </button>
                </p>
            </section>

            <p className="login-bottom-text">
                Understand your codebase. Navigate with confidence.
            </p>
        </main>
    );
}

export default Register;
