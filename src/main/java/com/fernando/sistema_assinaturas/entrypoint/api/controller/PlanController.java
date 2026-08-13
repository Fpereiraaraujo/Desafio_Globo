package com.fernando.sistema_assinaturas.entrypoint.api.controller;

import com.fernando.sistema_assinaturas.core.service.PlanCatalogService;
import com.fernando.sistema_assinaturas.core.domain.model.Plan;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
public class PlanController {

	private final PlanCatalogService planCatalogService;

	@GetMapping
	public List<Plan> findAll() {
		return planCatalogService.findAll();
	}
}
