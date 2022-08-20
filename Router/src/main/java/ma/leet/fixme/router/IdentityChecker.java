package ma.leet.fixme.router;

public class IdentityChecker extends MessageProcessor {
  IdentityChecker(MessageProcessor nextProcessor) {
    super(nextProcessor);
  }

  @Override
  public boolean shouldPass(String message) {
    return false;
  }
}
