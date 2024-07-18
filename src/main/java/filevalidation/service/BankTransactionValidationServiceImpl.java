package filevalidation.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import filevalidation.constants.ApplicationConstants;
import filevalidation.data.BankTransaction;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BankTransactionValidationServiceImpl implements BankTransactionValidationService {
    ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        BankTransactionValidationServiceImpl fileValidatorServiceImpl = new BankTransactionValidationServiceImpl();
        try {
            List<BankTransaction> transactions = fileValidatorServiceImpl
                    .readTransactionsFromFile(ApplicationConstants.in_file_path);
            List<BankTransaction> invalidTransactions = fileValidatorServiceImpl.validateBankTransactions(transactions);
            fileValidatorServiceImpl.generateInvalidTransactionsReport(invalidTransactions, ApplicationConstants.out_report_path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    @Override
    public List<BankTransaction> validateBankTransactions(List<BankTransaction> transactions) {
        List<BankTransaction> invalidTransactions = new ArrayList<>();
        Set<String> transactionReferences = new HashSet<>();
        transactions.forEach(bankTransaction -> {
            if (!bankTransaction.isValidTransaction(transactionReferences)) {
                invalidTransactions.add(bankTransaction);
            }
        });
        return invalidTransactions;
    }


    @Override
    public List<BankTransaction> readTransactionsFromFile(String filePath) throws IOException {
        String fileType = getFileType(filePath);
        return switch (fileType) {
            case ApplicationConstants.csv_file_extension -> readTransactionFromCsvFile(filePath);
            case ApplicationConstants.json_file_extension -> readTransactionFromJsonFile(filePath);
            default -> throw new IllegalArgumentException(ApplicationConstants.unsupported_file_extension + filePath);
        };
    }

    private String getFileType(String filePath) {
        int lastDotIndex = filePath.lastIndexOf('.');
        if (lastDotIndex == -1) {
            throw new IllegalArgumentException("No file extension found for file: " + filePath);
        }
        return filePath.substring(lastDotIndex + 1).toLowerCase();
    }

    @Override
    public List<BankTransaction> readTransactionFromJsonFile(String filePath) throws IOException {
        return mapper.readValue(new File(filePath), new TypeReference<List<BankTransaction>>() {
        });
    }

    @Override
    public List<BankTransaction> readTransactionFromCsvFile(String filePath) throws IOException {
        List<BankTransaction> bankTransactionList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] fields = line.split(",");
                if (fields.length == 6) {
                    String reference = fields[0];
                    String accountNumber = fields[1];
                    String description = fields[2];
                    BigDecimal startBalance = null;
                    BigDecimal mutation = null;
                    BigDecimal endBalance = null;
                    try {
                        startBalance = new BigDecimal(fields[3]);
                        mutation = new BigDecimal(fields[4]);
                        endBalance = new BigDecimal(fields[5]);
                    } catch (NumberFormatException e) {
                        //throw exception
                    }
                    BankTransaction bankTransaction = new BankTransaction(reference, accountNumber, description,
                            startBalance, mutation, endBalance);
                    bankTransactionList.add(bankTransaction);
                }

            }
        }
        return bankTransactionList;
    }

    @Override
    public void generateInvalidTransactionsReport(List<BankTransaction> invalidTransactions, String reportFilePath) throws IOException {
        try (FileWriter writer = new FileWriter((reportFilePath))) {
            writer.write(ApplicationConstants.out_report_header);
            for (BankTransaction invalidTransaction : invalidTransactions) {
                String invalidTransactionStr = invalidTransaction.reference() + "," + invalidTransaction.description() + "\n";
                writer.write(invalidTransactionStr);
            }
            System.out.println("Invalid Transactions Report generated at path: " + reportFilePath);
        }
    }
}
