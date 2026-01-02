package service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Locale;

public class BankingApiService {

    private static final String BASE_URL = "http://147.27.70.44:3020";
    private final HttpClient client;

    public BankingApiService() {
        this.client = HttpClient.newHttpClient();
    }

    //SEPA TRANSFER
    public String sendSepaTransfer(double amount, String name, String iban, String bic, String bankName, String requestedDate, String charges) throws Exception {
        String json = String.format(Locale.US,
            "{" +
            "\"amount\": %.2f," +
            "\"creditor\": { \"name\": \"%s\", \"iban\": \"%s\" }," +
            "\"creditorBank\": { \"bic\": \"%s\", \"name\": \"%s\" }," +
            "\"execution\": { \"requestedDate\": \"%s\", \"charges\": \"%s\" }" +
            "}",
            amount, name, iban, bic, bankName, requestedDate, charges
        );
        return sendRequest("/transfer/sepa", json);
    }

    //SWIFT TRANSFER
    public String sendSwiftTransfer(double amount, String currency, String name, String address, String account, 
                                    String bankName, String swiftCode, String country, String chargingModel) throws Exception {
        
        String json = String.format(Locale.US,
            "{" +
            "\"currency\": \"%s\"," +
            "\"amount\": %.2f," +
            "\"beneficiary\": { \"name\": \"%s\", \"address\": \"%s\", \"account\": \"%s\" }," +
            "\"beneficiaryBank\": { \"name\": \"%s\", \"swiftCode\": \"%s\", \"country\": \"%s\" }," +
            "\"fees\": { \"chargingModel\": \"%s\" }," +
            "\"correspondentBank\": { \"required\": false }" + 
            "}",
            currency, amount, name, address, account, bankName, swiftCode, country, chargingModel
        );

        return sendRequest("/transfer/swift", json);
    }

    //GENERIC SEND
    private String sendRequest(String endpoint, String jsonBody) throws Exception {
        System.out.println("Sending JSON: " + jsonBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return "SUCCESS:" + response.body();
        } else {
            return "FAILED (" + response.statusCode() + "):" + response.body();
        }
    }
}