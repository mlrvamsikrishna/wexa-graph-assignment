package com.wexa.graph.model;

import java.util.List;

public record CareerGapAnalysis(
        PersonOption person,
        RoleOption targetRole,
        List<MissingSkill> missingSkills,
        List<CourseRecommendation> courseRecommendations,
        List<MentorRecommendation> mentorRecommendations) {
}

