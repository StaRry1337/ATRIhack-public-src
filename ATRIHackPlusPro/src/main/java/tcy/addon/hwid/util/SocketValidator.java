package hwid.util;

import java.io.IOException;
import java.net.Socket;

public class SocketValidator {
    private static Socket socket;
    private static boolean ConnectState = false;

    private void Validator() {
        try {
            socket = new Socket("101.200.120.18", 9999);
            ConnectState = true;
        } catch (IOException e) {
            ConnectState = false;
            throw new RuntimeException(e);

        }

    }
}