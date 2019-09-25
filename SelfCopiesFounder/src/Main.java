import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {

    public static void main(String[] args) {
        try {
            SelfCopiesDetector.detectCopies(InetAddress.getByName(args[0]));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

}