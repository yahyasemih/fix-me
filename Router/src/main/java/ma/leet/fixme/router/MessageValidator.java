package ma.leet.fixme.router;

import java.util.Set;

import static java.lang.Integer.parseInt;

public class MessageValidator extends MessageProcessor {
  private static final String ID = "1";
  private static final String TYPE = "2";
  private static final String INSTRUMENT = "3";
  private static final String QUANTITY = "4";
  private static final String MARKET = "5";
  private static final String PRICE = "6";
  private static final String CHECKSUM = "7";
  private static final Set<String> validFields = Set.of(ID, TYPE, INSTRUMENT, QUANTITY, MARKET, PRICE, CHECKSUM);

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
        System.out.println("Invalid field : " + s);
        return false;
      }
      String key = keyValue[0];
      String value = keyValue[1];
      if (!validFields.contains(key)) {
        System.out.println("Invalid field : " + key);
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
            System.out.println("Checksum incorrect : " + value);
          }
          return expectedChecksum == checksum && nextProcessor.shouldPass(data);
        } catch (NumberFormatException e) {
          System.out.println("Invalid checksum : " + value);
          return false;
        }
      }
    }
    return false;
  }
}
