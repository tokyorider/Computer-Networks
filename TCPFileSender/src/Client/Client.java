package Client;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Client {

    public static void main(String[] args) {
        File file = new File(args[0]);
        try {
            if (file.exists()) {
                FileSender.send(file, InetAddress.getByName(args[1]), Integer.parseInt(args[2]));
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

}
