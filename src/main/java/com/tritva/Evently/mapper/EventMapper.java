package com.tritva.Evently.mapper;

import com.tritva.Evently.model.dto.CreateEventDto;
import com.tritva.Evently.model.dto.EventResponseDto;
import com.tritva.Evently.model.entity.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface EventMapper {

    // Convert from Event entity → EventResponseDto
    @Mapping(source = "organiser.fullName", target = "organiserName")
    @Mapping(source = "category.categoryName", target = "categoryName")
    EventResponseDto toDto(Event event);

    // Convert from CreateEventDto → Event entity
    // organiser and category will be set manually in the service
    Event toEntity(CreateEventDto createEventDto);
}
