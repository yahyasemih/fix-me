package ma.leet.fixme.router;

public class MessageValidator extends MessageProcessor {
  MessageValidator(MessageProcessor nextProcessor) {
    super(nextProcessor);
  }

  @Override
  public boolean shouldPass(String message) {
    String[] fields = message.split("\1");
    System.out.println("number of fields: " + fields.length);
    for (String s : fields) {
      System.out.println(s);
    }
    return false;
  }
}
