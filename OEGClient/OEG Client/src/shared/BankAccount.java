package shared;

/**
 * @author bcheek/dlyle (it's a work/non-work relationship.)
 */
public class BankAccount {
    /**
     * startingBalance is the starting balance of the account.
     */
    private static int startingBalance;
    /**
     * curBalence is the current balance of the account, updated each round.
     */
    private int        curBalance;
    /**
     * UnusableFunds are funds that have been allocated to expenses in the
     * current round that have not been processed by the Director, IE drilling
     * or bid requests.
     */
    protected int      unusableFunds;
    /**
     * Income is the current rate at which the client's bank account by each
     * round.
     */
    private int        income;

    /**
     * Constructor for the BankAccount Class. Sets the current balance to the
     * starting balance, sets income to 0, and sets unuseableFunds to 0.
     */
    public BankAccount() {
        curBalance = startingBalance;
        income = 0;
        unusableFunds = 0;
    }

    /**
     * This method is used by the XML parser to set the starting balance
     * 
     * @param start
     *            is passed from the XML parser
     */
    public static void setStart(int start) {
        startingBalance = start;
    }

    /**
     * Returns the starting balance
     * 
     * @return the starting balance
     */
    public int getStart() {
        return startingBalance;
    }

    /**
     * returns the balance of the account minus the unuseableFunds, representing
     * the amount that the user has available to use.
     * 
     * @return adjustedBalence, the amount the user has available to use.
     */
    public int getAdjustedBalance() {
        return (curBalance) - (unusableFunds);
    }

    /**
     * returns the current balance of the bank account
     * 
     * @return curBalance The current balance of the bank account
     */
    public int getBalance() {
        return curBalance;
    }

    /**
     * Changes the curBalance variable by the amount passed. amount can be
     * positive or negative depending on need.
     * 
     * @param amount
     *            is the amount of funds by which to increase/decrease the bank
     *            account
     */
    public void updateFunds(int amount) {
        curBalance += amount;
    }

    /**
     * Sets curBalance to the passed amount.
     * 
     * @param amount
     *            The amount to set curBalance to.
     */
    public void setBalance(int amount) {
        curBalance = amount;
    }

    /**
     * Sets a user's income to a passed amount.
     * 
     * @param inIncome
     *            The amount to set the income to.
     */
    public void adjustIncome(int inIncome) {
        income += inIncome;
    }

    /**
     * Returns the current income rate.
     * 
     * @return income The current income rate.
     */
    public int getIncome() {
        return income;
    }

    /**
     * Adds a passed amount to the unuseableFunds variable.
     * 
     * @param amount
     *            The amount to add to unuseableFunds
     */
    public void changeUnusable(int amount) {
        unusableFunds += amount;
    }

    /**
     * Resets the unusableFunds variable to 0
     */
    public void resetUnusable() {
        unusableFunds = 0;
    }

    public void applyIncome() {
        curBalance += income;

    }

}