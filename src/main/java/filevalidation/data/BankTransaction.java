package filevalidation.data;

import java.math.BigDecimal;
import java.util.Set;

public record BankTransaction(String reference, String accountNumber, String description, BigDecimal startBalance,
                              BigDecimal mutation, BigDecimal endBalance) {

    public boolean isValidTransaction(Set<String> transactionReferences) {
        if (!transactionReferences.add(reference())) {
            System.out.println("Duplicate transaction ID " + reference());
            return false; //duplicate reference ID
        }

        BigDecimal mutatedBalance = startBalance().add(mutation());
        if (mutatedBalance.doubleValue() != endBalance().doubleValue()) {
            System.out.println("Mutated balance " + mutatedBalance + " does not match with end balance for reference ID " + reference());
            return false;
        }
        return true;
    }
}
