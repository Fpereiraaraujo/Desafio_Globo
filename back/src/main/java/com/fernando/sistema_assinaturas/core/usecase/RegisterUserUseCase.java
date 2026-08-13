package com.fernando.sistema_assinaturas.core.usecase;

import com.fernando.sistema_assinaturas.core.domain.model.User;
import com.fernando.sistema_assinaturas.core.domain.param.RegisterUserParam;

public interface RegisterUserUseCase {

	User execute(RegisterUserParam param);
}
