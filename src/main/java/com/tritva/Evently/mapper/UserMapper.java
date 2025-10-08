package com.tritva.assessment.mapper;

import com.tritva.assessment.model.dto.RegisterDto;
import com.tritva.assessment.model.dto.UserResponseDto;
import com.tritva.assessment.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserResponseDto toDto(User user);
}
