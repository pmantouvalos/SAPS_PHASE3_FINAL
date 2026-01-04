package service.bridge;

import integration.BankingApiService;
import model.entities.Account;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SwiftProtocol implements TransferProtocol {

    private BankingApiService apiService;
    // ... (τα πεδία παραμένουν ίδια)
    private String currency;
    private String beneficiaryName;
    private String address;
    private String bankName;
    private String swiftCode;
    private String country;
    private String chargingModel;

    public SwiftProtocol(String beneficiaryName, String address, String bankName, 
                         String swiftCode, String country, String currency, String chargingModel) {
        this.apiService = new BankingApiService();
        this.beneficiaryName = beneficiaryName;
        this.address = address;
        this.bankName = bankName;
        this.swiftCode = swiftCode;
        this.country = country;
        this.currency = currency;
        this.chargingModel = chargingModel;
    }

    @Override
    public String executeTransfer(Account source, String targetAccount, double amount) throws Exception {
        String response = apiService.sendSwiftTransfer(amount, currency, beneficiaryName, address, targetAccount, bankName, swiftCode, country, chargingModel);

        if (response.startsWith("SUCCESS:")) {
            String jsonBody = response.substring("SUCCESS:".length());
            return extractTransactionId(jsonBody);
        } else {
            throw new Exception("SWIFT Failed: " + response);
        }
    }
    
    private String extractTransactionId(String json) {
        Pattern p = Pattern.compile("\"transaction_id\"\\s*:\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(json);
        if (m.find()) return m.group(1);
        return "UNKNOWN_ID";
    }

    @Override
    public String getProtocolName() { return "SWIFT"; }
}