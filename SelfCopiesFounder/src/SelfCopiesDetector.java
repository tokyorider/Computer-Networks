import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class SelfCopiesDetector {

    private static final int PORT = 5555;

    //all milliseconds
    private static final int TIMEOUT = 5000;

    private static final int PERIOD = TIMEOUT;

    private static final int DELAY = 0;

    private static final int ACTIVE_TIME = TIMEOUT * 2;

    private static final Timer sendTimer = new Timer(true);

    public static void detectCopies(InetAddress multicastAddress) {
        try(MulticastSocket multicastSocket = new MulticastSocket(new InetSocketAddress(PORT))) {
            multicastSocket.joinGroup(multicastAddress);
            multicastSocket.setSoTimeout(TIMEOUT);
            byte[] recvBuffer = new byte[0];
            DatagramPacket recvPacket = new DatagramPacket(recvBuffer, 0);
            ConcurrentHashMap<InetAddress, Date> activeCopies = new ConcurrentHashMap<>();

            setTimer(multicastSocket, multicastAddress);
            while (true) {
                removeInactiveCopies(activeCopies);

                try {
                    multicastSocket.receive(recvPacket);
                } catch (SocketTimeoutException e) {
                    continue;
                }
                if (!activeCopies.containsKey(recvPacket.getAddress())) {
                    activeCopies.put(recvPacket.getAddress(), new Date());
                    System.out.println(recvPacket.getAddress().getHostAddress() + " joined.");
                    printActiveCopies(activeCopies);
                } else {
                    activeCopies.put(recvPacket.getAddress(), new Date());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printActiveCopies(ConcurrentHashMap<InetAddress, Date> copiesList) {
        System.out.println("List of active copies:");
        copiesList.forEach(((inetAddress, date) -> System.out.println(inetAddress.getHostAddress())));
        System.out.print("\n");
    }

    private static void setTimer(MulticastSocket multicastSocket, InetAddress multicastAddress) {
        sendTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    multicastSocket.send(new DatagramPacket(new byte[0], 0, multicastAddress, PORT));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, DELAY, PERIOD);
    }

    private static void removeInactiveCopies(ConcurrentHashMap<InetAddress, Date> activeCopies) {
        activeCopies.forEach(((inetAddress, date) -> {
            if (new Date().getTime() - activeCopies.get(inetAddress).getTime() > ACTIVE_TIME) {
                activeCopies.remove(inetAddress);
                System.out.println(inetAddress.getHostAddress() + " left");
                printActiveCopies(activeCopies);
            }
        }));
    }

}