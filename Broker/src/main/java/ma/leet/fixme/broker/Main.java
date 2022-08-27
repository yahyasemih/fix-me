package ma.leet.fixme.broker;

import java.util.Scanner;

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
      Scanner scanner = new Scanner(System.in);
      String command;
      String name;
      String marketId;
      int quantity;
      int price;
      while (true) {
        System.out.print("Enter command (BUY, SELL, EXIT) : ");
        if (!scanner.hasNext()) {
          break;
        }
        command = scanner.nextLine().trim();
        if (command.equalsIgnoreCase("BUY") || command.equalsIgnoreCase("SELL")) {
          System.out.print("Instrument name : ");
          if (!scanner.hasNext()) {
            throw new IllegalArgumentException("Incomplete command");
          }
          name = scanner.nextLine();
          System.out.print("Quantity : ");
          if (!scanner.hasNextInt()) {
            throw new IllegalArgumentException("Incomplete command");
          }
          quantity = scanner.nextInt();
          scanner.skip("\n");
          System.out.print("Price : ");
          if (!scanner.hasNextInt()) {
            throw new IllegalArgumentException("Incomplete command");
          }
          price = scanner.nextInt();
          scanner.skip("\n");
          System.out.print("Market ID : ");
          if (!scanner.hasNext()) {
            throw new IllegalArgumentException("Incomplete command");
          }
          marketId = scanner.nextLine();
          if (command.equalsIgnoreCase("BUY")) {
            broker.buy(name, quantity, marketId, price);
          } else {
            broker.sell(name, quantity, marketId, price);
          }
        } else if (command.equalsIgnoreCase("EXIT")) {
          break;
        } else {
          System.out.println("Invalid command : " + command);
        }
      }
    } catch (Exception e) {
      System.out.println("Error while trading : " + e);
    }
  }
}
