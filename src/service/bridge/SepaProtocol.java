package service.bridge;

import integration.BankingApiService;
import model.entities.Account;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SepaProtocol implements TransferProtocol {
    
    private BankingApiService apiService;
    private String beneficiaryName;
    private String bic;
    private String bankName;
    private String requestedDate;
    private String charges;

    public SepaProtocol(String beneficiaryName, String bic, String bankName, String requestedDate, String charges) {
        this.apiService = new BankingApiService();
        this.beneficiaryName = beneficiaryName;
        this.bic = bic;
        this.bankName = bankName;
        this.requestedDate = requestedDate;
        this.charges = charges;
    }

    @Override
    public String executeTransfer(Account source, String targetIban, double amount) throws Exception {
        String response = apiService.sendSepaTransfer(amount, beneficiaryName, targetIban, bic, bankName, requestedDate, charges);

        if (response.startsWith("SUCCESS:")) {
            // Εξαγωγή του transaction_id από το JSON με Regex (για να μην βάλουμε εξωτερικές βιβλιοθήκες)
            // JSON format: { "status": "success", "message": "...", "transaction_id": "XYZ-123" }
            String jsonBody = response.substring("SUCCESS:".length());
            return extractTransactionId(jsonBody);
        } else {
            // Εξαγωγή του μηνύματος λάθους
            throw new Exception("SEPA Failed: " + response);
        }
    }

    // Βοηθητική μέθοδος για να πάρουμε το ID από το JSON
    private String extractTransactionId(String json) {
        Pattern p = Pattern.compile("\"transaction_id\"\\s*:\\s*\"([^\"]+)\""); // 
        Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return "UNKNOWN_ID"; // Fallback αν δεν βρεθεί
    }

    @Override
    public String getProtocolName() { return "SEPA"; }
}