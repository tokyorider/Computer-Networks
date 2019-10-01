package Server;

import Utility.GuaranteedReader;
import Utility.Pair;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

class FileReceiver implements Runnable {

    private Socket socket;

    private static final int TIMEOUT = 1000, BUF_SIZE = 1 << 20; //megabyte

    private Timer timer = new Timer(true), updateTimer = new Timer(true);

    private long generalCount = 0, temporaryCount = 0;

    private int instantSpeed; //kilobytes/second

    private Semaphore mutex = new Semaphore(1);

    class AverageSpeed extends TimerTask {

        private Date start;

        AverageSpeed(Date start) {
            this.start = start;
        }

        @Override
        public void run() {
            System.out.println(socket.getInetAddress().getHostAddress() + ":\nInstant uploading speed = " + instantSpeed + " kBytes/s");
            System.out.println("Average uploading speed = " +
                    (1000 * generalCount / (new Date().getTime() - start.getTime())) / 1024 + " kBytes/s");
        }

    }

    class InstantSpeed extends TimerTask {

        private final double timeInterval;

        InstantSpeed(double timeInterval) {
            this.timeInterval = timeInterval;
        }

        @Override
        public void run() {
            try {
                mutex.acquire();
                instantSpeed = (int) ((temporaryCount / (timeInterval * 1024)));
                temporaryCount = 0;
                mutex.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public FileReceiver(Socket socket) {
        this.socket = socket;
        new Thread(this).start();
    }

    @Override
    public void run() {
        InputStream socketInputStream;
        Pair<String, Long> header;
        try {
            socket.setSoTimeout(TIMEOUT);
            socketInputStream = socket.getInputStream();
            header = receiveHeader(socketInputStream);
        } catch(Exception e) {
            e.printStackTrace();
            return;
        }

        try(OutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(
                new File("Uploads" + File.pathSeparator + header.getFirst()))))
        {
            receiveFile(socketInputStream, fileOutputStream);
            if (generalCount == header.getSecond()) {
                socket.getOutputStream().write(0);
            } else {
                socket.getOutputStream().write(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            updateTimer.cancel();
            timer.cancel();
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Pair<String, Long> receiveHeader(InputStream socketInputStream) throws IOException {
        short fileNameLength = ByteBuffer.allocate(Short.BYTES).put(
                GuaranteedReader.guaranteedRead(socketInputStream, Short.BYTES)).getShort();
        String fileName = new String(GuaranteedReader.guaranteedRead(socketInputStream, fileNameLength),
                StandardCharsets.UTF_8);
        long fileLength = ByteBuffer.allocate(Long.BYTES).put(
                GuaranteedReader.guaranteedRead(socketInputStream, Long.BYTES)).getLong();
        return new Pair<>(fileName, fileLength);
    }

    private void receiveFile(InputStream socketInputStream, OutputStream fileOutputStream) throws IOException {
        byte[] buf = new byte[BUF_SIZE];
        Date start = new Date();
        timer.scheduleAtFixedRate(new AverageSpeed(start), 3000, 3000);
        updateTimer.scheduleAtFixedRate(new InstantSpeed(1), 1000, 1000);

        try {
            while (true) {
                int count = socketInputStream.read(buf);
                try {
                    mutex.acquire();
                    temporaryCount += count;
                    mutex.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                generalCount += count;
                fileOutputStream.write(buf, 0, count);
            }
        } catch (SocketTimeoutException e) {
            timer.schedule(new AverageSpeed(start), 0);
        }
    }

}