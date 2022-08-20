package ma.leet.fixme.broker;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class Main {
  public static void main(String[] args) {
    try (Broker broker = new Broker("localhost", 5000)) {
      broker.buy("maticha", 5, "100000", 10);
      Thread.sleep(5000);
//      String instrument;
//      int quantity;
//      int marketId;
//      int price;
//      byte[] byteBuffer = new byte[100];
//      System.out.println(socket.getChannel());
//      int read = socket.getInputStream().read(byteBuffer);
//      System.out.println("Read : " + read);
//      System.out.println(new String(byteBuffer, 0, read));
//      socket.getOutputStream().write(String.join("\1", "hello", "world", "cc").getBytes());
//      socket.getOutputStream().flush();
//      socket.getOutputStream().write("hello".getBytes());
//      socket.getOutputStream().close();
//      socket.close();
    } catch (Exception e) {
      System.out.println("Error while trading : " + e.getMessage());
    }
  }
}
