package ma.leet.fixme.router;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static java.lang.Integer.parseInt;

public class IdentityChecker extends MessageProcessor {
  private static final LogManager logManager = LogManager.getLogManager();
  private final static Logger logger = Logger.getLogger(IdentityChecker.class.getSimpleName());

  static {
    try {
      logManager.readConfiguration(new FileInputStream("logger.properties"));
    } catch (IOException exception) {
      logger.log(Level.SEVERE, "Cannot read configuration file ", exception);
    }
  }

  private final Map<Integer, SocketChannel> table;
  IdentityChecker(MessageProcessor nextProcessor, Map<Integer, SocketChannel> table) {
    super(nextProcessor);
    this.table = table;
  }

  @Override
  public boolean shouldPass(Object data) {
    if (!(data instanceof String)) {
      logger.severe("Invalid data");
      return false;
    }
    String message = (String) data;
    String[] fields = message.split("\1");
    if (fields.length == 0) {
      logger.severe("Invalid fields");
      return false;
    }
    int i = 0;
    for (; i < fields.length; ++i) {
      if (fields[i].startsWith("5=")) {
        break;
      }
    }
    if (i >= fields.length) {
      logger.severe("Could not get destination");
      return false;
    }
    String destinationIdField = fields[i];
    String[] keyValue = destinationIdField.split("=");
    if (keyValue.length != 2) {
      logger.log(Level.SEVERE, "Invalid field {0}", destinationIdField);
      return false;
    }
    try {
      int id = parseInt(keyValue[1]);
      if (table.containsKey(id)) {
        Request request = new Request(data, table.get(id));
        logger.info("Forwarding message");
        return nextProcessor.shouldPass(request);
      } else {
        logger.log(Level.SEVERE, "ID ''{0}'' not found in the rooting table", id);
        return false;
      }
    } catch (NumberFormatException e) {
      logger.log(Level.SEVERE, "Invalid ID : {0}", keyValue[1]);
      return false;
    }
  }
}
