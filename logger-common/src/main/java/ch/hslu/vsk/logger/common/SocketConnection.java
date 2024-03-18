package ch.hslu.vsk.logger.common;

public class SocketConnection {
    private SocketConnection() {
        throw new AssertionError();
    }

    public static final int SOCKET_PORT = 9999;
    public static final String SOCKET_ADDRESS = "localhost";
}
