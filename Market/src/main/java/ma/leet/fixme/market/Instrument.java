package ma.leet.fixme.market;

import java.util.Objects;

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

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Instrument)) {
      return false;
    }
    return name.equals(((Instrument)o).getName());
  }
}
