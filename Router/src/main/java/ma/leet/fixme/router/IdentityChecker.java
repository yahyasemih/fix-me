package ma.leet.fixme.router;

import java.nio.channels.SocketChannel;
import java.util.Map;

import static java.lang.Integer.parseInt;

public class IdentityChecker extends MessageProcessor {
  private final Map<Integer, SocketChannel> table;
  IdentityChecker(MessageProcessor nextProcessor, Map<Integer, SocketChannel> table) {
    super(nextProcessor);
    this.table = table;
  }

  @Override
  public boolean shouldPass(Object data) {
    if (!(data instanceof String)) {
      System.out.println("Invalid data");
      return false;
    }
    String message = (String) data;
    String[] fields = message.split("\1");
    if (fields.length == 0) {
      System.out.println("Invalid fields");
      return false;
    }
    int i = 0;
    for (; i < fields.length; ++i) {
      if (fields[i].startsWith("5=")) {
        break;
      }
    }
    if (i >= fields.length) {
      System.out.println("Could not get destination");
      return false;
    }
    String destinationIdField = fields[i];
    String[] keyValue = destinationIdField.split("=");
    if (keyValue.length != 2) {
      System.out.println("Invalid field");
      return false;
    }
    try {
      int id = parseInt(keyValue[1]);
      if (table.containsKey(id)) {
        Request request = new Request(data, table.get(id));
        System.out.println("going to message forwarder...");
        return nextProcessor.shouldPass(request);
      }
    } catch (NumberFormatException e) {
      System.out.println("Invalid ID : " + keyValue[1]);
      return false;
    }
    return false;
  }
}
