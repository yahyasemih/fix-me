package ma.leet.fixme.market;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Market {
  private final Map<Instrument, Integer> instruments;

  Market() {
    this.instruments = new ConcurrentHashMap<>(); // market is initially empty
  }

  Instrument findInstrumentByName(String name) {
    Optional<Instrument> instrument = instruments.entrySet().stream().filter(
        e -> e.getKey().getName().equals(name)).findAny().map(Map.Entry::getKey);
    if (instrument.isEmpty()) {
      return null;
    } else{
      return instrument.get();
    }
  }

  boolean instrumentExists(Instrument instrument) {
    return instruments.containsKey(instrument);
  }

  boolean instrumentExists(String name) {
    Instrument instrument = new Instrument(name, 0); // we don't care about the price
    return instrumentExists(instrument);
  }

  boolean updateInstrumentPrice(Instrument instrument, int newPrice) {
    if (!instrumentExists(instrument)) {
      return false;
    }
    instruments.put(instrument, newPrice);
    return true;
  }
}
