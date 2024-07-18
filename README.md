
## To compile and run the Validation Service application.
mvn compile
mvn exec:java -Dexec.mainClass=filevalidation.service.BankTransactionValidationServiceImpl

This programme will take src/main/resources/records.json file as input and
processes it and generates a report src/main/resources/out/report.csv with invalid transactions

Assumptions
1. Start balance and End balance can be negative
2. There is no other validation on any field except what's mentioned in the assignment sheet.
3. File is already present to be processed under src/main/resources
4. Output report generated is in csv format and will be stored under the folder sec/main/resources/out.

What can be improved further: 
1. System outs can be replaced with proper loggers
2. We can add jacoco to put a benchmark figure for line and branch coverage in junits
3. Exception Handling
