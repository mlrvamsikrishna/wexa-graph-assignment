package com.wexa.graph.service;

import com.wexa.graph.model.PersonOption;
import com.wexa.graph.model.RoleOption;

import java.util.List;

public record CatalogResponse(List<PersonOption> people, List<RoleOption> roles) {
}
