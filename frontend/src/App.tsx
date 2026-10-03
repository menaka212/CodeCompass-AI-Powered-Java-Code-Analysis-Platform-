import { useState } from "react";

import Sidebar from "./components/Sidebar";
import Header from "./components/Header";
import ImpactAnalysis from "./pages/ImpactAnalysis";
import Visualizations from "./pages/Visualizations";
import Login from "./pages/Login";
import CallFlows from "./pages/CallFlows";
import Dashboard from "./pages/Dashboard";
import Architecture from "./pages/Architecture";
import Dependencies from "./pages/Dependencies";
import ApiExplorer from "./pages/ApiExplorer";
import Projects from "./pages/Projects";
import AIAssistant from "./pages/AIAssistant";
import "./App.css";

function App() {

    const [activePage, setActivePage] =
        useState("dashboard");

    const [isLoggedIn, setIsLoggedIn] =
        useState(() => {

            const token =
                localStorage.getItem("token");

            return !!token;

        });


    const handleLogin = () => {

        setIsLoggedIn(true);

        setActivePage("dashboard");

    };


    const handleLogout = () => {

        localStorage.removeItem("token");

        setIsLoggedIn(false);

    };


    // ==============================
    // LOGIN PAGE
    // ==============================

    if (!isLoggedIn) {

        return (

            <Login
                onLogin={handleLogin}
            />

        );

    }


    // ==============================
    // MAIN APPLICATION
    // ==============================

    return (

        <div className="app">

            <Sidebar
                activePage={activePage}
                setActivePage={setActivePage}
            />

            <div className="main-area">

                <Header
                    onLogout={handleLogout}
                />

                <main className="content">

                    {activePage === "dashboard" && (

                        <Dashboard />

                    )}

                    {activePage === "architecture" && (

                        <Architecture />

                    )}

                    {activePage === "dependencies" && (

                        <Dependencies />

                    )}

                    {activePage === "api-explorer" && (

                        <ApiExplorer />

                    )}
                    {activePage === "call-flows" && (

                        <CallFlows />

                    )}
                    {activePage === "impact-analysis" && (

                        <ImpactAnalysis />

                    )}
                    {activePage === "projects" && (
  <Projects setActivePage={setActivePage} />
)}
{activePage === "ai-assistant" && (
    <AIAssistant />
)}
{activePage === "visualizations" && (
    <Visualizations />
)}
                </main>

            </div>

        </div>

    );

}

export default App;