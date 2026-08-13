package com.fernando.sistema_assinaturas.core.service;

import com.fernando.sistema_assinaturas.core.domain.model.Plan;
import java.util.Arrays;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class PlanCatalogService {

	@Cacheable(cacheNames = "plans", cacheManager = "caffeineCacheManager")
	public List<Plan> findAll() {
		return Arrays.asList(Plan.values());
	}
}
