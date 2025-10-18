package com.tritva.Evently.mapper;

import com.tritva.Evently.model.dto.PaymentDto;
import com.tritva.Evently.model.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "event.id", target = "eventId")
    PaymentDto toDto(Payment payment);

    Payment toEntity(PaymentDto paymentDto);
}

