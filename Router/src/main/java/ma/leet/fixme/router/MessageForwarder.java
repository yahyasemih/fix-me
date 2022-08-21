package ma.leet.fixme.router;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class MessageForwarder extends MessageProcessor {
  MessageForwarder() {
    super(null); // this is the last element of the chain, there is no next processor
  }

  @Override
  public boolean shouldPass(Object data) {
    if (!(data instanceof Request)) {
      System.out.println("Invalid data");
      return false;
    }
    Request request = (Request) data;
    String message = (String) request.getData();
    SocketChannel channel = request.getChannel();

    if (!channel.isConnected()) {
      System.out.println("Channel " + channel + " not connected");
      return false;
    }
    try {
      System.out.println("Sending '" + message + "' to channel " + channel);
      message += "\n";
      channel.write(ByteBuffer.wrap(message.getBytes()));
    } catch (IOException e) {
      System.out.println("Could not forward message to " + channel + " : " + e.getMessage());
      return false;
    }
    return true;
  }
}
