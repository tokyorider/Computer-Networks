package Utility;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class GuaranteedReader {

    public static byte[] guaranteedRead(InputStream inputStream, int sizeInBytes) throws IOException {
        byte[] buf = new byte[sizeInBytes];
        int count, generalCount = 0;
        while (generalCount < sizeInBytes) {
            count = inputStream.read(buf, generalCount, sizeInBytes - generalCount);
            if (count == -1) {
                    return Arrays.copyOfRange(buf, 0, generalCount);
            }
            generalCount += count;
        }

        return buf;
    }

}
