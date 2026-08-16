package com.wexa.graph.service;

import com.wexa.graph.model.CareerGapAnalysis;
import com.wexa.graph.repository.CareerGraphRepository;
import org.springframework.stereotype.Service;

@Service
public class CareerGraphService {

    private final CareerGraphRepository repository;

    public CareerGraphService(CareerGraphRepository repository) {
        this.repository = repository;
    }

    public CatalogResponse fetchCatalog() {
        return new CatalogResponse(repository.findPeople(), repository.findRoles());
    }

    public CareerGapAnalysis analyze(String personId, String roleId) {
        var person = repository.findPerson(personId);
        var role = repository.findRole(roleId);
        var missingSkills = repository.findMissingSkills(personId, roleId);
        var missingSkillIds = missingSkills.stream().map(skill -> skill.id()).toList();

        var courseRecommendations = missingSkills.isEmpty()
                ? repository.findRoleAlignedCourses(roleId)
                : repository.findCourseRecommendations(personId, roleId);

        var mentorRecommendations = repository.findMentorRecommendationsForMissingSkills(personId, missingSkillIds);

        return new CareerGapAnalysis(
                person,
                role,
                missingSkills,
                courseRecommendations,
                mentorRecommendations
        );
    }
}

