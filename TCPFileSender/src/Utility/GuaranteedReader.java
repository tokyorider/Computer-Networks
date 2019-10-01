package Utility;

import java.io.IOException;
import java.io.InputStream;

public class GuaranteedReader {

    public static byte[] guaranteedRead(InputStream inputStream, int sizeInBytes) throws IOException {
        byte[] buf = new byte[sizeInBytes];
        int count, generalCount = 0;
        while (generalCount < sizeInBytes) {
            count = inputStream.read(buf, generalCount, sizeInBytes - generalCount);
            if (count == -1) {
                    throw new IOException("Cant read required amount of bytes");
            }
            generalCount += count;
        }

        return buf;
    }

}