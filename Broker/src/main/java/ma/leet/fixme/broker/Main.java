package ma.leet.fixme.broker;

import static java.lang.Integer.parseInt;

public class Main {
  public static void main(String[] args) {
    if (args.length < 1) {
      System.out.println("Usage: broker router_host [router_port]");
      return;
    }
    String routerHost = args[0];
    int routerPort = 5000;
    if (args.length >= 2) {
      try {
        routerPort = parseInt(args[1]);
      } catch (NumberFormatException e) {
        System.out.println("Invalid port : " + args[1]);
        return;
      }
    }
    try (Broker broker = new Broker(routerHost, routerPort)) {
      broker.buy("maticha", 5, "100000", 10);
      Thread.sleep(5000);
    } catch (Exception e) {
      System.out.println("Error while trading : " + e);
    }
  }
}
