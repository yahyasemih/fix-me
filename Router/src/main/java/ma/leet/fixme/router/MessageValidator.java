package ma.leet.fixme.router;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static java.lang.Integer.parseInt;

public class MessageValidator extends MessageProcessor {
  private static final String ID = "1";
  private static final String TYPE = "2";
  private static final String INSTRUMENT = "3";
  private static final String QUANTITY = "4";
  private static final String DESTINATION = "5";
  private static final String PRICE = "6";
  private static final String RESULT = "7";
  private static final String CHECKSUM = "8";
  private static final Set<String> validFields = Set.of(
      ID, TYPE, INSTRUMENT, QUANTITY, DESTINATION, PRICE, RESULT, CHECKSUM);

  private static final LogManager logManager = LogManager.getLogManager();
  private final static Logger logger = Logger.getLogger(MessageValidator.class.getSimpleName());

  static {
    try {
      logManager.readConfiguration(
          MessageValidator.class.getClassLoader().getResourceAsStream("logger.properties"));
    } catch (IOException exception) {
      logger.log(Level.SEVERE, "Cannot read configuration file : {0}", exception.getMessage());
    }
  }

  MessageValidator(MessageProcessor nextProcessor) {
    super(nextProcessor);
  }

  @Override
  public boolean shouldPass(Object data) {
    if (!(data instanceof String)) {
      return false;
    }
    String message = (String) data;
    String[] fields = message.split("\1");
    int checksum = 0;
    for (String s : fields) {
      String[] keyValue = s.split("=");
      if (keyValue.length != 2) {
        logger.log(Level.SEVERE, "Invalid field : {0}", s);
        return false;
      }
      String key = keyValue[0];
      String value = keyValue[1];
      if (!validFields.contains(key)) {
        logger.log(Level.SEVERE, "Invalid field : {0}", key);
        return false;
      }
      if (!key.equals(CHECKSUM)) {
        checksum++; // for SOH character
        for (int i = 0; i < s.length(); ++i) {
          checksum += s.charAt(i);
          checksum %= 256;
        }
      } else {
        try {
          int expectedChecksum = parseInt(value);
          if (expectedChecksum != checksum) {
            logger.log(Level.SEVERE, "Checksum incorrect : {0}", value);
          }
          return expectedChecksum == checksum && nextProcessor.shouldPass(data);
        } catch (NumberFormatException e) {
          logger.log(Level.SEVERE, "Invalid checksum : {0}", value);
          return false;
        }
      }
    }
    return false;
  }
}
