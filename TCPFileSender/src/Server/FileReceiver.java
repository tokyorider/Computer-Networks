package Server;

import Utility.Converter;
import Utility.GuaranteedReader;
import Utility.Pair;

import java.io.*;
import java.net.Socket;

class FileReceiver implements Runnable {

    private Socket socket;

    private static final int TIMEOUT = 5000;

    private static final int BUF_SIZE = 1 << 20;

    public FileReceiver(Socket socket) {
        this.socket = socket;
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            socket.setSoTimeout(TIMEOUT);

            InputStream socketInputStream = socket.getInputStream();
            Pair<String, Long> header = receiveHeader(socketInputStream);

            OutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(
                    new File("Uploads" + File.pathSeparator + header.getFirst()))), socketOutputStream = socket.getOutputStream();
            receiveFile(socketInputStream, fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private Pair<String, Long> receiveHeader(InputStream socketInputStream) throws IOException {
        int fileNameLength = (int)Converter.convertToNumber(GuaranteedReader.guaranteedRead(socketInputStream, Short.SIZE / 8), Short.SIZE / 8);
        String fileName = new String(GuaranteedReader.guaranteedRead(socketInputStream, fileNameLength));
        long fileLength = Converter.convertToNumber(GuaranteedReader.guaranteedRead(socketInputStream, Long.SIZE / 8), Long.SIZE / 8);

        return new Pair<>(fileName, fileLength);
    }

    private void receiveFile(InputStream socketInputStream, OutputStream fileOutputStream) throws IOException {
        byte[] buf = GuaranteedReader.guaranteedRead(socketInputStream, BUF_SIZE);
        while (buf.length != 0) {
            fileOutputStream.write(buf);
            buf = GuaranteedReader.guaranteedRead(socketInputStream, BUF_SIZE);
        }
    }
}
