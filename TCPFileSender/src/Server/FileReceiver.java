package Server;

import Utility.Converter;
import Utility.GuaranteedReader;
import Utility.Pair;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;

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
            if (receiveFile(socketInputStream, fileOutputStream) == header.getSecond()) {
                socket.getOutputStream().write(0);
            } else {
                socket.getOutputStream().write(1);
            }
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

    private long receiveFile(InputStream socketInputStream, OutputStream fileOutputStream) throws IOException {
        byte[] buf = new byte[BUF_SIZE];
        long generalCount = 0;
        try {
            while (true) {
                int count = socketInputStream.read(buf);
                generalCount += count;
                fileOutputStream.write(buf, 0, count);
            }
        } catch (SocketTimeoutException e) {
            return generalCount;
        }
    }
}
