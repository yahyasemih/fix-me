package ma.leet.fixme.broker;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Broker implements AutoCloseable {
  private static final LogManager logManager = LogManager.getLogManager();
  private final static Logger logger = Logger.getLogger(Broker.class.getSimpleName());

  static {
    try {
      logManager.readConfiguration(Broker.class.getClassLoader().getResourceAsStream("logger.properties"));
    } catch (IOException exception) {
      logger.log(Level.SEVERE, "Cannot read configuration file : {0}", exception.getMessage());
    }
  }

  private final String id;
  private final Socket socket;

  Broker(String host, int port) throws IOException {
    socket = new Socket(host, port);
    byte[] byteBuffer = new byte[7];
    int read = socket.getInputStream().read(byteBuffer);
    if (!isValidId(byteBuffer, read)) {
      long available = socket.getInputStream().available();
      long skipped = socket.getInputStream().skip(available);
      logger.log(Level.INFO, "Skipping {0} bytes", skipped);
      socket.close();
      throw new IllegalArgumentException("Invalid broker ID");
    }
    this.id = new String(byteBuffer, 0, read);
  }

  @Override
  public void close() throws Exception {
    socket.close();
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

  public void buy(String instrument, int quantity, String market, int price) throws Exception {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("1=");
    stringBuilder.append(id);
    stringBuilder.append("\1");
    stringBuilder.append("2=");
    stringBuilder.append("BUY");
    stringBuilder.append("\1");
    stringBuilder.append("3=");
    stringBuilder.append(instrument);
    stringBuilder.append("\1");
    stringBuilder.append("4=");
    stringBuilder.append(quantity);
    stringBuilder.append("\1");
    stringBuilder.append("5=");
    stringBuilder.append(market);
    stringBuilder.append("\1");
    stringBuilder.append("6=");
    stringBuilder.append(price);
    stringBuilder.append("\1");
    int checksum = 0;
    for (int i = 0; i < stringBuilder.length(); ++i) {
      checksum += stringBuilder.charAt(i);
      checksum %= 256;
    }
    stringBuilder.append("8=");
    stringBuilder.append(checksum);
    sendToMarket(stringBuilder.toString());
  }

  private void sendToMarket(String message) throws Exception {
    socket.getOutputStream().write(message.getBytes());
    logger.log(Level.INFO, "Waiting for market response...");
    byte[] buffer = new byte[1024];
    int read = socket.getInputStream().read(buffer);
    if (read < 0) {
      logger.log(Level.SEVERE, "Could not read market's response");
      return;
    }
    logger.log(Level.INFO, "received ''{0}''", new String(buffer, 0, read - 1));
  }

  public void sell(String instrument, int quantity, String market, int price) throws Exception {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("1=");
    stringBuilder.append(id);
    stringBuilder.append("\1");
    stringBuilder.append("2=");
    stringBuilder.append("SELL");
    stringBuilder.append("\1");
    stringBuilder.append("3=");
    stringBuilder.append(instrument);
    stringBuilder.append("\1");
    stringBuilder.append("4=");
    stringBuilder.append(quantity);
    stringBuilder.append("\1");
    stringBuilder.append("5=");
    stringBuilder.append(market);
    stringBuilder.append("\1");
    stringBuilder.append("6=");
    stringBuilder.append(price);
    stringBuilder.append("\1");
    int checksum = 0;
    for (int i = 0; i < stringBuilder.length(); ++i) {
      checksum += stringBuilder.charAt(i);
      checksum %= 256;
    }
    stringBuilder.append("8=");
    stringBuilder.append(checksum);
    sendToMarket(stringBuilder.toString());
  }
}
