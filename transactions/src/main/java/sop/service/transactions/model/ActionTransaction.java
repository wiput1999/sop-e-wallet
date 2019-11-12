package sop.service.transactions.model;

public class ActionTransaction {

    private  boolean status;
    private String action;
    private double balance;
    private String currency_source;
    private String currency_destination;
    Account account_source;
    Account account_destination;

    public ActionTransaction() {
    }

    public ActionTransaction(boolean status, String action, double balance, String currency_source, Account account_source) {
        this.status = status;
        this.action = action;
        this.balance = balance;
        this.currency_source = currency_source;
        this.account_source = account_source;
    }

    public ActionTransaction(boolean status, String action, double balance, String currency_source, String currency_destination, Account account_source) {
        this.status = status;
        this.action = action;
        this.balance = balance;
        this.currency_source = currency_source;
        this.currency_destination = currency_destination;
        this.account_source = account_source;
    }

    public ActionTransaction(boolean status, String action, double balance, String currency_source, String currency_destination, Account account_source, Account account_destination) {
        this.status = status;
        this.action = action;
        this.balance = balance;
        this.currency_source = currency_source;
        this.currency_destination = currency_destination;
        this.account_source = account_source;
        this.account_destination = account_destination;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getCurrency_source() {
        return currency_source;
    }

    public void setCurrency_source(String currency_source) {
        this.currency_source = currency_source;
    }

    public String getCurrency_destination() {
        return currency_destination;
    }

    public void setCurrency_destination(String currency_destination) {
        this.currency_destination = currency_destination;
    }

    public Account getAccount_source() {
        return account_source;
    }

    public void setAccount_source(Account account_source) {
        this.account_source = account_source;
    }

    public Account getAccount_destination() {
        return account_destination;
    }

    public void setAccount_destination(Account account_destination) {
        this.account_destination = account_destination;
    }
}
