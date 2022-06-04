package ma.leet.fixme.router;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Router implements Closeable {
  private static final AtomicInteger ID = new AtomicInteger(99999);
  private static final LogManager logManager = LogManager.getLogManager();
  private final static Logger logger = Logger.getLogger("router");

  static {
    try {
      logManager.readConfiguration(new FileInputStream("../logger.properties"));
    } catch (IOException exception) {
      logger.log(Level.SEVERE, "Cannot read configuration file", exception);
    }
  }

  private final ServerSocketChannel brokersChannel;
  private final ServerSocketChannel marketsChannel;
  private final Selector selector;
  private final AtomicBoolean isRunning;
  private final List<SocketChannel> brokers;
  private final List<SocketChannel> markets;
  private final int brokersPort;
  private final int marketsPort;

  public Router(int brokersPort, int marketsPort) throws Exception {
    if (brokersPort == marketsPort) {
      throw new InvalidParameterException("Markets and Brokers ports should be different");
    }
    this.brokersPort = brokersPort;
    this.marketsPort = marketsPort;
    selector = Selector.open();
    brokersChannel = ServerSocketChannel.open();
    brokersChannel.configureBlocking(false);
    brokersChannel.bind(new InetSocketAddress(brokersPort));
    brokersChannel.register(selector, SelectionKey.OP_ACCEPT);
    marketsChannel = ServerSocketChannel.open();
    marketsChannel.configureBlocking(false);
    marketsChannel.bind(new InetSocketAddress(marketsPort));
    marketsChannel.register(selector, SelectionKey.OP_ACCEPT);
    isRunning = new AtomicBoolean(true);
    brokers = new ArrayList<>();
    markets = new ArrayList<>();
  }

  public void start() throws Exception {
    while (isRunning.get()) {
      int selected = selector.select(100);
      if (selected == 0) {
        continue;
      }
      Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
      while (keys.hasNext()) {
        SelectionKey key = keys.next();
        keys.remove();
        if (!key.isValid()) {
          logger.info("closed connection");
        } else if (key.isAcceptable()) {
          acceptConnection(key);
        } else if (key.isWritable()) {
          logger.info("Writing...");
        } else if (key.isReadable()) {
          receiveMessage(key);
        }
      }
    }
  }

  private void receiveMessage(SelectionKey key) throws Exception {
    logger.info("Reading connection");
    ByteBuffer byteBuffer = ByteBuffer.allocate(5);
    SocketChannel s = (SocketChannel) key.channel();
    int read = s.read(byteBuffer);
    logger.info("Read : " + read);
    if (read == -1) {
      int port = ((InetSocketAddress) (s.getLocalAddress())).getPort();
      if (port == brokersPort) {
        brokers.remove(s);
      } else {
        markets.remove(s);
      }
      s.close();
      key.cancel();
    } else {
      logger.info(new String(byteBuffer.array()));
    }
  }

  private void acceptConnection(SelectionKey key) throws Exception {
    SocketChannel s = ((ServerSocketChannel) key.channel()).accept();
    logger.log(Level.INFO, "Accepting new connection from {0}",
        ((InetSocketAddress) (s.getRemoteAddress())).getHostName());
    if (((InetSocketAddress) (s.getLocalAddress())).getPort() == marketsPort) {
      markets.add(s);
    } else {
      brokers.add(s);
    }
    logger.info("markets : " + markets.size());
    logger.info("brokers : " + brokers.size());
    //System.out.println(((InetSocketAddress)((ServerSocketChannel) key.channel()).getLocalAddress()).getPort());
    s.configureBlocking(false);
    s.write(ByteBuffer.wrap(("Your id is " + ID.incrementAndGet() + "\n").getBytes()));
    s.register(selector, SelectionKey.OP_READ);
  }

  public void setRunning(boolean running) {
    isRunning.set(running);
  }

  @Override
  public void close() throws IOException {
    logger.info("Closing and cleaning server...");
    brokersChannel.close();
    marketsChannel.close();
  }
}
