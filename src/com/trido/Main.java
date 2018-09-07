package com.trido;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    public static File xhc20 = new File("amazing");

    public static void main(String[] args)
    {
        if (xhc20.exists())
            System.out.println("Exists !!");

        byte[] file_contents = null;
        try
        {
            file_contents = Files.readAllBytes(xhc20.toPath());
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        read(file_contents);

        System.exit(0);
    }

    public static int indexOf(byte[] outerArray, byte[] smallerArray, int offset) {
        for(int i = offset; i < outerArray.length - smallerArray.length+1; ++i) {
            boolean found = true;
            for(int j = 0; j < smallerArray.length; ++j) {
                if (outerArray[i+j] != smallerArray[j]) {
                    found = false;
                    break;
                }
            }
            if (found) return i;
        }
        return -1;
    }

    public static ByteArrayOutputStream strip_data(ArrayList<URB_Packet> urb_packets)
    {
        ByteArrayOutputStream raw_data = new ByteArrayOutputStream();

        // Not pretty but if value is 000000 * 3 then skip

        byte[] thing = new byte[] {0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0x7F};
        byte[] target = new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        try
        {
        for (URB_Packet urb : urb_packets)
        {
            for (int j = 0; j < urb.number_of_frames; j++)
            {
                Subframe sub = urb.frames.get(j);
                System.out.println(Subframe.bytesToHex(sub.audio_data));
                int value = 0;
                int value2 = 0;
                for (int i = sub.audio_data.length - 8; i < sub.audio_data.length; i++)
                    value += sub.audio_data[i];
                for (int i = 0; i < 8; i++)
                    value2 += sub.audio_data[i];

                if (j == urb.number_of_frames - 1)
                    raw_data.write(Arrays.copyOfRange(sub.audio_data, 0, sub.audio_data.length - 12));
                else
                    raw_data.write(sub.audio_data);
            }
        }
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return raw_data;
    }

    // TODO: Output raw audio to file

    public static long find_next_URB_header(byte[] contents, long start)
    {
        boolean found_it = false;
        int possibility = (int)start;

        byte[] target = new byte[] {0x00, 0x01, 0x20, 0x01};
        //byte[] target = new byte[] {0x0A, 0x0D, 0x0D, 0x0A};

        while (!found_it)
        {
            String content_string = new String(contents, StandardCharsets.UTF_8);
            String target_string = new String(target, StandardCharsets.UTF_8);

            possibility = indexOf(contents, target, possibility + 1);
            if (java.nio.ByteBuffer.wrap(Arrays.copyOfRange(contents, possibility + 4, possibility + 8)).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt() > 8400)
                found_it = true;
            //System.out.println("content_string: " + Arrays.copyOfRange(contents, possibility + 4, possibility + 8) + " ; value: " + java.nio.ByteBuffer.wrap(Arrays.copyOfRange(contents, possibility + 4, possibility + 8)).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt() + " ; possibility: " + possibility);
        }

        //System.out.println("Possibility: " + possibility);
        return possibility;
    }

    public static void read(byte[] contents)
    {
        System.out.println("Length: " + contents.length);

        ArrayList<URB_Packet> urb_packets = new ArrayList<>(contents.length / 2000);

        long index = 0;
        while (index < contents.length)
        {
            index = find_next_URB_header(contents, index);
            if (index < 0)
                break;
            URB_Packet packet = URB_Packet.populate_URB_packet(Arrays.copyOfRange(contents, (int)index, (int)index + 28 * java.nio.ByteBuffer.wrap(Arrays.copyOfRange(contents, (int)index + 12, (int)index + 16)).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt() + java.nio.ByteBuffer.wrap(Arrays.copyOfRange(contents, (int)index + 2, (int)index + 3)).order(java.nio.ByteOrder.LITTLE_ENDIAN).get() + java.nio.ByteBuffer.wrap(Arrays.copyOfRange(contents, (int)index + 4, (int)index + 8)).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt()));
            //System.out.println(new String(Arrays.copyOfRange(contents, (int)index + 6, (int)index + 7)));
            urb_packets.add(packet);
            //System.out.println(index - 1 + " length: " + packet.header_length + " speed: " + packet.request_type + " device_location: " + packet.device_location);
        }

        ByteArrayOutputStream raw_data = strip_data(urb_packets);
        FileOutputStream output = null;
        try
        {
            output = new FileOutputStream(new File("output_data"));
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        try
        {
            raw_data.writeTo(output);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
