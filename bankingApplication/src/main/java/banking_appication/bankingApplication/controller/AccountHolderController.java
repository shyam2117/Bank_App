package banking_appication.bankingApplication.controller;

import banking_appication.bankingApplication.service.AccountHolderService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
public class AccountHolderController {

    @Autowired
    private AccountHolderService accountHolderService;

    // create Account

    @PostMapping("/create")
    public JsonNode createAccount(@RequestBody JsonNode jsonNode){
        return accountHolderService.createAccount(jsonNode);
    }

    // getAllAccountHolder

    @GetMapping("/getAll")
    public JsonNode getAllAccountHolderData(){
        return accountHolderService.getAllAccountHolderData();
    }

    // getAccountHolderByAccountId
    @GetMapping("/{accountId}")
    public JsonNode getAccountHolderByAccountId(@PathVariable("accountId") Long accountId){
        return accountHolderService.getAccountHolderByAccountId(accountId);
    }

    // DeleteAccountHolder
    @DeleteMapping("/{accountId}")
    public JsonNode deleteAccountHolderByAccountId(@PathVariable("accountId") Long accountId){
        return accountHolderService.deleteAccountHolderByAccountId(accountId);
    }

    // Deposit --> money deposit in our bank account
    @GetMapping("/deposit/{accountId}")
    public JsonNode deposit(@PathVariable Long accountId, @RequestParam Double amount){
        return accountHolderService.deposit(accountId,amount);
    }

    //Withdrawal --> Money can be withdraw from bank account

    @GetMapping("/withdrawal/{accountId}")
    public JsonNode withdraw(@PathVariable Long accountId,@RequestParam Double amount){
        return accountHolderService.withdraw(accountId,amount);
    }

    // Download bank statement

    @GetMapping("statement/{accountId}")
    public JsonNode getBankStatement(@PathVariable Long accountId){
        return accountHolderService.getBankStatement(accountId);
    }

    // Download bank statement by date range --> fromDate to toDate

    @PostMapping("/statement/dateRange/{accountId}")
    public JsonNode getBankStatementByDateRange(@PathVariable Long accountId,@RequestBody JsonNode jsonNode){
        return accountHolderService.getBankStatementByDateRange(accountId,jsonNode);
    }

}
