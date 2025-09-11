package banking_appication.bankingApplication.repository;

import banking_appication.bankingApplication.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction,Long> {
    List<Transaction> findByAccountId(Long accountId);

    List<Transaction> findByAccountIdAndDateBetween(Long accountId, LocalDate fromDate, LocalDate toDate);
}
