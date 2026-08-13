package com.fernando.sistema_assinaturas.core.usecase;

import com.fernando.sistema_assinaturas.core.domain.model.RenewalProcessingResult;
import com.fernando.sistema_assinaturas.core.domain.param.ProcessRenewalParam;

public interface ProcessRenewalUseCase {

	RenewalProcessingResult execute(ProcessRenewalParam param);
}
