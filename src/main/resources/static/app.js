const personEl = document.getElementById("person");
const roleEl = document.getElementById("role");
const formEl = document.getElementById("analysis-form");
const statusEl = document.getElementById("status");
const analyzeBtnEl = document.getElementById("analyze-btn");
const resultsEl = document.getElementById("results");
const titleEl = document.getElementById("result-title");
const summaryEl = document.getElementById("result-summary");
const peopleCountEl = document.getElementById("people-count");
const rolesCountEl = document.getElementById("roles-count");

const skillsListEl = document.getElementById("skills-list");
const coursesListEl = document.getElementById("courses-list");
const mentorsListEl = document.getElementById("mentors-list");
const skillsEmptyEl = document.getElementById("skills-empty");
const coursesEmptyEl = document.getElementById("courses-empty");
const mentorsEmptyEl = document.getElementById("mentors-empty");

async function loadCatalog() {
    clearStatus();
    setLoading(true, "Loading people and roles from the graph...");

    try {
        const res = await fetch("/api/catalog");
        if (!res.ok) {
            throw new Error("Failed to load people and roles");
        }

        const data = await res.json();
        personEl.innerHTML = data.people
            .map((person) => `<option value="${person.id}">${person.name}</option>`)
            .join("");

        roleEl.innerHTML = data.roles
            .map((role) => `<option value="${role.id}">${role.title}</option>`)
            .join("");

        peopleCountEl.textContent = `People: ${data.people.length}`;
        rolesCountEl.textContent = `Roles: ${data.roles.length}`;
        showStatus("Catalog loaded. Select inputs and run analysis.", "success");
    } catch (e) {
        showStatus(e.message, "error");
    } finally {
        setLoading(false);
    }
}

formEl.addEventListener("submit", async (event) => {
    event.preventDefault();
    clearStatus();
    setLoading(true, "Running graph analysis...");

    try {
        const res = await fetch("/api/analysis", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
                personId: personEl.value,
                roleId: roleEl.value,
            }),
        });

        const payload = await res.json();

        if (!res.ok) {
            throw new Error(payload.message || "Failed to analyze career move");
        }

        renderAnalysis(payload);
        showStatus("Analysis completed successfully.", "success");
    } catch (e) {
        resultsEl.classList.add("hidden");
        showStatus(e.message, "error");
    } finally {
        setLoading(false);
    }
});

function renderAnalysis(data) {
    titleEl.textContent = `${data.person.name} -> ${data.targetRole.title}`;
    summaryEl.textContent = data.missingSkills.length === 0
        ? `${data.person.name} already meets the target role. The course list below now shows role-aligned growth opportunities.`
        : `${data.person.name} has ${data.missingSkills.length} identified gap(s) for ${data.targetRole.title}.`;

    skillsListEl.innerHTML = data.missingSkills
        .map((skill) => `<li><strong>${skill.name}</strong> <span class="state">importance: ${skill.importance}/5</span></li>`)
        .join("");

    coursesListEl.innerHTML = data.courseRecommendations
        .map((course) => `<li><strong>${course.title}</strong><br><span class="state">Covers: ${course.coveredSkills.join(", ")}</span></li>`)
        .join("");

    coursesEmptyEl.textContent = data.missingSkills.length === 0
        ? "No gaps found — these are the most relevant courses to deepen role-aligned expertise."
        : "No course recommendation required.";

    mentorsListEl.innerHTML = data.mentorRecommendations
        .map((mentor) => `<li><strong>${mentor.name}</strong><br><span class="state">Coverage: ${mentor.matchedSkills}/${mentor.totalMissingSkills} skills | Distance: ${mentor.hops} hop(s)</span></li>`)
        .join("");

    toggleEmptyState(skillsEmptyEl, data.missingSkills.length === 0);
    toggleEmptyState(coursesEmptyEl, data.courseRecommendations.length === 0);
    toggleEmptyState(mentorsEmptyEl, data.mentorRecommendations.length === 0);

    resultsEl.classList.remove("hidden");
}

function toggleEmptyState(element, visible) {
    element.classList.toggle("hidden", !visible);
}

function setLoading(isLoading, message = "") {
    analyzeBtnEl.disabled = isLoading;
    analyzeBtnEl.textContent = isLoading ? "Analyzing..." : "Analyze with Graph Traversal";
    personEl.disabled = isLoading;
    roleEl.disabled = isLoading;
    if (isLoading) {
        showStatus(message || "Loading...", "loading");
    }
}

function showStatus(message, type) {
    statusEl.textContent = message;
    statusEl.className = `status ${type}`;
}

function clearStatus() {
    statusEl.textContent = "";
    statusEl.className = "status hidden";
}

loadCatalog();
