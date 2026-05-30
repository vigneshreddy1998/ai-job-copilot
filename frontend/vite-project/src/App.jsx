import { useEffect, useState } from "react";
import axios from "axios";
import "./App.css";

const BASE_URL = "http://localhost:8080/api/ai";

function App() {
    const [jobDescription, setJobDescription] = useState("");
    const [resumeText, setResumeText] = useState("");
    const [result, setResult] = useState(null);
    const [savedJobs, setSavedJobs] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetchSavedJobs();
    }, []);

    const fetchSavedJobs = async () => {
        try {
            const response = await axios.get(`${BASE_URL}/jobs`);
            setSavedJobs(response.data);
        } catch (err) {
            console.error("Failed to fetch saved jobs:", err);
        }
    };

    const analyzeMatch = async () => {
        if (!jobDescription.trim() || !resumeText.trim()) {
            setError("Please enter both a job description and resume.");
            return;
        }

        try {
            setLoading(true);
            setError(null);
            setResult(null);

            const [jobResponse, resumeResponse] = await Promise.all([
                axios.post(`${BASE_URL}/analyze-job`, { jobDescription }),
                axios.post(`${BASE_URL}/analyze-resume`, { resumeText })
            ]);

            const gapResponse = await axios.post(`${BASE_URL}/analyze-gap`, {
                job: jobResponse.data,
                resume: resumeResponse.data
            });

            setResult(gapResponse.data);
            fetchSavedJobs();

        } catch (err) {
            setError("Analysis failed. Please try again.");
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="app">
            <header className="header">
                <h1 className="header-title">🤖 AI Job Copilot</h1>
                <p className="header-subtitle">
                    Paste a job description and your resume to see how well you match
                </p>
            </header>

            <main className="main">
                {savedJobs.length > 0 && (
                    <section className="saved-jobs">
                        <h2 className="section-title">Previous Analyses</h2>
                        <div className="saved-jobs-grid">
                            {savedJobs.map((job) => (
                                <div key={job.id} className="saved-job-card">
                                    <h3 className="saved-job-role">{job.role}</h3>
                                    <p className="saved-job-exp">
                                        Experience: {job.experience}
                                    </p>
                                    <p className="saved-job-skills">
                                        {job.requiredSkills?.slice(0, 4).join(" • ")}
                                        {job.requiredSkills?.length > 4 && " ..."}
                                    </p>
                                </div>
                            ))}
                        </div>
                    </section>
                )}

                <section className="input-section">
                    <div className="input-grid">
                        <div className="input-group">
                            <label className="input-label">Job Description</label>
                            <textarea
                                className="textarea"
                                placeholder="Paste the job description here..."
                                value={jobDescription}
                                onChange={(e) => setJobDescription(e.target.value)}
                            />
                        </div>
                        <div className="input-group">
                            <label className="input-label">Your Resume</label>
                            <textarea
                                className="textarea"
                                placeholder="Paste your resume text here..."
                                value={resumeText}
                                onChange={(e) => setResumeText(e.target.value)}
                            />
                        </div>
                    </div>

                    {error && <p className="error-message">{error}</p>}

                    <button
                        className={`analyze-btn ${loading ? "loading" : ""}`}
                        onClick={analyzeMatch}
                        disabled={loading}
                    >
                        {loading ? "⏳ Analyzing..." : "⚡ Analyze Match"}
                    </button>
                </section>

                {result && (
                    <section className="result-section">
                        <div className={`score-card ${
                            result.matchScore >= 70 ? "score-high" :
                            result.matchScore >= 40 ? "score-mid" : "score-low"
                        }`}>
                            <div className="score-number">{result.matchScore}%</div>
                            <div className="score-label">Match Score</div>
                        </div>

                        <div className="skills-grid">
                            <div className="skills-card skills-matching">
                                <h3 className="skills-title">✅ Matching Skills</h3>
                                <div className="skills-tags">
                                    {result.matchingSkills.map((skill, i) => (
                                        <span key={i} className="tag tag-green">
                                            {skill}
                                        </span>
                                    ))}
                                </div>
                            </div>

                            <div className="skills-card skills-missing">
                                <h3 className="skills-title">❌ Missing Skills</h3>
                                <div className="skills-tags">
                                    {result.missingSkills.map((skill, i) => (
                                        <span key={i} className="tag tag-red">
                                            {skill}
                                        </span>
                                    ))}
                                </div>
                            </div>
                        </div>

                        <div className="suggestions-card">
                            <h3 className="skills-title">💡 Suggestions</h3>
                            <ul className="suggestions-list">
                                {result.suggestions.map((item, i) => (
                                    <li key={i} className="suggestion-item">
                                        {item}
                                    </li>
                                ))}
                            </ul>
                        </div>
                    </section>
                )}
            </main>
        </div>
    );
}

export default App;