package me.mortaldev.jbeaster.modules.bunnyrace;

public class PlayerBet {

  private static final double PAYOUT_PERCENT = 0.0001067;
  private final int bunnyID;
  private final double amount;
  private boolean cashedIn;

  public PlayerBet(int bunnyID, double amount) {
    this.bunnyID = bunnyID;
    this.amount = amount;
    this.cashedIn = false;
  }

  public boolean isCashedIn() {
    return cashedIn;
  }

  public void cashIn() {
    cashedIn = true;
  }

  public int getBunnyID() {
    return bunnyID;
  }

  public double getAmount() {
    return amount;
  }

  /**
   * Calculate the payout for a player's bet.
   *
   * @return The number of eggs the player will win.
   */
  public int eggPayout() {
    return (int) Math.floor(amount * PAYOUT_PERCENT);
  }
}
