package model;

public class BusinessAccount extends Account{
	public BusinessAccount(String iban, String owner, double balance) { super(iban, owner, balance); }
    @Override public String getAccountType() { return "Επαγγελματικός"; }
}
