package com.trido;

import java.util.Arrays;

public class Subframe
{
    int header_length = 0;
    int length = 0;
    int success = 0;
    long frame_number = 0;
    long timestamp = 0;

    byte[] audio_data = null;

    public Subframe(int header_length, int length, int success, long frame_number, long timestamp, byte[] audio_data)
    {
        this.header_length = header_length;
        this.length = length;
        this.success = success;
        this.frame_number = frame_number;
        this.timestamp = timestamp;
        this.audio_data = audio_data;
    }

    public Subframe()
    {
    }

    @Override
    public String toString()
    {
        return "Subframe{" +
                "header_length=" + header_length +
                ", length=" + length +
                ", success=" + success +
                ", frame_number=" + frame_number +
                ", timestamp=" + timestamp +
                ", audio_data=" + Arrays.toString(audio_data) +
                '}';
    }

    /*
            Diagnostics
     */

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static Subframe populate_subframe(byte[] subframe)
    {
        Subframe result = new Subframe();

        result.header_length = java.nio.ByteBuffer.wrap(Arrays.copyOfRange(subframe, 0, 4)).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
        //System.out.println(bytesToHex(Arrays.copyOfRange(subframe, 4, 8)));
        result.length = java.nio.ByteBuffer.wrap(Arrays.copyOfRange(subframe, 4, 8)).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
        result.success = java.nio.ByteBuffer.wrap(Arrays.copyOfRange(subframe, 8, 12)).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
        result.frame_number = java.nio.ByteBuffer.wrap(Arrays.copyOfRange(subframe, 12, 20)).order(java.nio.ByteOrder.LITTLE_ENDIAN).getLong();
        result.timestamp = java.nio.ByteBuffer.wrap(Arrays.copyOfRange(subframe, 20, 28)).order(java.nio.ByteOrder.LITTLE_ENDIAN).getLong();

        result.audio_data = Arrays.copyOfRange(subframe, 28, result.length + 28);

        //System.out.println(result.audio_data.length);
        //System.out.println(bytesToHex(result.audio_data));

        return result;
    }
}
