package ma.leet.fixme.market;

import static java.lang.Integer.parseInt;

public class Main {
  public static void main(String[] args) {
    if (args.length < 1) {
      System.out.println("Usage: market router_host [router_port]");
      return;
    }
    String routerHost = args[0];
    int routerPort = 5001;
    if (args.length >= 2) {
      try {
        routerPort = parseInt(args[1]);
      } catch (NumberFormatException e) {
        System.out.println("Invalid port : " + args[1]);
        return;
      }
    }
    try (Market market = new Market(routerHost, routerPort)) {
      market.listen();
    } catch (Exception e) {
      System.out.println("Error while trading : " + e);
    }
  }
}
