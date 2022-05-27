package ma.leet.fixme;

import ma.leet.fixme.router.Router;
import sun.misc.Signal;

public class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("usage: router brokers_port markets_port");
            System.exit(1);
        }
        if (!args[0].matches("[0-9]+")) {
            System.out.println("Brokers port is invalid: " + args[0]);
            System.exit(1);
        } else if (!args[1].matches("[0-9]+")) {
            System.out.println("Markets port is invalid: " + args[1]);
            System.exit(1);
        }
        int brokersPort = Integer.parseInt(args[0]);
        int marketsPort = Integer.parseInt(args[1]);
        try (Router router = new Router(brokersPort, marketsPort)) {
            sun.misc.Signal.handle(new Signal("INT"), sig -> router.setRunning(false));
            sun.misc.Signal.handle(new Signal("TERM"), sig -> router.setRunning(false));
            sun.misc.Signal.handle(new Signal("HUP"), sig -> router.setRunning(false));
            router.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
