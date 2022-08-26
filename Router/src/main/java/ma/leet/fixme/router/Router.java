package ma.leet.fixme.router;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Router implements Closeable {
  private static final AtomicInteger ID = new AtomicInteger(99999);
  private static final LogManager logManager = LogManager.getLogManager();
  private final static Logger logger = Logger.getLogger(Router.class.getSimpleName());

  static {
    try {
      logManager.readConfiguration(Router.class.getClassLoader().getResourceAsStream("logger.properties"));
    } catch (IOException exception) {
      logger.log(Level.SEVERE, "Cannot read configuration file : {0}", exception.getMessage());
    }
  }

  private final ServerSocketChannel brokersChannel;
  private final ServerSocketChannel marketsChannel;
  private final Selector selector;
  private final AtomicBoolean isRunning;
  private final List<SocketChannel> brokers;
  private final List<SocketChannel> markets;
  private final Map<Integer, SocketChannel> idToChannelMap;
  private final Map<SocketChannel, Integer> channelToIdMap;
  private final int brokersPort;
  private final int marketsPort;
  private final ExecutorService executorService;

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
    idToChannelMap = new HashMap<>();
    channelToIdMap = new HashMap<>();
    executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
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
        } else if (key.isReadable()) {
          receiveMessage(key);
        }
      }
    }
  }

  private void receiveMessage(SelectionKey key) {
    logger.info("Reading connection");
    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
    SocketChannel socketChannel = (SocketChannel) key.channel();
    try {
      int read = socketChannel.read(byteBuffer);
      logger.info("Read : " + read);
      if (read == -1) {
        int port = ((InetSocketAddress) (socketChannel.getLocalAddress())).getPort();
        if (port == brokersPort) {
          brokers.remove(socketChannel);
        } else {
          markets.remove(socketChannel);
        }
        int id = channelToIdMap.get(socketChannel);
        channelToIdMap.remove(socketChannel);
        idToChannelMap.remove(id).close();
        key.cancel();
      } else if (read > 0) {
        MessageHandler messageHandler = new MessageHandler(
            new String(byteBuffer.array(), 0, read), socketChannel);
        executorService.submit(messageHandler);
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error while receiving response : {0}", e.getMessage());
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
    s.configureBlocking(false);
    s.write(ByteBuffer.wrap(("" + ID.incrementAndGet()).getBytes()));
    idToChannelMap.put(ID.get(), s);
    channelToIdMap.put(s, ID.get());
    s.register(selector, SelectionKey.OP_READ);
  }

  public void setRunning(boolean running) {
    isRunning.set(running);
  }

  @Override
  public void close() throws IOException {
    logger.info("Cleaning and closing server...");
    for (SocketChannel socketChannel : markets) {
      socketChannel.close();
    }
    for (SocketChannel socketChannel : brokers) {
      socketChannel.close();
    }
    markets.clear();
    brokers.clear();
    idToChannelMap.clear();
    channelToIdMap.clear();
    brokersChannel.close();
    marketsChannel.close();
    executorService.shutdownNow();
  }

  class MessageHandler implements Runnable {
    private final String message;
    private final SocketChannel socketChannel;

    public MessageHandler(String message, SocketChannel socketChannel) {
      this.message = message;
      this.socketChannel = socketChannel;
    }
    @Override
    public void run() {
      logger.log(Level.INFO, "{0} got message: ''{1}'' from channel {2}",
          new Object[] {Thread.currentThread().getName(), message, socketChannel});
      MessageProcessor messageProcessor = new MessageValidator(new IdentityChecker(new MessageForwarder(), idToChannelMap));
      if (!messageProcessor.shouldPass(message)) {
        logger.log(Level.INFO, "Message ''{0}'' could not be proceeded", message);
      }
    }
  }
}
