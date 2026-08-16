package com.wexa.graph.model;

public record MentorRecommendation(String id, String name, long hops, long matchedSkills, long totalMissingSkills,
                                   double coverage) {
}

