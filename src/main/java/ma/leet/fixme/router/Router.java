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
import java.util.Iterator;

public class Router implements Closeable {
    private final ServerSocketChannel brokersChannel;
    private final ServerSocketChannel marketsChannel;
    private final Selector selector;
    private static int ID = 99999;
    private boolean isRunning;

    public Router(int brokersPort, int marketsPort) throws Exception {
        if (brokersPort == marketsPort) {
            throw new InvalidParameterException("Markets and Brokers ports should be different");
        }
        selector = Selector.open();
        brokersChannel = ServerSocketChannel.open();
        brokersChannel.configureBlocking(false);
        brokersChannel.bind(new InetSocketAddress(brokersPort));
        brokersChannel.register(selector, SelectionKey.OP_ACCEPT);
        marketsChannel = ServerSocketChannel.open();
        marketsChannel.configureBlocking(false);
        marketsChannel.bind(new InetSocketAddress(marketsPort));
        marketsChannel.register(selector, SelectionKey.OP_ACCEPT);
        setRunning(true);
    }

    public void start() throws Exception {
        while (isRunning) {
            int selected = selector.select(100);
            if (selected == 0) {
                continue;
            }
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();
                if (!key.isValid()) {
                    System.out.println("closed connection");
                } else if (key.isAcceptable()) {
                    System.out.println("Accepting connection");
                    SocketChannel s = ((ServerSocketChannel) key.channel()).accept();
                    //System.out.println(((InetSocketAddress)((ServerSocketChannel) key.channel()).getLocalAddress()).getPort());
                    s.configureBlocking(false);
                    s.register(selector, SelectionKey.OP_READ);
                    ++ID;
                    s.write(ByteBuffer.wrap(("Your id is " + ID + "\n").getBytes()));
                } else if (key.isWritable()) {
                    System.out.println("Writing...");
                } else if (key.isReadable()) {
                    System.out.println("Reading connection");
                    ByteBuffer byteBuffer = ByteBuffer.allocate(5);
                    SocketChannel s = (SocketChannel) key.channel();
                    int read = s.read(byteBuffer);
                    System.out.println("Read : " + read);
                    if (read == -1) {
                        s.close();
                        key.cancel();
                    } else {
                        System.out.println(new String(byteBuffer.array()));
                    }
                }
            }
        }
        System.out.println("\rClosing server...");
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    @Override
    public void close() throws IOException {
        System.out.println("\rClosing and cleaning server...");
        brokersChannel.close();
        marketsChannel.close();
    }
}
