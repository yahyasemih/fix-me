package ma.leet.fixme.market;

public class InstrumentMetadata {
  private final int brokerId;
  private final String name;
  private final String type;
  private final int quantity;
  private final int price;

  InstrumentMetadata(int brokerId, String name, String type, int quantity, int price) {
    this.brokerId = brokerId;
    this.name = name;
    this.type = type;
    this.quantity = quantity;
    this.price = price;
  }

  public int getBrokerId() {
    return brokerId;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public int getQuantity() {
    return quantity;
  }

  public int getPrice() {
    return price;
  }
}
