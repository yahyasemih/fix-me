package ma.leet.fixme.router;

import sun.misc.Signal;

public class Main {
  public static void main(String[] args) {
    int brokersPort = 5000;
    int marketsPort = 5001;
    try (Router router = new Router(brokersPort, marketsPort)) {
      Signal.handle(new Signal("INT"), sig -> router.setRunning(false));
      Signal.handle(new Signal("TERM"), sig -> router.setRunning(false));
      Signal.handle(new Signal("HUP"), sig -> router.setRunning(false));
      router.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
