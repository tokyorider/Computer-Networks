package Client;

import Utility.Converter;
import Utility.GuaranteedReader;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

class FileSender {

    private static final int BUF_SIZE = 1 << 20;

    static void send(File file, InetAddress serverAddress, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(serverAddress, port));
            OutputStream socketOutputStream = socket.getOutputStream();
            sendHeader(file, socketOutputStream);

            BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(file));
            sendFile(fileInputStream, socketOutputStream);
            int flag = socket.getInputStream().read();
            if (flag == 0) {
                System.out.println("File sending is successful");
            } else {
                System.out.println("File sending isn't successful");
            }
        } catch(IOException e) {
            e.printStackTrace();
            System.out.println("File sending isn't successful");
        }
    }

    private static void sendHeader(File file, OutputStream socketOutputStream) throws IOException {
        socketOutputStream.write(Converter.convertToByteArr((short)file.getName().length(), Short.SIZE / 8));
        socketOutputStream.write(file.getName().getBytes(StandardCharsets.UTF_8));
        socketOutputStream.write(Converter.convertToByteArr(file.length(), Long.SIZE / 8));
    }

    private static void sendFile(InputStream fileInputStream, OutputStream socketOutputStream) throws IOException {
        byte[] buf = new byte[BUF_SIZE];
        int count = fileInputStream.read(buf);
        while (count != -1) {
            socketOutputStream.write(buf, 0, count);
            count = fileInputStream.read(buf);
        }
    }

}