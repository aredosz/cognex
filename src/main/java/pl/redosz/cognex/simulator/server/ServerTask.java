package pl.redosz.cognex.simulator.server;

import javafx.scene.control.TextField;
import pl.redosz.cognex.simulator.common.TextAreaLogger;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ServerTask implements Runnable {

    private TextAreaLogger logger;

    private InetAddress host;
    private TextField port;
    private TextField prefix;
    private TextField postfix;
    private TextField value;
    private TextField triggerPhrase;

    private boolean stopServer;
    private boolean isTriggered;
    private Integer serverPort;

    private Map<String, SocketChannel> clients = new HashMap<>();

    public ServerTask(TextField port, TextField prefix, TextField postfix, TextField value, TextField triggerPhrase, TextAreaLogger logger) {
        try {
            this.host = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            this.host = null;
        }
        this.port = port;
        this.prefix = prefix;
        this.postfix = postfix;
        this.value = value;
        this.triggerPhrase = triggerPhrase;
        this.logger = logger;
    }

    @Override
    public void run() {
        Selector selector = null;
        ServerSocketChannel serverSocketChannel = null;
        serverPort = Integer.valueOf(port.getText());
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(host, serverPort));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            logger.log("TCP server started on port " + serverPort);
        } catch (IOException e) {
            logger.log("Can't create TCP server on port " + serverPort);
        }

        if (selector != null && serverSocketChannel != null) {
            while(isActive()) {
                try {
                    if (selector.select(1_000) <= 0) {
                        continue;
                    }
                } catch (IOException e) {
                    logger.log("Can't check selector queue");
                    break;
                }

                process(selector.selectedKeys());

                if (isTriggered) {
                    isTriggered = false;
                    sendResponseToClients();
                }
            }

            try {
                serverSocketChannel.close();
                selector.close();
                logger.log("TCP server stopped on port " + serverPort);
            } catch (IOException e) {
                logger.log("Can't stop TCP server on port " + serverPort);
            }
        }
    }

    public void stopServer() {
        stopServer = true;
    }

    public void trigger() {
        isTriggered = true;
    }

    private boolean isActive() {
        return !Thread.currentThread().isInterrupted() && !stopServer;
    }

    private void process(Set readySet) {
        Iterator iterator = readySet.iterator();
        while (iterator.hasNext()) {
            SelectionKey key = (SelectionKey) iterator.next();
            iterator.remove();

            if (!isActive()) {
                key.cancel();
                continue;
            }

            if (key.isAcceptable()) {
                ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                try {
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(key.selector(), SelectionKey.OP_READ | SelectionKey.OP_WRITE);

                    clients.put(
                            String.format("%s:%d", socketChannel.socket().getLocalAddress().getHostAddress(), socketChannel.socket().getPort()),
                            socketChannel
                    );
                } catch (IOException e) {
                    logger.log("Can't accept waiting connection");
                }
            }

            if (key.isReadable()) {
                String request = processRead(key);
                if (request.length() > 0 && request.equals(triggerPhrase.getText() + "\n")) {
                    sendResponseToClients();
                }
            }
        }
    }

    private String processRead(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(128);
        try {
            int read = socketChannel.read(buffer);
            if (read > 0) {
                buffer.flip();
                return new String(buffer.array(), 0, read);
            }
        } catch (IOException e) {
            return "";
        }
        return "";
    }

    private void sendResponseToClients() {
        String response = String.format("%s%s%s", prefix.getText(), value.getText(), postfix.getText());
        logger.log(String.format("Send reponse on port %s \"%s\"", serverPort, response));
        response = response.replaceAll("\\\\n", "\n").replaceAll("\\\\r", "\r");

        Iterator<Map.Entry<String, SocketChannel>> iterator = clients.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, SocketChannel> value = iterator.next();

            try {
                value.getValue().write(ByteBuffer.wrap(response.getBytes()));
            } catch (IOException e) {
                iterator.remove();
            }
        }
    }
}
