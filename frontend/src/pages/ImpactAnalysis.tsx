import { useState } from "react";
import { getImpactAnalysis } from "../services/api";


interface Caller {
    className: string;
    methodName: string;
}


interface ImpactResult {
    targetClass: string;
    targetMethod: string;
    classType: string;
    databaseOperation: string;
    callers: Caller[];
}


function ImpactAnalysis() {

    const [targetClass, setTargetClass] = useState("");
    const [targetMethod, setTargetMethod] = useState("");

    const [result, setResult] =
        useState<ImpactResult | null>(null);

    const [loading, setLoading] =
        useState(false);

    const [error, setError] =
        useState("");


    const handleAnalyze = async () => {

        const repositoryUrl =
            localStorage.getItem("githubUrl");


        if (!repositoryUrl) {

            setError(
                "Please analyze a GitHub repository first."
            );

            return;
        }


        if (!targetClass.trim()) {

            setError(
                "Please enter a target class."
            );

            return;
        }


        if (!targetMethod.trim()) {

            setError(
                "Please enter a target method."
            );

            return;
        }


        try {

            setLoading(true);

            setError("");

            setResult(null);


            const data =
                await getImpactAnalysis(
                    repositoryUrl,
                    targetClass,
                    targetMethod
                );


            console.log(
                "Impact Analysis Result:",
                data
            );


            setResult(data);


        } catch (err) {

            console.error(err);

            setError(
                "Unable to analyze impact."
            );


        } finally {

            setLoading(false);

        }

    };


    return (

        <div className="page-container">


            {/* PAGE HEADER */}

            <div className="page-header">

                <p className="eyebrow">
                    CODE INSIGHTS
                </p>


                <h1>
                    Impact Analysis
                </h1>


                <p>
                    Discover which parts of your
                    application may be affected by
                    a code change.
                </p>

            </div>



            {/* INPUT CARD */}

            <div className="impact-input-card">

                <h2>
                    Analyze Code Impact
                </h2>


                <p>
                    Enter a class and method to
                    identify the components that
                    depend on it.
                </p>


                <div className="impact-inputs">


                    <div className="input-group">

                        <label>
                            Target Class
                        </label>


                        <input
                            type="text"
                            value={targetClass}
                            onChange={(event) =>
                                setTargetClass(
                                    event.target.value
                                )
                            }
                            placeholder="Example: OwnerRepository"
                        />

                    </div>



                    <div className="input-group">

                        <label>
                            Target Method
                        </label>


                        <input
                            type="text"
                            value={targetMethod}
                            onChange={(event) =>
                                setTargetMethod(
                                    event.target.value
                                )
                            }
                            placeholder="Example: findById"
                        />

                    </div>


                </div>



                <button
                    className="analyze-button"
                    onClick={handleAnalyze}
                    disabled={loading}
                >

                    {loading
                        ? "Analyzing..."
                        : "Analyze Impact"
                    }

                </button>


                {error && (

                    <p className="error-message">

                        {error}

                    </p>

                )}

            </div>



            {/* RESULT */}

            {result && (

                <div className="impact-results">


                    {/* TARGET INFORMATION */}

                    <div className="impact-target-card">

                        <h2>
                            Target Component
                        </h2>


                        <div className="target-info">


                            <div>

                                <span>
                                    Class
                                </span>


                                <strong>
                                    {result.targetClass}
                                </strong>

                            </div>



                            <div>

                                <span>
                                    Method
                                </span>


                                <strong>
                                    {result.targetMethod}()
                                </strong>

                            </div>



                            <div>

                                <span>
                                    Type
                                </span>


                                <strong>
                                    {result.classType}
                                </strong>

                            </div>



                            <div>

                                <span>
                                    Database Operation
                                </span>


                                <strong>
                                    {result.databaseOperation}
                                </strong>

                            </div>


                        </div>

                    </div>



                    {/* CALLERS */}

                    <div className="callers-section">


                        <div className="section-heading">

                            <div>

                                <h2>
                                    Affected Callers
                                </h2>


                                <p>

                                    {result.callers.length}
                                    {" "}
                                    component
                                    {result.callers.length !== 1
                                        ? "s"
                                        : ""}

                                    {" "}
                                    may be affected.

                                </p>

                            </div>

                        </div>



                        {result.callers.length === 0 ? (

                            <div className="empty-state">

                                No callers were found.

                            </div>

                        ) : (

                            <div className="caller-grid">


                                {result.callers.map(
                                    (caller, index) => (

                                        <div
                                            className="caller-card"
                                            key={index}
                                        >

                                            <div className="caller-icon">

                                                ⚡

                                            </div>


                                            <div>

                                                <h3>

                                                    {
                                                        caller.className
                                                    }

                                                </h3>


                                                <p>

                                                    {
                                                        caller.methodName
                                                    }
                                                    ()

                                                </p>

                                            </div>


                                        </div>

                                    )
                                )}

                            </div>

                        )}

                    </div>


                </div>

            )}


        </div>

    );

}


export default ImpactAnalysis;