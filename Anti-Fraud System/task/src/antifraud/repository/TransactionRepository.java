package antifraud.repository;

import antifraud.entity.Transaction;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, Long> {

    List<Transaction> findAllByNumber(@NotEmpty String number);
}
