package com.wexa.graph.service;

import com.wexa.graph.model.CourseRecommendation;
import com.wexa.graph.model.MentorRecommendation;
import com.wexa.graph.model.MissingSkill;
import com.wexa.graph.model.PersonOption;
import com.wexa.graph.model.RoleOption;
import com.wexa.graph.repository.CareerGraphRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CareerGraphServiceTest {

    @Test
    void shouldAggregateAnalysisFromRepository() {
        CareerGraphRepository repository = mock(CareerGraphRepository.class);
        CareerGraphService service = new CareerGraphService(repository);

        when(repository.findPerson("person_ava")).thenReturn(new PersonOption("person_ava", "Ava"));
        when(repository.findRole("role_graph")).thenReturn(new RoleOption("role_graph", "Graph Data Engineer"));
        when(repository.findMissingSkills("person_ava", "role_graph"))
                .thenReturn(List.of(new MissingSkill("skill_cypher", "Cypher", 5)));
        when(repository.findCourseRecommendations("person_ava", "role_graph"))
                .thenReturn(List.of(new CourseRecommendation("course_cypher", "Cypher Query Masterclass", List.of("Cypher"), 1)));
        when(repository.findMentorRecommendations("person_ava", "role_graph"))
                .thenReturn(List.of(new MentorRecommendation("person_liam", "Liam", 2, 1, 2, 0.5)));

        var result = service.analyze("person_ava", "role_graph");

        assertEquals("Ava", result.person().name());
        assertEquals("Graph Data Engineer", result.targetRole().title());
        assertEquals(1, result.missingSkills().size());
        assertEquals(1, result.courseRecommendations().size());
        assertEquals(1, result.mentorRecommendations().size());
    }
}

