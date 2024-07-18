package filevalidation.service;

import filevalidation.data.BankTransaction;

import java.io.IOException;
import java.util.List;

public interface BankTransactionValidationService {

    List<BankTransaction> validateBankTransactions(List<BankTransaction> transactions);

    void generateInvalidTransactionsReport(List<BankTransaction> invalidTransactions, String reportFilePath) throws IOException;

    List<BankTransaction> readTransactionsFromFile(String filePath) throws IOException;

    List<BankTransaction> readTransactionFromJsonFile(String filePath) throws IOException;

    List<BankTransaction> readTransactionFromCsvFile(String filePath) throws IOException;
}
