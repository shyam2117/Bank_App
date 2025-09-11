package banking_appication.bankingApplication.service;

import banking_appication.bankingApplication.exception.AccountNotFoundException;
import banking_appication.bankingApplication.exception.InsufficientBalanceException;
import banking_appication.bankingApplication.model.AccountHolder;
import banking_appication.bankingApplication.model.Transaction;
import banking_appication.bankingApplication.repository.AccountHolderRepository;
import banking_appication.bankingApplication.repository.TransactionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AccountHolderService {

    @Autowired
    private AccountHolderRepository accountHolderRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode createAccount(JsonNode jsonNode) {

        ObjectNode response = objectMapper.createObjectNode();

        AccountHolder accountHolder;

        if(jsonNode.has("accountId")){
            Long accountId= jsonNode.get("accountId").asLong();

            Optional<AccountHolder> optionalAccountHolder = accountHolderRepository.findById(accountId);
            if(optionalAccountHolder.isPresent()){
                accountHolder = optionalAccountHolder.get();
            }else{
                response.put("status","error");
                response.put("message","account holder does not exist");
                return response;
            }
        }else{
            accountHolder = new AccountHolder();
        }

        accountHolder.setAccountHolderName(jsonNode.has("accountHolderName")?jsonNode.get("accountHolderName").asText():null);
        accountHolder.setBalance(jsonNode.has("balance")?jsonNode.get("balance").asDouble():null);

        accountHolderRepository.save(accountHolder);

        response.put("status","success");
        response.put("message","account created or updated successfully");
        response.put("accountId",accountHolder.getAccountId());
        response.put("balance",accountHolder.getBalance());

        return response;
    }

    public JsonNode getAllAccountHolderData() {

        ObjectNode response = objectMapper.createObjectNode();

        List<AccountHolder> accountHolderList = accountHolderRepository.findAll();

        if(accountHolderList.isEmpty()){
            response.put("status","error");
            response.put("message","No account holder data present");

            return response;
        }

        ArrayNode arrayNode = objectMapper.createArrayNode();

        for(AccountHolder accountHolder:accountHolderList){
            ObjectNode accountNode = objectMapper.createObjectNode();

            accountNode.put("accountId",accountHolder.getAccountId());
            accountNode.put("accountHolderName",accountHolder.getAccountHolderName());
            accountNode.put("balance",accountHolder.getBalance());

            arrayNode.add(accountNode);
        }

        response.put("status","success");
        response.put("message","fetched the data successfully");
        response.set("AccountHolder",arrayNode);

        return response;
    }

    public JsonNode getAccountHolderByAccountId(Long accountId) {

        ObjectNode response = objectMapper.createObjectNode();

        Optional<AccountHolder> accountHolder = accountHolderRepository.findById(accountId);

        if(accountHolder.isPresent()){
            AccountHolder accountHolder1 = accountHolder.get();
            response.put("accountId",accountHolder1.getAccountId());
            response.put("accountHolderName",accountHolder1.getAccountHolderName());
            response.put("balance",accountHolder1.getBalance());

            response.put("status","success");
            response.put("message","data fetched successfully");

        }else{
            response.put("status","error");
            response.put("message","account does not exist");
        }


        return response;
    }

    public JsonNode deleteAccountHolderByAccountId(Long accountId) {

        ObjectNode response = objectMapper.createObjectNode();

        Optional<AccountHolder>optionalAccountHolder = accountHolderRepository.findById(accountId);
        if(optionalAccountHolder.isPresent()){
            AccountHolder accountHolder = optionalAccountHolder.get();
            accountHolderRepository.delete(accountHolder);

            response.put("status","success");
            response.put("message","data deleted successfully");
        }else{
            response.put("status","error");
            response.put("message","AccountId does not exist");
        }

        return response;
    }

    public JsonNode deposit(Long accountId, Double amount) {

        ObjectNode response = objectMapper.createObjectNode();

        AccountHolder accountHolder = accountHolderRepository.findById(accountId).orElseThrow(()->new AccountNotFoundException("Account with ID"+accountId+" not found"));

        Transaction transaction = new Transaction();
        transaction.setAccountId(accountId);
        transaction.setDate(LocalDate.now());
        transaction.setType("DEPOSIT");
        transaction.setAmount(amount);

        accountHolder.setBalance(accountHolder.getBalance()+amount); // Deposit--> amount=5000 , Balance = 10000 = 10000+5000=15000

        accountHolderRepository.save(accountHolder);
        transactionRepository.save(transaction);

        response.put("status","success");
        response.put("message","Deposit completed");
        response.put("newBalance",accountHolder.getBalance());

        return response;
    }

    public JsonNode withdraw(Long accountId, Double amount) {

        ObjectNode response = objectMapper.createObjectNode();

        AccountHolder accountHolder= accountHolderRepository.findById(accountId).orElseThrow(()->new AccountNotFoundException("Account with ID"+accountId+" not found"));

        // User cant withdraw the amount greater than the available balance
        if(amount>accountHolder.getBalance()){
            throw new InsufficientBalanceException("Insufficient balance for withdrawal");
        }

        Transaction transaction = new Transaction();
        transaction.setType("WITHDRAW");
        transaction.setDate(LocalDate.now());
        transaction.setAccountId(accountId);
        transaction.setAmount(amount);

        accountHolder.setBalance(accountHolder.getBalance()-amount); // fetching balance = 10000 , amount =5000 --> Balance = 10000-5000 = 5000

        accountHolderRepository.save(accountHolder);
        transactionRepository.save(transaction);

        response.put("status","success");
        response.put("message","Withdrawal completed");
        response.put("newBalance",accountHolder.getBalance());

        return response;

    }

    public JsonNode getBankStatement(Long accountId) {

        ObjectNode response = objectMapper.createObjectNode();

        AccountHolder accountHolder = accountHolderRepository.findById(accountId).orElseThrow(()->new AccountNotFoundException("Account with ID"+accountId+" not found"));

        List<Transaction>transactionList = transactionRepository.findByAccountId(accountId);

        ArrayNode arrayNode = objectMapper.createArrayNode();

        for(Transaction transaction:transactionList){
            ObjectNode txNode = objectMapper.createObjectNode();

            txNode.put("transactionId",transaction.getTransactionId());
            txNode.put("accountId",transaction.getAccountId());
            txNode.put("type",transaction.getType());
            txNode.put("amount",transaction.getAmount());
            txNode.put("date",transaction.getDate().toString());

            arrayNode.add(txNode);
        }

        response.put("accountId",accountId);
        response.put("accountHolderName",accountHolder.getAccountHolderName());
        response.put("currentBalance",accountHolder.getBalance());
        response.set("Transaction",arrayNode);

        return response;
    }

    public JsonNode getBankStatementByDateRange(Long accountId, JsonNode jsonNode) {

        ObjectNode response = objectMapper.createObjectNode();

        AccountHolder accountHolder = accountHolderRepository.findById(accountId).orElseThrow(()->new AccountNotFoundException("Account with ID"+accountId+" not found"));

        LocalDate fromDate = LocalDate.parse(jsonNode.get("fromDate").asText());
        LocalDate toDate = LocalDate.parse(jsonNode.get("toDate").asText());

        List<Transaction>transactionList = transactionRepository.findByAccountIdAndDateBetween(accountId,fromDate,toDate);

        ArrayNode arrayNode = objectMapper.createArrayNode();

        for(Transaction transaction:transactionList){
            ObjectNode txNode = objectMapper.createObjectNode();


            txNode.put("transactionId",transaction.getTransactionId());
            txNode.put("accountId",transaction.getAccountId());
            txNode.put("type",transaction.getType());
            txNode.put("amount",transaction.getAmount());
            txNode.put("date",transaction.getDate().toString());

            arrayNode.add(txNode);
        }

        response.put("accountId",accountId);
        response.put("accountHolderName",accountHolder.getAccountHolderName());
        response.put("currentBalance",accountHolder.getBalance());
        response.set("Transaction",arrayNode);

        return response;
    }
}
