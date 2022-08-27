package ma.leet.fixme.market;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static java.lang.Integer.parseInt;

public class Market implements AutoCloseable {
  private static final LogManager logManager = LogManager.getLogManager();
  private final static Logger logger = Logger.getLogger(Market.class.getSimpleName());

  static {
    try {
      logManager.readConfiguration(Market.class.getClassLoader().getResourceAsStream("logger.properties"));
    } catch (IOException exception) {
      logger.log(Level.SEVERE, "Cannot read configuration file : {0}", exception.getMessage());
    }
  }

  private final String id;
  private final Socket socket;
  private final Map<Instrument, Integer> instruments;

  Market(String host, int port) throws IOException {
    socket = new Socket(host, port);
    byte[] byteBuffer = new byte[7];
    int read = socket.getInputStream().read(byteBuffer);
    if (!isValidId(byteBuffer, read)) {
      long available = socket.getInputStream().available();
      long skipped = socket.getInputStream().skip(available);
      logger.log(Level.INFO, "Skipping {0} bytes", skipped);
      socket.close();
      throw new IllegalArgumentException("Invalid market ID");
    }
    this.id = new String(byteBuffer, 0, read);
    this.instruments = new ConcurrentHashMap<>(); // market is initially empty
  }

  private Instrument findInstrumentByName(String name) {
    Optional<Instrument> instrument = instruments.entrySet().stream().filter(
        e -> e.getKey().getName().equals(name)).findAny().map(Map.Entry::getKey);
    if (instrument.isEmpty()) {
      return null;
    } else{
      return instrument.get();
    }
  }

  private boolean instrumentExists(Instrument instrument) {
    return instruments.containsKey(instrument);
  }

  private boolean instrumentExists(String name) {
    Instrument instrument = new Instrument(name, 0); // we don't care about the price
    return instrumentExists(instrument);
  }

  private void addInstrument(Instrument instrument, int quantity) {
    int oldQuantity = instruments.getOrDefault(instrument, 0);
    if (instrumentExists(instrument)) {
      Instrument existingInstrument = findInstrumentByName(instrument.getName());
      assert existingInstrument != null;
      int newPrice = existingInstrument.getPrice() * oldQuantity;
      newPrice += quantity * instrument.getPrice();
      newPrice /= quantity + oldQuantity;
      instrument.setPrice(newPrice);
    }
    logger.log(Level.INFO, "Adding {0} item of instrument {1} with price {2}",
        new Object[]{quantity, instrument.getName(), instrument.getPrice()});
    instruments.put(instrument, oldQuantity + quantity);
  }

  public void processMessage(String message) throws Exception {
    message = message.trim();
    logger.log(Level.INFO, "Processing message ''{0}''", message);
    InstrumentMetadata metadata = extractInstrumentMetadata(message);
    if (metadata == null) {
      logger.log(Level.SEVERE, "Received invalid message ''{0}'', dropping it...", message);
      return;
    }
    if (metadata.getType().equalsIgnoreCase("BUY") && !instrumentExists(metadata.getName())) {
      logger.log(Level.INFO, "Instrument ''{0}'' not found in this market", metadata.getName());
      sendResponse("REJECTED", metadata);
      return;
    }
    if (metadata.getType().equalsIgnoreCase("SELL")) {
      addInstrument(new Instrument(metadata.getName(), metadata.getPrice()), metadata.getQuantity());
    } else {
      int quantity = instruments.get(new Instrument(metadata.getName(), metadata.getPrice()));
      if (quantity < metadata.getQuantity()) {
        logger.log(Level.INFO, "No enough items of instrument ''{0}'' in this market", metadata.getName());
        sendResponse("REJECTED", metadata);
        return;
      }
      Instrument instrument = findInstrumentByName(metadata.getName());
      assert instrument != null;
      if (instrument.getPrice() > metadata.getPrice()) {
        logger.log(Level.INFO, "Price too low for instrument ''{0}'' in this market", metadata.getName());
        sendResponse("REJECTED", metadata);
        return;
      }
      instruments.put(instrument, instruments.get(instrument) - metadata.getQuantity());
    }
    sendResponse("EXECUTED", metadata);
  }

  private void sendResponse(String response, InstrumentMetadata metadata) throws Exception {
    StringBuilder stringBuilder = new StringBuilder();

    stringBuilder.append("1=");
    stringBuilder.append(id);
    stringBuilder.append("\1");
    stringBuilder.append("2=");
    stringBuilder.append(metadata.getType());
    stringBuilder.append("\1");
    stringBuilder.append("3=");
    stringBuilder.append(metadata.getName());
    stringBuilder.append("\1");
    stringBuilder.append("4=");
    stringBuilder.append(metadata.getQuantity());
    stringBuilder.append("\1");
    stringBuilder.append("5=");
    stringBuilder.append(metadata.getBrokerId());
    stringBuilder.append("\1");
    stringBuilder.append("6=");
    stringBuilder.append(metadata.getPrice());
    stringBuilder.append("\1");
    stringBuilder.append("7=");
    stringBuilder.append(response);
    stringBuilder.append("\1");

    int checksum = 0;
    for (int i = 0; i < stringBuilder.length(); ++i) {
      checksum += stringBuilder.charAt(i);
      checksum %= 256;
    }
    stringBuilder.append("8=");
    stringBuilder.append(checksum);
    socket.getOutputStream().write(stringBuilder.toString().getBytes());
  }

  private InstrumentMetadata extractInstrumentMetadata(String message) {
    String name = null;
    String type = null;
    int brokerId = 0;
    int quantity = 0;
    int price = 0;

    String[] fields = message.split("\1");
    for (String s : fields) {
      String[] keyValue = s.split("=");
      if (keyValue.length != 2) {
        return null;
      }
      String key = keyValue[0];
      String value = keyValue[1];
      switch (key) {
        case "1":
          brokerId = parseInt(value);
          break;
        case "2":
          type = value;
        case "3":
          name = value;
          break;
        case "4":
          quantity = parseInt(value);
          break;
        case "6":
          price = parseInt(value);
          break;
      }
    }
    if (name == null || type == null) {
      return null;
    }
    return new InstrumentMetadata(brokerId, name, type, quantity, price);
  }

  public void listen() throws Exception{
    while (socket.isConnected()) {
      StringBuilder stringBuilder = new StringBuilder();
      byte[] buffer = new byte[1024];

      int read = socket.getInputStream().read(buffer);
      if (read < 0){
        break;
      }
      stringBuilder.append(new String(buffer, 0, read));
      processMessage(stringBuilder.toString());
    }
    logger.info("Connection to router is closed");
  }

  private boolean isValidId(byte[] bytes, int length) {
    if (length < 0 || length > 6) {
      return false;
    }
    for (int i = 0; i < 6; ++i) {
      if (bytes[i] < '0' || bytes[i] > '9') {
        return false;
      }
    }
    return true;
  }

  @Override
  public void close() throws Exception {
    socket.close();
  }
}
