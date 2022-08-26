package ma.leet.fixme.market;

public class Instrument implements Comparable<Instrument> {
  private final String name;
  private int price;

  Instrument(String name, int price) {
    this.name = name;
    this.price = price;
  }

  String getName() {
    return name;
  }

  int getPrice() {
    return price;
  }

  void setPrice(int price) {
    this.price = price;
  }

  @Override
  public int compareTo(Instrument o) {
    return name.compareTo(o.name);
  }
}
