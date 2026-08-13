package com.fernando.sistema_assinaturas.dataprovider.database.mapper;

import com.fernando.sistema_assinaturas.core.domain.model.User;
import com.fernando.sistema_assinaturas.dataprovider.database.entity.UserJpaEntity;

public final class UserDatabaseMapper {

	private UserDatabaseMapper() {
	}

	public static UserJpaEntity toEntity(User user) {
		return UserJpaEntity.builder()
			.id(user.getId())
			.name(user.getName())
			.email(user.getEmail())
			.createdAt(user.getCreatedAt())
			.build();
	}

	public static User toDomain(UserJpaEntity entity) {
		return User.create(
			entity.getId(),
			entity.getName(),
			entity.getEmail(),
			entity.getCreatedAt()
		);
	}
}
