

package tcy.addon.hwid;


import meteordevelopment.meteorclient.addons.MeteorAddon;
import tcy.addon.atrihack.ATRIHack;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

import static com.mojang.text2speech.Narrator.LOGGER;

public class Hwid{

//    public static boolean validateHwid() {
//        String hwid;
//        try {
//            hwid = ATRIHack.getHWID();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        if (ClientSocket.connect()){
//            new Thread(new ClientListener(new Socket())).start();
//            new Thread(new ClientSenter(new Socket())).start();
//        }
//
//        return false;
//    }

static class ClientListener implements Runnable{
    private Socket socket;
    ClientListener(Socket socket){
        this.socket = socket;
    }


    @Override
    public void run() {
        try {
            ObjectInputStream input_Stream = new ObjectInputStream(socket.getInputStream());
            while (true){
                LOGGER.info("Server:"+input_Stream.readObject());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
static class ClientSenter implements Runnable{
    private Socket socket;
    public String getHWID() throws IOException {
        String hwid = null;
        try {
            Process process = Runtime.getRuntime().exec("wmic csproduct get uuid");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    hwid = line.trim();
                }
            }
            reader.close();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new IOException("Failed to retrieve HWID.");
        }
        return hwid;

    }
    public ClientSenter(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
//        try {
//            ObjectOutputStream output_stream = new ObjectOutputStream(socket.getOutputStream());
//            Socket socket = new Socket("127.0.0.1", 5000);
//            OutputStream outputStream = socket.getOutputStream();
//            PrintWriter printWriter = new PrintWriter(outputStream);
//            printWriter.write("Client:"+getHWID());
//            printWriter.flush();
//
//            printWriter.close();
//            outputStream.close();
//            socket.close();
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//            }
        }
    }
}
class ClientSocket{
    private static Socket socket;


    static boolean connect(){
        try {
            socket = new Socket("43.248.79.78",1234);
            ObjectOutputStream output_stream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream input_stream = new ObjectInputStream(socket.getInputStream());
            new Thread(new Hwid.ClientListener(socket)).start();
            new Thread(new Hwid.ClientSenter(socket)).start();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}



