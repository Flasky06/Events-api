package com.tritva.Evently.mapper;

import com.tritva.Evently.model.dto.UserResponseDto;
import com.tritva.Evently.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {

    /**
     * Convert User entity to UserResponseDto
     * MapStruct will automatically map fields with matching names
     * @param user the user entity
     * @return UserResponseDto
     */
    UserResponseDto toDto(User user);
}