package model;
public class CurrentAccount extends Account {
    public CurrentAccount(String iban, String owner, double balance) { super(iban, owner, balance); }
    @Override public String getAccountType() { return "Τρεχούμενος"; }
}