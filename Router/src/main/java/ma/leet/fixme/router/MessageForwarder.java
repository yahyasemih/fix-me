package ma.leet.fixme.router;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class MessageForwarder extends MessageProcessor {
  private static final LogManager logManager = LogManager.getLogManager();
  private final static Logger logger = Logger.getLogger(MessageForwarder.class.getSimpleName());

  static {
    try {
      logManager.readConfiguration(
          MessageForwarder.class.getClassLoader().getResourceAsStream("logger.properties"));
    } catch (IOException exception) {
      logger.log(Level.SEVERE, "Cannot read configuration file : {0}", exception.getMessage());
    }
  }

  MessageForwarder() {
    super(null); // this is the last element of the chain, there is no next processor
  }

  @Override
  public boolean shouldPass(Object data) {
    if (!(data instanceof Request)) {
      logger.severe("Invalid data");
      return false;
    }
    Request request = (Request) data;
    String message = (String) request.getData();
    SocketChannel channel = request.getChannel();

    if (!channel.isConnected()) {
      logger.log(Level.SEVERE, "Channel {0} not connected", channel);
      return false;
    }
    try {
      logger.log(Level.INFO, "Sending ''{0}'' to channel {1}", new Object[]{message, channel});
      if (!message.endsWith("\n")) {
        message += "\n";
      }
      channel.write(ByteBuffer.wrap(message.getBytes()));
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Could not forward message to {0} : {1}", new Object[] {channel, e.getMessage()});
      return false;
    }
    return true;
  }
}
