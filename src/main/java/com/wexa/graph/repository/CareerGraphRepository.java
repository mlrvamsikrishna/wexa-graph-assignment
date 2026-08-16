package com.wexa.graph.repository;

import com.wexa.graph.model.CourseRecommendation;
import com.wexa.graph.model.MentorRecommendation;
import com.wexa.graph.model.MissingSkill;
import com.wexa.graph.model.PersonOption;
import com.wexa.graph.model.RoleOption;
import com.wexa.graph.web.DatabaseUnavailableException;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.exceptions.ServiceUnavailableException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class CareerGraphRepository {

    private final Driver driver;

    public CareerGraphRepository(Driver driver) {
        this.driver = driver;
    }

    public List<PersonOption> findPeople() {
        String query = """
                MATCH (p:Person)
                RETURN p.id AS id, p.name AS name
                ORDER BY p.name
                """;
        return runRead(query, Map.of(), record ->
                new PersonOption(record.get("id").asString(), record.get("name").asString()));
    }

    public List<RoleOption> findRoles() {
        String query = """
                MATCH (r:Role)
                RETURN r.id AS id, r.title AS title
                ORDER BY r.title
                """;
        return runRead(query, Map.of(), record ->
                new RoleOption(record.get("id").asString(), record.get("title").asString()));
    }

    public PersonOption findPerson(String personId) {
        String query = """
                MATCH (p:Person {id: $personId})
                RETURN p.id AS id, p.name AS name
                """;
        List<PersonOption> people = runRead(query, Map.of("personId", personId), record ->
                new PersonOption(record.get("id").asString(), record.get("name").asString()));
        if (people.isEmpty()) {
            throw new IllegalArgumentException("Unknown personId: " + personId);
        }
        return people.get(0);
    }

    public RoleOption findRole(String roleId) {
        String query = """
                MATCH (r:Role {id: $roleId})
                RETURN r.id AS id, r.title AS title
                """;
        List<RoleOption> roles = runRead(query, Map.of("roleId", roleId), record ->
                new RoleOption(record.get("id").asString(), record.get("title").asString()));
        if (roles.isEmpty()) {
            throw new IllegalArgumentException("Unknown roleId: " + roleId);
        }
        return roles.get(0);
    }

    public List<MissingSkill> findMissingSkills(String personId, String roleId) {
        String query = """
                MATCH (p:Person {id: $personId}), (r:Role {id: $roleId})
                MATCH (r)-[req:REQUIRES]->(s:Skill)
                WHERE NOT EXISTS { MATCH (p)-[:HAS_SKILL]->(s) }
                RETURN s.id AS id, s.name AS name, req.importance AS importance
                ORDER BY importance DESC, name ASC
                """;

        return runRead(query, Map.of("personId", personId, "roleId", roleId), record ->
                new MissingSkill(
                        record.get("id").asString(),
                        record.get("name").asString(),
                        record.get("importance").asInt())
        );
    }

    public List<CourseRecommendation> findCourseRecommendations(String personId, String roleId) {
        String query = """
                MATCH (p:Person {id: $personId}), (r:Role {id: $roleId})
                MATCH (r)-[:REQUIRES]->(s:Skill)
                WHERE NOT EXISTS { MATCH (p)-[:HAS_SKILL]->(s) }
                MATCH (c:Course)-[:TEACHES]->(s)
                RETURN c.id AS id,
                       c.title AS title,
                       collect(DISTINCT s.name) AS coveredSkills,
                       count(DISTINCT s) AS coverageCount
                ORDER BY coverageCount DESC, title ASC
                LIMIT 8
                """;

        return runRead(query, Map.of("personId", personId, "roleId", roleId), record ->
                new CourseRecommendation(
                        record.get("id").asString(),
                        record.get("title").asString(),
                        record.get("coveredSkills").asList(value -> value.asString()),
                        record.get("coverageCount").asLong())
        );
    }

    public List<CourseRecommendation> findRoleAlignedCourses(String roleId) {
        String query = """
                MATCH (r:Role {id: $roleId})-[:REQUIRES]->(s:Skill)
                MATCH (c:Course)-[:TEACHES]->(s)
                RETURN c.id AS id,
                       c.title AS title,
                       collect(DISTINCT s.name) AS coveredSkills,
                       count(DISTINCT s) AS coverageCount
                ORDER BY coverageCount DESC, title ASC
                LIMIT 8
                """;

        return runRead(query, Map.of("roleId", roleId), record ->
                new CourseRecommendation(
                        record.get("id").asString(),
                        record.get("title").asString(),
                        record.get("coveredSkills").asList(value -> value.asString()),
                        record.get("coverageCount").asLong())
        );
    }

    public List<MentorRecommendation> findMentorRecommendations(String personId, String roleId) {
        String query = """
                MATCH (target:Person {id: $personId})
                MATCH (r:Role {id: $roleId})-[:REQUIRES]->(missing:Skill)
                WHERE NOT EXISTS { MATCH (target)-[:HAS_SKILL]->(missing) }
                WITH target, collect(DISTINCT missing) AS missingSkills
                MATCH path = (target)-[:MENTORS*1..2]-(mentor:Person)
                WHERE mentor.id <> target.id
                WITH mentor, missingSkills, min(length(path)) AS hops
                MATCH (mentor)-[:HAS_SKILL]->(s:Skill)
                WHERE s IN missingSkills
                WITH mentor, hops, count(DISTINCT s) AS matchedSkills, size(missingSkills) AS totalMissingSkills
                WHERE totalMissingSkills > 0
                RETURN mentor.id AS id,
                       mentor.name AS name,
                       hops,
                       matchedSkills,
                       totalMissingSkills,
                       toFloat(matchedSkills) / totalMissingSkills AS coverage
                ORDER BY coverage DESC, hops ASC, name ASC
                LIMIT 5
                """;

        return runRead(query, Map.of("personId", personId, "roleId", roleId), record ->
                new MentorRecommendation(
                        record.get("id").asString(),
                        record.get("name").asString(),
                        record.get("hops").asLong(),
                        record.get("matchedSkills").asLong(),
                        record.get("totalMissingSkills").asLong(),
                        record.get("coverage").asDouble())
        );
    }

    public List<MentorRecommendation> findMentorRecommendationsForMissingSkills(String personId, List<String> missingSkillIds) {
        if (missingSkillIds.isEmpty()) {
            return List.of();
        }

        String query = """
                MATCH (target:Person {id: $personId})
                UNWIND $missingSkillIds AS skillId
                MATCH (missing:Skill {id: skillId})
                WITH target, collect(DISTINCT missing) AS missingSkills
                MATCH path = (target)-[:MENTORS*1..2]-(mentor:Person)
                WHERE mentor.id <> target.id
                WITH mentor, missingSkills, min(length(path)) AS hops
                MATCH (mentor)-[:HAS_SKILL]->(s:Skill)
                WHERE s IN missingSkills
                WITH mentor, hops, count(DISTINCT s) AS matchedSkills, size(missingSkills) AS totalMissingSkills
                WHERE totalMissingSkills > 0
                RETURN mentor.id AS id,
                       mentor.name AS name,
                       hops,
                       matchedSkills,
                       totalMissingSkills,
                       toFloat(matchedSkills) / totalMissingSkills AS coverage
                ORDER BY coverage DESC, hops ASC, name ASC
                LIMIT 5
                """;

        return runRead(query, Map.of("personId", personId, "missingSkillIds", missingSkillIds), record ->
                new MentorRecommendation(
                        record.get("id").asString(),
                        record.get("name").asString(),
                        record.get("hops").asLong(),
                        record.get("matchedSkills").asLong(),
                        record.get("totalMissingSkills").asLong(),
                        record.get("coverage").asDouble())
        );
    }

    private <T> List<T> runRead(String query, Map<String, Object> params, Mapper<T> mapper) {
        try (var session = driver.session()) {
            return session.executeRead(tx -> tx.run(query, params).list(record -> mapper.map(record)));
        } catch (ServiceUnavailableException e) {
            throw new DatabaseUnavailableException("Unable to connect to CognoDB", e);
        } catch (Neo4jException e) {
            throw new DatabaseUnavailableException("Cypher execution failed", e);
        }
    }

    @FunctionalInterface
    private interface Mapper<T> {
        T map(Record record);
    }
}



