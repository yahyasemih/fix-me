package ma.leet.fixme.router;

import java.nio.channels.SocketChannel;

public class Request {
  private final Object data;
  private final SocketChannel channel;

  public Object getData() {
    return data;
  }

  public SocketChannel getChannel() {
    return channel;
  }

  public Request(Object data, SocketChannel channel) {
    this.data = data;
    this.channel = channel;
  }
}
