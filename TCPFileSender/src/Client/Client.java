package Client;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;

public class Client {

    public static void main(String[] args) {
        File file = new File(args[0]);
        try {
            if (file.exists()) {
                FileSender.send(file, InetAddress.getByName(args[1]), Integer.parseInt(args[2]));
            } else {
                throw new FileNotFoundException("Error: file doesn't exist");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("File sending isn't successful");
        }
    }

}