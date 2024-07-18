import filevalidation.data.BankTransaction;
import filevalidation.service.BankTransactionValidationService;
import filevalidation.service.BankTransactionValidationServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.SequencedCollection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BankTransactionValidationServiceTest {

    BankTransactionValidationService bankTransactionValidationService = new BankTransactionValidationServiceImpl();

    @DisplayName("Test that other than csv and json, no file type is supported")
    @Test
    void testInvalidFileType() throws IOException {
        String filePath = "src/test/resources/records.txt";
        Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
            bankTransactionValidationService.readTransactionsFromFile(filePath);
        });
        assertEquals("Unsupported file received: src/test/resources/records.txt", exception.getMessage());
    }

    @DisplayName("Read transactions from csv file")
    @Test
    void testTransactionsReadFromCsvFile() throws IOException {
        String filePath = "src/test/resources/records.csv";
        SequencedCollection<BankTransaction> bankTransactions = bankTransactionValidationService.readTransactionFromCsvFile(filePath);
        assertEquals(10, bankTransactions.size());
        assertEquals("194261", bankTransactions.getFirst().reference());
    }

    @DisplayName("Read transactions from json file")
    @Test
    void testTransactionsReadFromJsonFile() throws IOException {
        String filePath = "src/test/resources/records.json";
        SequencedCollection<BankTransaction> bankTransactions = bankTransactionValidationService.readTransactionFromJsonFile(filePath);
        assertEquals(10, bankTransactions.size());
        assertEquals("130498", bankTransactions.getFirst().reference());
    }

    @DisplayName("Testing validation that all txn references should be unique")
    @Test
    public void testValidateTransacationsWithDuplicates() throws IOException {

        SequencedCollection<BankTransaction> invalidTransactions = bankTransactionValidationService
                .validateBankTransactions(createListOfBankTransactionsWithDuplicates());
        assertEquals(1, invalidTransactions.size());
        assertEquals("NL91RABO0315273630", invalidTransactions.getFirst().accountNumber());
    }

    @DisplayName("Testing validation that end balance should match with start balance and mutation sum")
    @Test
    public void testValidateTransacationsWithIncorrectEndBalance() throws IOException {

        SequencedCollection<BankTransaction> invalidTransactions = bankTransactionValidationService
                .validateBankTransactions(createListOfBankTransactionsWithIncorrectEndBalance());
        assertEquals(1, invalidTransactions.size());
        assertEquals("NL91RABO0315273630", invalidTransactions.getFirst().accountNumber());
    }

    @DisplayName("Testing validation where all transactions all valid")
    @Test
    public void testValidateTransacationsWithAllValid() throws IOException {

        SequencedCollection<BankTransaction> invalidTransactions = bankTransactionValidationService
                .validateBankTransactions(createListOfValidBankTransactions());
        assertEquals(0, invalidTransactions.size());
    }

    @DisplayName("Test validation where all transactions all valid")
    @Test
    public void testGenerateInvalidTxnReport() throws IOException {
        String filePath = "src/test/resources/out/report.csv";
        bankTransactionValidationService
                .generateInvalidTransactionsReport(createListOfBankTransactionsWithIncorrectEndBalance(), filePath);
        int rowsInReport = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                rowsInReport++;
                String[] fields = line.split(",");
                String description = fields[1];
                assertEquals(description, "Test");
            }
        }
        assertEquals(3, rowsInReport);
    }

    List<BankTransaction> createListOfValidBankTransactions() {
        return List.of(
                new BankTransaction("1", "NL91RABO0315273637", "Test", new BigDecimal(0.0), new BigDecimal(2.5), new BigDecimal(2.5)),
                new BankTransaction("3", "NL91RABO0315273633", "Test3", new BigDecimal(-2), new BigDecimal(2), new BigDecimal(0)),
                new BankTransaction("2", "NL91RABO0315273630", "Test2", new BigDecimal(0.0), new BigDecimal(-5), new BigDecimal(-5)));
    }

    List<BankTransaction> createListOfBankTransactionsWithDuplicates() {
        return List.of(
                new BankTransaction("1", "NL91RABO0315273637", "Test1", new BigDecimal(10.0), new BigDecimal(-5), new BigDecimal(5)),
                new BankTransaction("1", "NL91RABO0315273630", "Test2", new BigDecimal(10.0), new BigDecimal(-5), new BigDecimal(5)));
    }

    List<BankTransaction> createListOfBankTransactionsWithIncorrectEndBalance() {
        return List.of(
                new BankTransaction("1", "NL91RABO0315273637", "Test", new BigDecimal(10.0), new BigDecimal(-5), new BigDecimal(5)),
                new BankTransaction("2", "NL91RABO0315273630", "Test", new BigDecimal(19.0), new BigDecimal(-7.2), new BigDecimal(5)),
                new BankTransaction("3", "NL91RABO0315273631", "Test", new BigDecimal(19.0), new BigDecimal(-7.2), new BigDecimal(11.8)));
    }

}
