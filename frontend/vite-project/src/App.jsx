import { useEffect, useState } from "react";
import axios from "axios";

function App() {

    const [jobDescription, setJobDescription] = useState("");
    const [resumeText, setResumeText] = useState("");

    const [result, setResult] = useState(null);
    const [savedJobs, setSavedJobs] = useState([]);

    const [loading, setLoading] = useState(false);

    const analyzeMatch = async () => {
        try {
            setLoading(true);
            const jobResponse = await axios.post("http://localhost:8080/api/ai/analyze-job",
                {
                    jobDescription: jobDescription
                }
            );
            const resumeResponse = await axios.post("http://localhost:8080/api/ai/analyze-resume",
                {
                    resumeText: resumeText
                }
            );
            const gapResponse = await axios.post("http://localhost:8080/api/ai/analyze-gap",
                {
                    job: jobResponse.data,
                    resume: resumeResponse.data
                }
            );
            setResult(gapResponse.data);

        } catch (error) {

            console.error("FULL ERROR:", error);
            if (error.response) {
                console.log("Backend Response:",
                    error.response.data);
                alert(JSON.stringify(error.response.data));
            } else {
                alert(error.message);
            }
        } finally {
            setLoading(false);
        }
    };
useEffect(() => {
    fetchSavedJobs();

}, []);

const fetchSavedJobs = async () => {

    try {

        const response = await axios.get(
            "http://localhost:8080/api/ai/jobs"
        );

        setSavedJobs(response.data);

    } catch (error) {

        console.error(error);
    }
};

    return (
        <div style={{
            maxWidth: "900px",
            margin: "0 auto",
            padding: "40px",
            fontFamily: "Arial"
        }}>

            <h1>AI Job Copilot</h1>
            <h2>Saved Job Analyses</h2>

            {savedJobs.map((job) => (

                <div
                    key={job.id}
                    style={{
                        border: "1px solid gray",
                        padding: "10px",
                        marginBottom: "10px"
                    }}
                >

                    <h3>{job.role}</h3>

                    <p>
                        Experience: {job.experience}
                    </p>

                    <p>
                        Required Skills:
                        {job.requiredSkills?.join(", ")}
                    </p>

                </div>
            ))}

            <h3>Job Description</h3>

            <textarea
                rows="10"
                style={{
                    width: "100%",
                    marginBottom: "20px"
                }}
                value={jobDescription}
                onChange={(e) =>
                    setJobDescription(e.target.value)
                }
            />

            <h3>Resume</h3>

            <textarea
                rows="10"
                style={{
                    width: "100%",
                    marginBottom: "20px"
                }}
                value={resumeText}
                onChange={(e) =>
                    setResumeText(e.target.value)
                }
            />

            <button
                onClick={analyzeMatch}
                disabled={loading}
                style={{
                    padding: "10px 20px",
                    cursor: "pointer"
                }}
            >
                {loading ? "Analyzing..." : "Analyze Match"}
            </button>

            {result && (

                <div style={{
                    marginTop: "40px",
                    border: "1px solid #ccc",
                    padding: "20px"
                }}>

                    <h2>
                        Match Score: {result.matchScore}%
                    </h2>

                    <h3>Matching Skills</h3>

                    <ul>
                        {result.matchingSkills.map((skill, index) => (
                            <li key={index}>{skill}</li>
                        ))}
                    </ul>

                    <h3>Missing Skills</h3>

                    <ul>
                        {result.missingSkills.map((skill, index) => (
                            <li key={index}>{skill}</li>
                        ))}
                    </ul>

                    <h3>Suggestions</h3>

                    <ul>
                        {result.suggestions.map((item, index) => (
                            <li key={index}>{item}</li>
                        ))}
                    </ul>

                </div>
            )}

        </div>
    );
}

export default App;