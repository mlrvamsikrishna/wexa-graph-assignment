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

        return new CareerGapAnalysis(
                person,
                role,
                repository.findMissingSkills(personId, roleId),
                repository.findCourseRecommendations(personId, roleId),
                repository.findMentorRecommendations(personId, roleId)
        );
    }
}

