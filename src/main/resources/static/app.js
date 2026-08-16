const personEl = document.getElementById("person");
const roleEl = document.getElementById("role");
const formEl = document.getElementById("analysis-form");
const loadingEl = document.getElementById("loading");
const errorEl = document.getElementById("error");
const resultsEl = document.getElementById("results");
const titleEl = document.getElementById("result-title");

const skillsListEl = document.getElementById("skills-list");
const coursesListEl = document.getElementById("courses-list");
const mentorsListEl = document.getElementById("mentors-list");
const skillsEmptyEl = document.getElementById("skills-empty");
const coursesEmptyEl = document.getElementById("courses-empty");
const mentorsEmptyEl = document.getElementById("mentors-empty");

async function loadCatalog() {
    setLoading(true);
    clearError();

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
    } catch (e) {
        showError(e.message);
    } finally {
        setLoading(false);
    }
}

formEl.addEventListener("submit", async (event) => {
    event.preventDefault();
    setLoading(true);
    clearError();

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
    } catch (e) {
        resultsEl.classList.add("hidden");
        showError(e.message);
    } finally {
        setLoading(false);
    }
});

function renderAnalysis(data) {
    titleEl.textContent = `${data.person.name} -> ${data.targetRole.title}`;

    skillsListEl.innerHTML = data.missingSkills
        .map((skill) => `<li>${skill.name} (importance: ${skill.importance})</li>`)
        .join("");

    coursesListEl.innerHTML = data.courseRecommendations
        .map((course) => `<li><strong>${course.title}</strong> - covers: ${course.coveredSkills.join(", ")}</li>`)
        .join("");

    mentorsListEl.innerHTML = data.mentorRecommendations
        .map((mentor) => `<li>${mentor.name} - ${mentor.matchedSkills}/${mentor.totalMissingSkills} skills, ${mentor.hops} hop(s)</li>`)
        .join("");

    toggleEmptyState(skillsEmptyEl, data.missingSkills.length === 0);
    toggleEmptyState(coursesEmptyEl, data.courseRecommendations.length === 0);
    toggleEmptyState(mentorsEmptyEl, data.mentorRecommendations.length === 0);

    resultsEl.classList.remove("hidden");
}

function toggleEmptyState(element, visible) {
    element.classList.toggle("hidden", !visible);
}

function setLoading(isLoading) {
    loadingEl.classList.toggle("hidden", !isLoading);
}

function showError(message) {
    errorEl.textContent = message;
    errorEl.classList.remove("hidden");
}

function clearError() {
    errorEl.textContent = "";
    errorEl.classList.add("hidden");
}

loadCatalog();
