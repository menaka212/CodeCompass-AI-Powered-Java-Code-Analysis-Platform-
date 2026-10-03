import { useEffect, useState } from "react";
import { getApiMap } from "../services/api";

interface ApiParameter {
    name: string;
    type: string;
    source: string;
}

interface ApiEndpoint {
    method: string;
    path: string;
    handler: string;
    returnType: string;
    responseStatus: string | null;
    parameters: ApiParameter[];
}

interface ApiController {
    controllerName: string;
    endpoints: ApiEndpoint[];
}

function ApiExplorer() {

    const [apiData, setApiData] = useState<ApiController[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {

    const repositoryUrl =
        localStorage.getItem("githubUrl");

    if (!repositoryUrl) {

        setError(
            "Please analyze a GitHub repository first."
        );

        setLoading(false);

        return;
    }


    const loadApiMap = async () => {

        try {

            setLoading(true);

            setError("");

            const data =
                await getApiMap(repositoryUrl);

            console.log(
                "API Map:",
                data
            );

            setApiData(data);

        } catch (err) {

            console.error(err);

            setError(
                "Unable to load API information."
            );

        } finally {

            setLoading(false);

        }

    };


    loadApiMap();

}, []);

    if (loading) {

        return (

            <div className="page-container">

                <div className="page-header">

                    <p className="eyebrow">
                        CODE INSIGHTS
                    </p>

                    <h1>
                        API Explorer
                    </h1>

                    <p>
                        Discover API endpoints in your repository.
                    </p>

                </div>

                <p className="loading-message">
                    Loading API endpoints...
                </p>

            </div>

        );
    }


    if (error) {

        return (

            <div className="page-container">

                <div className="page-header">

                    <p className="eyebrow">
                        CODE INSIGHTS
                    </p>

                    <h1>
                        API Explorer
                    </h1>

                </div>

                <div className="error-box">

                    {error}

                </div>

            </div>

        );
    }


    return (

        <div className="page-container">

            <div className="page-header">

                <p className="eyebrow">
                    CODE INSIGHTS
                </p>

                <h1>
                    API Explorer
                </h1>

                <p>
                    Explore controllers and API endpoints
                    discovered in your repository.
                </p>

            </div>


            <div className="api-summary">

                <div className="summary-card">

                    <span>
                        Controllers
                    </span>

                    <strong>
                        {apiData.length}
                    </strong>

                </div>


                <div className="summary-card">

                    <span>
                        Total Endpoints
                    </span>

                    <strong>

                        {apiData.reduce(
                            (total, controller) =>
                                total +
                                controller.endpoints.length,
                            0
                        )}

                    </strong>

                </div>

            </div>


            <div className="api-controller-list">

                {apiData.map((controller) => (

                    <div
                        className="controller-card"
                        key={controller.controllerName}
                    >

                        <div className="controller-header">

                            <div>

                                <h2>
                                    {controller.controllerName}
                                </h2>

                                <p>
                                    {controller.endpoints.length}
                                    {" "}
                                    endpoint
                                    {controller.endpoints.length !== 1
                                        ? "s"
                                        : ""}
                                </p>

                            </div>

                        </div>


                        <div className="endpoint-list">

                            {controller.endpoints.map(
                                (endpoint, index) => (

                                    <div
                                        className="endpoint-card"
                                        key={`${endpoint.path}-${index}`}
                                    >

                                        <div className="endpoint-main">

                                            <span
                                                className={`method-badge ${endpoint.method.toLowerCase()}`}
                                            >

                                                {endpoint.method}

                                            </span>


                                            <span className="endpoint-path">

                                                {endpoint.path}

                                            </span>

                                        </div>


                                        <div className="endpoint-details">

                                            <div>

                                                <span>
                                                    Handler
                                                </span>

                                                <strong>
                                                    {endpoint.handler}
                                                </strong>

                                            </div>


                                            <div>

                                                <span>
                                                    Return Type
                                                </span>

                                                <strong>
                                                    {endpoint.returnType}
                                                </strong>

                                            </div>

                                        </div>


                                        {endpoint.parameters.length > 0 && (

                                            <div className="parameters">

                                                <span>
                                                    Parameters:
                                                </span>

                                                {endpoint.parameters.map(
                                                    (parameter, parameterIndex) => (

                                                        <div
                                                            className="parameter"
                                                            key={parameterIndex}
                                                        >

                                                            <strong>
                                                                {parameter.name}
                                                            </strong>

                                                            <span>
                                                                {parameter.type}
                                                            </span>

                                                            <span>
                                                                {parameter.source}
                                                            </span>

                                                        </div>

                                                    )
                                                )}

                                            </div>

                                        )}

                                    </div>

                                )
                            )}

                        </div>

                    </div>

                ))}

            </div>

        </div>

    );
}

export default ApiExplorer;