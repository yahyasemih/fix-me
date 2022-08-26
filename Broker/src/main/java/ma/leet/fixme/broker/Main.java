package ma.leet.fixme.broker;

public class Main {
  private static final String BROKER_HOST = "localhost";
  private static final int BROKER_PORT = 5000;
  public static void main(String[] args) {
    try (Broker broker = new Broker(BROKER_HOST, BROKER_PORT)) {
      broker.buy("maticha", 5, "100000", 10);
      Thread.sleep(5000);
    } catch (Exception e) {
      System.out.println("Error while trading : " + e.getMessage());
    }
  }
}
