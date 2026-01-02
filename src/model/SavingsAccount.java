package model;
public class SavingsAccount extends Account {
    public SavingsAccount(String iban, String owner, double balance) { super(iban, owner, balance); }
    @Override public String getAccountType() { return "Ταμιευτηρίου"; }
}