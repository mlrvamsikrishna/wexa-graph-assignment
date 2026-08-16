package com.wexa.graph.model;

import java.util.List;

public record CourseRecommendation(String id, String title, List<String> coveredSkills, long coverageCount) {
}

