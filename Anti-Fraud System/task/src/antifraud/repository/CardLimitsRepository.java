package antifraud.repository;

import antifraud.entity.CardLimits;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardLimitsRepository extends CrudRepository<CardLimits, String> {
}
