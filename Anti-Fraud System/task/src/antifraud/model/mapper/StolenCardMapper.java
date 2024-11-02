package antifraud.model.mapper;

import antifraud.entity.StolenCard;
import antifraud.model.StolenCardDTO;
import org.springframework.stereotype.Component;

@Component
public class StolenCardMapper {
    public StolenCard toEntity(StolenCardDTO stolenCardDTO) {
        StolenCard entity = new StolenCard();
        entity.setId(stolenCardDTO.getId());
        entity.setNumber(stolenCardDTO.getNumber());
        return entity;
    }

    public StolenCardDTO toDto(StolenCard entity) {
        return StolenCardDTO.builder()
                .id(entity.getId())
                .number(entity.getNumber())
                .build();
    }
}
