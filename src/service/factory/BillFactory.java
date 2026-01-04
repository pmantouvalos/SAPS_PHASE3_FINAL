package service.factory;

import model.entities.Bill;
import service.builder.BillBuilder;
import java.time.LocalDate;

public class BillFactory {

    public static Bill createBill(String ownerAfm, String rfCode, String provider, double amount, String desc) {
        // Default: Ημερομηνία έκδοσης τώρα, λήξη σε 30 μέρες
        return new BillBuilder()
                .setOwnerAfm(ownerAfm)
                .setRfCode(rfCode)
                .setProvider(provider)
                .setAmount(amount)
                .setDescription(desc)
                .setIssueDate(LocalDate.now())
                .setDueDate(LocalDate.now().plusDays(30))
                .build();
    }
    
    // Πλήρης μέθοδος (π.χ. για φόρτωση από CSV)
    public static Bill createBillFull(String ownerAfm, String rfCode, String provider, double amount, 
                                      String desc, LocalDate issue, LocalDate due) {
        return new BillBuilder()
                .setOwnerAfm(ownerAfm)
                .setRfCode(rfCode)
                .setProvider(provider)
                .setAmount(amount)
                .setDescription(desc)
                .setIssueDate(issue)
                .setDueDate(due)
                .build();
    }
}