package Client;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
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
        ByteBuffer fileNameLength = ByteBuffer.allocate(Short.BYTES);
        fileNameLength.putShort((short)file.getName().getBytes(StandardCharsets.UTF_8).length);
        socketOutputStream.write(fileNameLength.array());

        socketOutputStream.write(file.getName().getBytes(StandardCharsets.UTF_8));

        ByteBuffer fileLength = ByteBuffer.allocate(Long.BYTES);
        fileLength.putLong(file.length());
        socketOutputStream.write(fileLength.array());
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