import java.io.IOException;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class SelfCopiesDetector {

    private static final int PORT = 5555;

    private static final int TIMEOUT = 5000; // milliseconds

    private static final Timer sendTimer = new Timer(true);

    public static void detectCopies(InetAddress multicastAddress) {
        try(MulticastSocket multicastSocket = new MulticastSocket(new InetSocketAddress(PORT))) {
            multicastSocket.joinGroup(multicastAddress);
            multicastSocket.setSoTimeout(TIMEOUT);
            byte[] recvBuffer = new byte[0];
            DatagramPacket recvPacket = new DatagramPacket(recvBuffer, 0);
            ConcurrentHashMap<InetAddress, Boolean> activeCopies = new ConcurrentHashMap<>();

            setTimer(multicastSocket, multicastAddress);
            while (true) {
                activeCopies.forEach((inetAddress, aBoolean) -> activeCopies.put(inetAddress, false));
                try {
                    while (true) {
                        multicastSocket.receive(recvPacket);
                        if (!activeCopies.containsKey(recvPacket.getAddress())) {
                            activeCopies.put(recvPacket.getAddress(), true);
                            System.out.println(recvPacket.getAddress().getHostAddress() + " joined.");
                            printActiveCopies(activeCopies);
                        } else {
                            activeCopies.put(recvPacket.getAddress(), true);
                        }
                    }
                } catch (SocketTimeoutException e) {

                }

                for (InetAddress address : activeCopies.keySet()) {
                    if (!activeCopies.get(address)) {
                        activeCopies.remove(address);
                        System.out.println(address.getHostAddress() + " left.");
                        printActiveCopies(activeCopies);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printActiveCopies(ConcurrentHashMap<InetAddress, Boolean> copiesList) {
        System.out.println("List of active copies:");
        copiesList.forEach(((inetAddress, aBoolean) -> System.out.println(inetAddress.getHostAddress())));
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
        }, 0, TIMEOUT);
    }

}