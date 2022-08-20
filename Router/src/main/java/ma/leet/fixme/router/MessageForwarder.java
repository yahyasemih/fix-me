package ma.leet.fixme.router;

public class MessageForwarder extends MessageProcessor {
  MessageForwarder() {
    super(null);
  }

  @Override
  public boolean shouldPass(String message) {
    return false;
  }
}
