package com.tritva.Evently.mapper;

import com.tritva.Evently.model.dto.CreateEventDto;
import com.tritva.Evently.model.dto.EventResponseDto;
import com.tritva.Evently.model.entity.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface EventMapper {

    /**
     * Convert from Event entity to EventResponseDto
     * Includes calculation of available tickets
     */
    @Mapping(source = "organiser.fullName", target = "organiserName")
    @Mapping(source = "category.categoryName", target = "categoryName")
    @Mapping(target = "availableTickets", expression = "java(calculateAvailableTickets(event))")
    EventResponseDto toDto(Event event);

    /**
     * Convert from CreateEventDto to Event entity
     * Note: organiser and category will be set manually in the service
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organiser", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Event toEntity(CreateEventDto createEventDto);

    /**
     * Calculate available tickets for an event
     * @param event the event entity
     * @return number of available tickets (capacity - tickets sold)
     */
    default long calculateAvailableTickets(Event event) {
        if (event == null) {
            return 0;
        }
        int ticketsSold = event.getTickets() != null ? event.getTickets().size() : 0;
        return Math.max(0, event.getCapacity() - ticketsSold);
    }
}