package com.wexa.graph.seed;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

import java.util.List;
import java.util.Map;

public class SeedDataLoader {

    public static void main(String[] args) {
        String uri = requiredEnv("COGNODB_URI");
        String user = envOrDefault("COGNODB_USER", "cognodb");
        String password = requiredEnv("COGNODB_PASSWORD");

        try (Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
             var session = driver.session()) {
            createConstraints(session);
            session.executeWrite(tx -> tx.run("MATCH (n) DETACH DELETE n").consume());

            session.executeWrite(tx -> tx.run("""
                    UNWIND $skills AS skill
                    MERGE (:Skill {id: skill.id, name: skill.name})
                    """, Map.of("skills", skills())).consume());

            session.executeWrite(tx -> tx.run("""
                    UNWIND $roles AS role
                    MERGE (:Role {id: role.id, title: role.title})
                    """, Map.of("roles", roles())).consume());

            session.executeWrite(tx -> tx.run("""
                    UNWIND $courses AS course
                    MERGE (:Course {id: course.id, title: course.title, provider: course.provider})
                    """, Map.of("courses", courses())).consume());

            session.executeWrite(tx -> tx.run("""
                    UNWIND $people AS person
                    MERGE (:Person {id: person.id, name: person.name, location: person.location})
                    """, Map.of("people", people())).consume());

            session.executeWrite(tx -> tx.run("""
                    UNWIND $roleSkills AS row
                    MATCH (r:Role {id: row.roleId})
                    MATCH (s:Skill {id: row.skillId})
                    MERGE (r)-[:REQUIRES {importance: row.importance}]->(s)
                    """, Map.of("roleSkills", roleSkills())).consume());

            session.executeWrite(tx -> tx.run("""
                    UNWIND $courseSkills AS row
                    MATCH (c:Course {id: row.courseId})
                    MATCH (s:Skill {id: row.skillId})
                    MERGE (c)-[:TEACHES]->(s)
                    """, Map.of("courseSkills", courseSkills())).consume());

            session.executeWrite(tx -> tx.run("""
                    UNWIND $personSkills AS row
                    MATCH (p:Person {id: row.personId})
                    MATCH (s:Skill {id: row.skillId})
                    MERGE (p)-[:HAS_SKILL {level: row.level}]->(s)
                    """, Map.of("personSkills", personSkills())).consume());

            session.executeWrite(tx -> tx.run("""
                    UNWIND $personRoles AS row
                    MATCH (p:Person {id: row.personId})
                    MATCH (r:Role {id: row.roleId})
                    MERGE (p)-[:WORKS_AS]->(r)
                    """, Map.of("personRoles", personRoles())).consume());

            session.executeWrite(tx -> tx.run("""
                    UNWIND $mentorships AS row
                    MATCH (from:Person {id: row.from})
                    MATCH (to:Person {id: row.to})
                    MERGE (from)-[:MENTORS]->(to)
                    """, Map.of("mentorships", mentorships())).consume());

            System.out.println("Seed data loaded successfully.");
        }
    }

    private static List<Map<String, Object>> skills() {
        return List.of(
                Map.of("id", "skill_java", "name", "Java"),
                Map.of("id", "skill_spring", "name", "Spring Boot"),
                Map.of("id", "skill_sql", "name", "SQL"),
                Map.of("id", "skill_graph", "name", "Graph Modeling"),
                Map.of("id", "skill_cypher", "name", "Cypher"),
                Map.of("id", "skill_react", "name", "React"),
                Map.of("id", "skill_design", "name", "System Design"),
                Map.of("id", "skill_llm", "name", "LLM Fundamentals"),
                Map.of("id", "skill_ds", "name", "Data Structures"),
                Map.of("id", "skill_python", "name", "Python")
        );
    }

    private static List<Map<String, Object>> roles() {
        return List.of(
                Map.of("id", "role_backend", "title", "Backend Engineer"),
                Map.of("id", "role_frontend", "title", "Frontend Engineer"),
                Map.of("id", "role_ml", "title", "ML Engineer"),
                Map.of("id", "role_graph", "title", "Graph Data Engineer"),
                Map.of("id", "role_architect", "title", "Solutions Architect")
        );
    }

    private static List<Map<String, Object>> courses() {
        return List.of(
                Map.of("id", "course_java_adv", "title", "Advanced Java Patterns", "provider", "Coursera"),
                Map.of("id", "course_spring_api", "title", "Spring Boot APIs in Practice", "provider", "Udemy"),
                Map.of("id", "course_cypher", "title", "Cypher Query Masterclass", "provider", "Neo4j GraphAcademy"),
                Map.of("id", "course_graph", "title", "Practical Graph Data Modeling", "provider", "Pluralsight"),
                Map.of("id", "course_react", "title", "React from Zero to Production", "provider", "Frontend Masters"),
                Map.of("id", "course_llm", "title", "LLM Engineering Essentials", "provider", "DeepLearning.AI"),
                Map.of("id", "course_design", "title", "System Design Interviews", "provider", "Educative"),
                Map.of("id", "course_python", "title", "Python for Engineers", "provider", "Codecademy")
        );
    }

    private static List<Map<String, Object>> people() {
        return List.of(
                Map.of("id", "person_ava", "name", "Ava", "location", "Bengaluru"),
                Map.of("id", "person_noah", "name", "Noah", "location", "Hyderabad"),
                Map.of("id", "person_olivia", "name", "Olivia", "location", "Pune"),
                Map.of("id", "person_liam", "name", "Liam", "location", "Chennai"),
                Map.of("id", "person_mia", "name", "Mia", "location", "Mumbai"),
                Map.of("id", "person_ethan", "name", "Ethan", "location", "Delhi"),
                Map.of("id", "person_emma", "name", "Emma", "location", "Kochi"),
                Map.of("id", "person_lucas", "name", "Lucas", "location", "Bengaluru")
        );
    }

    private static List<Map<String, Object>> roleSkills() {
        return List.of(
                Map.of("roleId", "role_backend", "skillId", "skill_java", "importance", 5),
                Map.of("roleId", "role_backend", "skillId", "skill_spring", "importance", 5),
                Map.of("roleId", "role_backend", "skillId", "skill_sql", "importance", 4),
                Map.of("roleId", "role_backend", "skillId", "skill_design", "importance", 3),
                Map.of("roleId", "role_frontend", "skillId", "skill_react", "importance", 5),
                Map.of("roleId", "role_frontend", "skillId", "skill_ds", "importance", 3),
                Map.of("roleId", "role_ml", "skillId", "skill_python", "importance", 5),
                Map.of("roleId", "role_ml", "skillId", "skill_llm", "importance", 5),
                Map.of("roleId", "role_ml", "skillId", "skill_ds", "importance", 4),
                Map.of("roleId", "role_graph", "skillId", "skill_graph", "importance", 5),
                Map.of("roleId", "role_graph", "skillId", "skill_cypher", "importance", 5),
                Map.of("roleId", "role_graph", "skillId", "skill_design", "importance", 3),
                Map.of("roleId", "role_graph", "skillId", "skill_sql", "importance", 3),
                Map.of("roleId", "role_architect", "skillId", "skill_design", "importance", 5),
                Map.of("roleId", "role_architect", "skillId", "skill_java", "importance", 4),
                Map.of("roleId", "role_architect", "skillId", "skill_graph", "importance", 3)
        );
    }

    private static List<Map<String, Object>> courseSkills() {
        return List.of(
                Map.of("courseId", "course_java_adv", "skillId", "skill_java"),
                Map.of("courseId", "course_java_adv", "skillId", "skill_design"),
                Map.of("courseId", "course_spring_api", "skillId", "skill_spring"),
                Map.of("courseId", "course_spring_api", "skillId", "skill_sql"),
                Map.of("courseId", "course_cypher", "skillId", "skill_cypher"),
                Map.of("courseId", "course_graph", "skillId", "skill_graph"),
                Map.of("courseId", "course_graph", "skillId", "skill_design"),
                Map.of("courseId", "course_react", "skillId", "skill_react"),
                Map.of("courseId", "course_llm", "skillId", "skill_llm"),
                Map.of("courseId", "course_python", "skillId", "skill_python"),
                Map.of("courseId", "course_python", "skillId", "skill_ds"),
                Map.of("courseId", "course_design", "skillId", "skill_design")
        );
    }

    private static List<Map<String, Object>> personSkills() {
        return List.of(
                Map.of("personId", "person_ava", "skillId", "skill_java", "level", 4),
                Map.of("personId", "person_ava", "skillId", "skill_sql", "level", 3),
                Map.of("personId", "person_noah", "skillId", "skill_react", "level", 5),
                Map.of("personId", "person_noah", "skillId", "skill_ds", "level", 3),
                Map.of("personId", "person_olivia", "skillId", "skill_python", "level", 4),
                Map.of("personId", "person_olivia", "skillId", "skill_llm", "level", 3),
                Map.of("personId", "person_liam", "skillId", "skill_graph", "level", 4),
                Map.of("personId", "person_liam", "skillId", "skill_cypher", "level", 4),
                Map.of("personId", "person_mia", "skillId", "skill_spring", "level", 4),
                Map.of("personId", "person_mia", "skillId", "skill_java", "level", 4),
                Map.of("personId", "person_ethan", "skillId", "skill_design", "level", 5),
                Map.of("personId", "person_ethan", "skillId", "skill_java", "level", 5),
                Map.of("personId", "person_emma", "skillId", "skill_sql", "level", 5),
                Map.of("personId", "person_emma", "skillId", "skill_graph", "level", 3),
                Map.of("personId", "person_lucas", "skillId", "skill_spring", "level", 3),
                Map.of("personId", "person_lucas", "skillId", "skill_design", "level", 2)
        );
    }

    private static List<Map<String, Object>> personRoles() {
        return List.of(
                Map.of("personId", "person_ava", "roleId", "role_backend"),
                Map.of("personId", "person_noah", "roleId", "role_frontend"),
                Map.of("personId", "person_olivia", "roleId", "role_ml"),
                Map.of("personId", "person_liam", "roleId", "role_graph"),
                Map.of("personId", "person_mia", "roleId", "role_backend"),
                Map.of("personId", "person_ethan", "roleId", "role_architect"),
                Map.of("personId", "person_emma", "roleId", "role_graph"),
                Map.of("personId", "person_lucas", "roleId", "role_backend")
        );
    }

    private static List<Map<String, Object>> mentorships() {
        return List.of(
                Map.of("from", "person_ethan", "to", "person_ava"),
                Map.of("from", "person_ethan", "to", "person_mia"),
                Map.of("from", "person_mia", "to", "person_lucas"),
                Map.of("from", "person_liam", "to", "person_emma"),
                Map.of("from", "person_emma", "to", "person_ava"),
                Map.of("from", "person_olivia", "to", "person_noah"),
                Map.of("from", "person_liam", "to", "person_olivia"),
                Map.of("from", "person_ava", "to", "person_noah")
        );
    }

    private static String requiredEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required environment variable: " + key);
        }
        return value;
    }

    private static String envOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static void createConstraints(org.neo4j.driver.Session session) {
        session.executeWrite(tx -> tx.run("""
                CREATE CONSTRAINT person_id IF NOT EXISTS
                FOR (p:Person)
                REQUIRE p.id IS UNIQUE
                """).consume());
        session.executeWrite(tx -> tx.run("""
                CREATE CONSTRAINT role_id IF NOT EXISTS
                FOR (r:Role)
                REQUIRE r.id IS UNIQUE
                """).consume());
        session.executeWrite(tx -> tx.run("""
                CREATE CONSTRAINT skill_id IF NOT EXISTS
                FOR (s:Skill)
                REQUIRE s.id IS UNIQUE
                """).consume());
        session.executeWrite(tx -> tx.run("""
                CREATE CONSTRAINT course_id IF NOT EXISTS
                FOR (c:Course)
                REQUIRE c.id IS UNIQUE
                """).consume());
    }
}
