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

    // Optimize resume state
    const [optimizing, setOptimizing] = useState(false);
    const [optimizedResult, setOptimizedResult] = useState(null);

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
            setOptimizedResult(null);

            const [jobResponse, resumeResponse] = await Promise.all([
                axios.post(`${BASE_URL}/analyze-job`, { jobDescription }),
                axios.post(`${BASE_URL}/analyze-resume`, { resumeText })
            ]);

            const gapResponse = await axios.post(
                `${BASE_URL}/analyze-gap`, {
                    job: jobResponse.data,
                    resume: resumeResponse.data,
                    rawJobDescription: jobDescription,
                    rawResumeText: resumeText
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

    const optimizeResume = async () => {
        try {
            setOptimizing(true);
            setError(null);

            const response = await axios.post(
                `${BASE_URL}/optimize-resume`, {
                    jobDescription: jobDescription,
                    resumeText: resumeText
                });

            setOptimizedResult(response.data);
        } catch (err) {
            setError("Failed to optimize resume. Please try again.");
            console.error(err);
        } finally {
            setOptimizing(false);
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

                {/* ── Previous Analyses ── */}
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

                {/* ── Input Section ── */}
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

                {/* ── Result Section ── */}
                {result && (
                    <section className="result-section">

                        {/* Match Score */}
                        <div className={`score-card ${
                            result.matchScore >= 70 ? "score-high" :
                            result.matchScore >= 40 ? "score-mid" : "score-low"
                        }`}>
                            <div className="score-number">{result.matchScore}%</div>
                            <div className="score-label">Match Score</div>
                        </div>

                        {/* Skills Grid */}
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

                        {/* Suggestions */}
                        {result.suggestions?.length > 0 && (
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
                        )}

                        {/* AI Recruiter Analysis */}
                        {result.insights && (
                            <div className="insights-card">
                                <h3 className="skills-title">
                                    🤖 AI Recruiter Analysis
                                </h3>

                                <div className="insight-row">
                                    <span className="insight-label">Overall Fit</span>
                                    <span className={`fit-badge ${
                                        result.insights.overallFit === 'Strong Fit'
                                            ? 'fit-strong' :
                                        result.insights.overallFit === 'Good Fit'
                                            ? 'fit-good' :
                                        result.insights.overallFit === 'Partial Fit'
                                            ? 'fit-partial' :
                                            'fit-weak'
                                    }`}>
                                        {result.insights.overallFit}
                                    </span>
                                </div>

                                <div className="insight-row">
                                    <span className="insight-label">Seniority</span>
                                    <span className="insight-value">
                                        {result.insights.seniorityMatch}
                                    </span>
                                </div>

                                <div className="insight-row">
                                    <span className="insight-label">Experience</span>
                                    <span className="insight-value">
                                        {result.insights.experienceAssessment}
                                    </span>
                                </div>

                                <div className="insight-row">
                                    <span className="insight-label">Industry</span>
                                    <span className="insight-value">
                                        {result.insights.industryRelevance}
                                    </span>
                                </div>

                                {result.insights.keyStrengths?.length > 0 && (
                                    <div className="insight-section">
                                        <p className="insight-section-title">
                                            💪 Key Strengths
                                        </p>
                                        <ul className="insight-list">
                                            {result.insights.keyStrengths.map(
                                                (s, i) => (
                                                <li key={i}
                                                    className="insight-list-item green">
                                                    {s}
                                                </li>
                                            ))}
                                        </ul>
                                    </div>
                                )}

                                {result.insights.mainGaps?.length > 0 && (
                                    <div className="insight-section">
                                        <p className="insight-section-title">
                                            ⚠️ Main Gaps
                                        </p>
                                        <ul className="insight-list">
                                            {result.insights.mainGaps.map(
                                                (g, i) => (
                                                <li key={i}
                                                    className="insight-list-item red">
                                                    {g}
                                                </li>
                                            ))}
                                        </ul>
                                    </div>
                                )}

                                {result.insights.recruiterPerspective && (
                                    <div className="recruiter-perspective">
                                        <p className="insight-section-title">
                                            👔 Recruiter Perspective
                                        </p>
                                        <p className="perspective-text">
                                            {result.insights.recruiterPerspective}
                                        </p>
                                    </div>
                                )}

                                <div className="score-breakdown">
                                    <div className="score-item">
                                        <span className="score-item-label">
                                            Required Skills
                                        </span>
                                        <span className="score-item-value">
                                            {result.requiredMatchScore}%
                                        </span>
                                    </div>
                                    <div className="score-item">
                                        <span className="score-item-label">
                                            Preferred Skills
                                        </span>
                                        <span className="score-item-value">
                                            {result.preferredMatchScore}%
                                        </span>
                                    </div>
                                </div>
                            </div>
                        )}

                        {/* ── Optimize Resume Section ── */}
                        <div className="optimize-section">
                            {!optimizedResult ? (
                                <button
                                    className="optimize-btn"
                                    onClick={optimizeResume}
                                    disabled={optimizing}
                                >
                                    {optimizing
                                        ? "✨ Optimizing your resume..."
                                        : "✨ Optimize Resume for This Job"}
                                </button>
                            ) : (
                                <div className="optimized-result">

                                    {/* Score improvement banner */}
                                    <div className="improvement-banner">
                                        <span className="improvement-label">
                                            📈 Estimated Score After Optimization
                                        </span>
                                        <span className="improvement-value">
                                            {result.matchScore}%
                                            <span className="arrow">→</span>
                                            <span className="improved-score">
                                                {Math.min(100,
                                                    result.matchScore +
                                                    optimizedResult.estimatedScoreImprovement)}%
                                            </span>
                                        </span>
                                    </div>

                                    {/* Side by side comparison */}
                                    <div className="comparison-grid">
                                        <div className="resume-column">
                                            <h3 className="column-title">
                                                📄 Original Resume
                                            </h3>
                                            <pre className="resume-text">
                                                {resumeText}
                                            </pre>
                                        </div>
                                        <div className="resume-column">
                                            <h3 className="column-title">
                                                ✨ Optimized Resume
                                            </h3>
                                            <pre className="resume-text optimized">
                                                {optimizedResult.optimizedResume}
                                            </pre>
                                        </div>
                                    </div>

                                    {/* Changes list */}
                                    <div className="changes-card">
                                        <h3 className="skills-title">
                                            🔧 Changes Made
                                        </h3>
                                        <ul className="changes-list">
                                            {optimizedResult.changes.map(
                                                (change, i) => (
                                                <li key={i} className="change-item">
                                                    {change}
                                                </li>
                                            ))}
                                        </ul>
                                    </div>

                                    {/* Download buttons */}
                                    <div className="download-section">
                                        <button className="download-btn pdf-btn">
                                            📥 Download PDF
                                        </button>
                                        <button className="download-btn docx-btn">
                                            📥 Download DOCX
                                        </button>
                                    </div>
                                </div>
                            )}
                        </div>

                    </section>
                )}
            </main>
        </div>
    );
}

export default App;
