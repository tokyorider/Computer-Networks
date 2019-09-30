package Utility;

public class Converter {

    public static <T extends Number> byte[] convertToByteArr(T num, int sizeInBytes) {
        byte[] arr = new byte[sizeInBytes];
        for (int i = 0; i < sizeInBytes; ++i) {
            arr[i] = (byte)(num.longValue() >> ((sizeInBytes - 1 - i) * 8));
        }

        return arr;
    }

    public static long convertToNumber(byte[] arr, int size) {
        long num = 0;
        for (int i = 0; i < size; ++i) {
            num += convertToUnsigned(arr[i]) *  Math.pow(2, (size - 1 - i) * 8);
        }

        return num;
    }

    private static short convertToUnsigned(byte num) {
        return (num >= 0) ? num : (short) (256 + num);
    }

}
