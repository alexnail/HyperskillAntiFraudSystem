package antifraud.repository;

import antifraud.entity.SuspiciousIp;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SuspiciousIpRepository extends CrudRepository<SuspiciousIp, Long> {
    Optional<SuspiciousIp> findSuspiciousIpByIp(String ip);

    void deleteSuspiciousIpById(Long id);
}
