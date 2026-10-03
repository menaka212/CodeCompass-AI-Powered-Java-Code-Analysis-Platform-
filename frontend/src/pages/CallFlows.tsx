import { useEffect, useState } from "react";
import { getApiCallFlows } from "../services/api";


interface CallFlowNode {

    className: string;

    methodName: string;

    classType: string;

    databaseOperation: string;

    nextCalls: CallFlowNode[];

}


interface ApiCallFlow {

    httpMethod: string;

    endpoint: string;

    callFlow: CallFlowNode;

}


function CallFlowNodeComponent({

    node,

    level = 0,

}: {

    node: CallFlowNode;

    level?: number;

}) {

    return (

        <div
            className="call-flow-node-wrapper"
            style={{
                marginLeft: `${level * 35}px`
            }}
        >

            <div className="call-flow-node">

                <div className="node-main">

                    <div className="node-class">

                        {node.className}

                    </div>

                    <div className="node-method">

                        {node.methodName}()

                    </div>

                </div>


                <div className="node-tags">

                    <span
                        className={`class-type ${node.classType.toLowerCase()}`}
                    >

                        {node.classType}

                    </span>


                    {node.databaseOperation !== "NONE" && (

                        <span
                            className={`database-operation ${node.databaseOperation.toLowerCase()}`}
                        >

                            {node.databaseOperation}

                        </span>

                    )}

                </div>

            </div>


            {node.nextCalls.length > 0 && (

                <div className="next-calls">

                    {node.nextCalls.map(

                        (nextNode, index) => (

                            <CallFlowNodeComponent

                                key={`${nextNode.className}-${nextNode.methodName}-${index}`}

                                node={nextNode}

                                level={level + 1}

                            />

                        )

                    )}

                </div>

            )}

        </div>

    );

}


function CallFlows() {


    const [

        callFlows,

        setCallFlows

    ] = useState<ApiCallFlow[]>([]);


    const [

        loading,

        setLoading

    ] = useState(true);


    const [

        error,

        setError

    ] = useState("");

useEffect(() => {

    const storedRepositoryUrl =
        localStorage.getItem("githubUrl");

    if (!storedRepositoryUrl) {

        setError(
            "Please analyze a GitHub repository first."
        );

        setLoading(false);

        return;
    }

    const repositoryUrl: string =
        storedRepositoryUrl;


    async function loadCallFlows() {

        try {

            setLoading(true);

            setError("");

            const data =
                await getApiCallFlows(repositoryUrl);

            console.log(
                "API Call Flows:",
                data
            );

            setCallFlows(data);

        } catch (err) {

            console.error(err);

            setError(
                "Unable to load API call flows."
            );

        } finally {

            setLoading(false);

        }

    }


    loadCallFlows();

}, []);
    


    if (loading) {


        return (

            <div className="page-container">


                <div className="page-header">


                    <p className="eyebrow">

                        CODE INSIGHTS

                    </p>


                    <h1>

                        Call Flows

                    </h1>


                    <p>

                        Loading API call flows...

                    </p>


                </div>


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

                        Call Flows

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

                    Call Flows

                </h1>


                <p>

                    Trace how API requests move through
                    controllers and repository operations.

                </p>


            </div>


            <div className="call-flow-summary">


                <div className="summary-card">


                    <span>

                        API Endpoints

                    </span>


                    <strong>

                        {callFlows.length}

                    </strong>


                </div>


                <div className="summary-card">


                    <span>

                        GET Requests

                    </span>


                    <strong>

                        {

                            callFlows.filter(

                                (flow) =>

                                    flow.httpMethod === "GET"

                            ).length

                        }

                    </strong>


                </div>


                <div className="summary-card">


                    <span>

                        POST Requests

                    </span>


                    <strong>

                        {

                            callFlows.filter(

                                (flow) =>

                                    flow.httpMethod === "POST"

                            ).length

                        }

                    </strong>


                </div>


            </div>


            <div className="call-flow-list">


                {

                    callFlows.map(

                        (flow, index) => (

                            <div

                                className="call-flow-card"

                                key={`${flow.endpoint}-${index}`}

                            >


                                <div className="call-flow-header">


                                    <span

                                        className={`method-badge ${flow.httpMethod.toLowerCase()}`}

                                    >

                                        {flow.httpMethod}

                                    </span>


                                    <h2>

                                        {flow.endpoint}

                                    </h2>


                                </div>


                                <div className="flow-section-title">

                                    Request Flow

                                </div>


                                <CallFlowNodeComponent

                                    node={flow.callFlow}

                                />


                            </div>

                        )

                    )

                }


            </div>


        </div>

    );

}


export default CallFlows;