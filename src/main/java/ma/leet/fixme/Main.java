package ma.leet.fixme;

import ma.leet.fixme.router.Router;
import sun.misc.Signal;

public class Main {
    public static void main(String[] args) {
        int brokersPort = 5000;
        int marketsPort = 5001;
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
