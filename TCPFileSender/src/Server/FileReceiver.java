package Server;

import Utility.GuaranteedReader;
import Utility.Pair;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

class FileReceiver implements Runnable {

    private Socket socket;

    private static final int BUF_SIZE = 1 << 20; //megabyte

    private Timer timer = new Timer(true);

    private Long generalCount = (long)0, temporaryCount = (long)0;

    private int instantSpeed; //kilobytes/second

    class AverageSpeed extends TimerTask {

        private Date start;

        AverageSpeed(Date start) {
            this.start = start;
        }

        @Override
        public void run() {
            System.out.println(socket.getInetAddress().getHostAddress() + ":\nInstant uploading speed = " + instantSpeed + " kBytes/s");
            synchronized (generalCount) {
                System.out.println("Average uploading speed = " +
                        (1000 * generalCount / (new Date().getTime() - start.getTime())) / 1024 + " kBytes/s");
            }
        }

    }

    class InstantSpeed extends TimerTask {

        private final double timeInterval;

        InstantSpeed(double timeInterval) {
            this.timeInterval = timeInterval;
        }

        @Override
        public void run() {
            synchronized (temporaryCount) {
                instantSpeed = (int) ((temporaryCount / (timeInterval * 1024)));
                temporaryCount = (long) 0;
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
            socketInputStream = socket.getInputStream();
            header = receiveHeader(socketInputStream);
        } catch(Exception e) {
            try {
                socket.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            e.printStackTrace();
            return;
        }

        try(OutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(
                new File("Uploads" + File.pathSeparator + header.getFirst()))))
        {
            receiveFile(socketInputStream, fileOutputStream);
            if (generalCount.equals(header.getSecond())) {
                socket.getOutputStream().write(0);
            } else {
                socket.getOutputStream().write(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
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
                GuaranteedReader.guaranteedRead(socketInputStream, Short.BYTES)).clear().getShort();
        String fileName = getUniqueFileName(new File(new String(GuaranteedReader.guaranteedRead(socketInputStream,
                fileNameLength), StandardCharsets.UTF_8)).getName());
        long fileLength = ByteBuffer.allocate(Long.BYTES).put(
                GuaranteedReader.guaranteedRead(socketInputStream, Long.BYTES)).clear().getLong();
        return new Pair<>(fileName, fileLength);
    }

    private static String getUniqueFileName(String firstFileName) {
        String fileName = firstFileName;
        int number = 1;
        while (new File("Uploads" + File.pathSeparator + fileName).exists()) {
            fileName = firstFileName + "(" + number + ")";
            number++;
        }

        return fileName;
    }

    private void receiveFile(InputStream socketInputStream, OutputStream fileOutputStream) throws IOException {
        int count;
        byte[] buf = new byte[BUF_SIZE];
        Date start = new Date();
        timer.scheduleAtFixedRate(new AverageSpeed(start), 3000, 3000);
        timer.scheduleAtFixedRate(new InstantSpeed(1), 1000, 1000);

        while ((count = socketInputStream.read(buf)) != -1) {
            synchronized (temporaryCount) {
                temporaryCount += count;
            }
            synchronized (generalCount) {
                generalCount += count;
            }
            fileOutputStream.write(buf, 0, count);
        }

        timer.cancel();
        timer = new Timer(true);
        timer.schedule(new InstantSpeed((double)(new Date().getTime() - start.getTime()) / 1000), 0);
        timer.schedule(new AverageSpeed(start), 0);
    }

}