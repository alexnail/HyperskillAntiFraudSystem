package antifraud.model.mapper;

import antifraud.entity.SuspiciousIp;
import antifraud.model.SuspiciousIpDTO;
import org.springframework.stereotype.Component;

@Component
public class SuspiciousIpMapper {

    public SuspiciousIpDTO toDto(SuspiciousIp ip) {
        return SuspiciousIpDTO.builder()
                .id(ip.getId())
                .ip(ip.getIp())
                .build();
    }

    public SuspiciousIp toEntity(SuspiciousIpDTO ipDTO) {
        SuspiciousIp entity = new SuspiciousIp();
        entity.setId(ipDTO.getId());
        entity.setIp(ipDTO.getIp());
        return entity;
    }
}
