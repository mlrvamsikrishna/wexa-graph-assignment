# WEXA CognoDB Take-Home Module

Career Graph Navigator is a small web app that helps a person evaluate a move to a target role.
It answers three questions with graph traversals:

1. What skills am I missing for that role?
2. Which courses cover those missing skills?
3. Which nearby mentors (within 2 hops) can help close the gap?

## Why a graph database?

This use case is relationship-heavy and traversal-first:

- Skills connect people, roles, and courses in many-to-many patterns.
- Mentor discovery requires variable-length traversal (`:MENTORS*1..2`).
- Recommendations combine direct and indirect links in one query pipeline.

In a relational schema, this needs multiple joins and query orchestration. In a graph, the path itself is the data model, so traversals remain expressive and concise.

## Data Model

```mermaid
graph LR
    Person[Person]
    Role[Role]
    Skill[Skill]
    Course[Course]

    Person -->|WORKS_AS| Role
    Person -->|HAS_SKILL {level}| Skill
    Role -->|REQUIRES {importance}| Skill
    Course -->|TEACHES| Skill
    Person -->|MENTORS| Person
```

## Stack

- Java 17
- Spring Boot (Web)
- Official Neo4j Java Driver (Bolt 5.x compatible for CognoDB)
- Static HTML/CSS/JS frontend

## CognoDB Setup

1. Sign up at `https://console.cognodb.com/signup`.
2. Create a free `c0` instance.
3. Save credentials:
   - URI: `bolt+s://<instance-id>.databases.cognodb.cloud`
   - User: `cognodb`
   - Password: generated once by console
4. Copy `.env.example` values into your local shell environment.

## Run Locally

Set environment variables:

```zsh
export COGNODB_URI="bolt+s://<instance-id>.databases.cognodb.cloud"
export COGNODB_USER="cognodb"
export COGNODB_PASSWORD="<your-password>"
```

Load seed data:

```zsh
zsh scripts/load-seed.sh
```

Run the web app:

```zsh
mvn spring-boot:run
```

Open `http://localhost:8081`.

## Main Cypher Queries

- **Skill gap (multi-hop context via role/skills/person):**
  - `Role -> REQUIRES -> Skill`
  - filter out skills where `Person -> HAS_SKILL -> Skill` exists
- **Course recommendation:**
  - find courses that teach missing skills and rank by coverage
- **Mentor recommendation (awkward in SQL):**
  - traverse mentorship network within 2 hops
  - score mentors by how many missing skills they cover

All queries are parameterized via Neo4j driver maps (no string concatenation).

## Project Structure

- `src/main/java/com/wexa/graph/config` - driver wiring from environment variables
- `src/main/java/com/wexa/graph/repository` - Cypher query layer
- `src/main/java/com/wexa/graph/service` - application service orchestration
- `src/main/java/com/wexa/graph/web` - REST API + API error handling
- `src/main/java/com/wexa/graph/seed` - seed script entrypoint
- `src/main/resources/static` - UI assets
- `scripts/load-seed.sh` - one-command seed loader

## API Endpoints

- `GET /api/catalog` - people and roles for dropdowns
- `POST /api/analysis` with body:

```json
{
  "personId": "person_ava",
  "roleId": "role_graph"
}
```

## What This Project Demonstrates

Career Graph Navigator is built to show where graph databases win in real product scenarios:

- **Relationship-first modeling** for role fit, learning paths, and mentor discovery.
- **Traversal-heavy querying** with variable-length paths (`MENTORS*1..2`) that are awkward in relational systems.
- **Clean engineering boundaries** across configuration, repository, service, and web layers.
- **Production-minded behavior** with parameterized Cypher, environment-based secrets, and graceful database error responses.

## UX Highlights

- Guided input via person + target role selectors.
- Clear result sections: missing skills, recommended courses, and nearby mentors.
- Thoughtful states for loading, empty results, and user-facing errors.
- Simple visual hierarchy for quick comprehension by non-technical users.

## Screenshots and Demo

- Hosted demo link: `ADD_YOUR_LINK`
- Screen recording: `ADD_YOUR_LINK`
- UI screenshots:
  - `docs/screenshots/home.png`
  - `docs/screenshots/analysis-result.png`
  - `docs/screenshots/error-state.png`

## Build and Test

```zsh
mvn test
mvn package
```

