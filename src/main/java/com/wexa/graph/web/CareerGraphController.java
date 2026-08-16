package com.wexa.graph.web;

import com.wexa.graph.model.CareerGapAnalysis;
import com.wexa.graph.service.CareerGraphService;
import com.wexa.graph.service.CatalogResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CareerGraphController {

    private final CareerGraphService service;

    public CareerGraphController(CareerGraphService service) {
        this.service = service;
    }

    @GetMapping("/catalog")
    public CatalogResponse catalog() {
        return service.fetchCatalog();
    }

    @PostMapping("/analysis")
    public CareerGapAnalysis analyze(@RequestBody AnalysisRequest request) {
        if (request.personId() == null || request.personId().isBlank()) {
            throw new IllegalArgumentException("personId is required");
        }
        if (request.roleId() == null || request.roleId().isBlank()) {
            throw new IllegalArgumentException("roleId is required");
        }
        return service.analyze(request.personId(), request.roleId());
    }
}

