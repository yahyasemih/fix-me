package ma.leet.fixme.router;

public abstract class MessageProcessor {
  public final MessageProcessor nextProcessor;

  MessageProcessor(MessageProcessor nextProcessor) {
    this.nextProcessor = nextProcessor;
  }
  public abstract boolean shouldPass(Object data);
}
