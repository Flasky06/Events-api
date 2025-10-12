package com.tritva.Evently.mapper;

import com.tritva.Evently.model.dto.TicketDto;
import com.tritva.Evently.model.entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TicketMapper {

    // Convert entity → DTO
    @Mapping(source = "event.id", target = "eventId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "payment.id", target = "paymentId")
    TicketDto toDto(Ticket ticket);

    // Convert DTO → entity (relationships set manually later)
    Ticket toEntity(TicketDto ticketDto);
}
