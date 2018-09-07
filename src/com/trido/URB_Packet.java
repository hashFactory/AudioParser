package com.trido;

import java.util.ArrayList;
import java.util.Arrays;

public class URB_Packet
{
    int header_version = 0;
    int header_length = 0;
    int request_type = 0;
    int data_length = 0;
    int success = 0;
    int number_of_frames = 0;
    long ioID = 0;
    int device_location = 0;
    int speed = 0;
    int device_index = 0;
    int endpoint_address = 0;
    int isochronous = 0;
    int packet_length = 0;

    ArrayList<Subframe> frames = new ArrayList<>(32);

    public URB_Packet(int header_version, int header_length, int request_type, int data_length, int success, int number_of_frames, long ioID, int device_location, int speed, int device_index, int endpoint_address, int isochronous, int packet_length)
    {
        this.header_version = header_version;
        this.header_length = header_length;
        this.request_type = request_type;
        this.data_length = data_length;
        this.success = success;
        this.number_of_frames = number_of_frames;
        this.ioID = ioID;
        this.device_location = device_location;
        this.speed = speed;
        this.device_index = device_index;
        this.endpoint_address = endpoint_address;
        this.isochronous = isochronous;
        this.packet_length = packet_length;
    }

    public URB_Packet()
    {
    }

    @Override
    public String toString()
    {
        return "URB_Packet{" +
                "header_version=" + header_version +
                ", header_length=" + header_length +
                ", request_type=" + request_type +
                ", data_length=" + data_length +
                ", success=" + success +
                ", number_of_frames=" + number_of_frames +
                ", ioID=" + ioID +
                ", device_location=" + device_location +
                ", speed=" + speed +
                ", device_index=" + device_index +
                ", endpoint_address=" + endpoint_address +
                ", isochronous=" + isochronous +
                ", frames=" + frames +
                ", packet_length=" + packet_length +
                '}';
    }

    public static URB_Packet populate_URB_packet(byte[] urb_packet)
    {
        URB_Packet result = new URB_Packet();
        result.header_version = java.nio.ByteBuffer.wrap(Arrays.copyOfRange(urb_packet, 0, 2)).order(java.nio.ByteOrder.LITTLE_ENDIAN).getShort();
        result.header_length = java.nio.ByteBuffer.wrap(Arrays.copyOfRange(urb_packet, 2, 3)).order(java.nio.ByteOrder.LITTLE_ENDIAN).get();
        result.request_type = java.nio.ByteBuffer.wrap(Arrays.copyOfRange(urb_packet, 3, 4)).order(java.nio.ByteOrder.LITTLE_ENDIAN).get();
        result.data_length = java.nio.ByteBuffer.wrap(Arrays.copyOfRange(urb_packet, 4, 8)).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
        result.success = java.nio.ByteBuffer.wrap(Arrays.copyOfRange(urb_packet, 8, 12)).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
        result.number_of_frames = java.nio.ByteBuffer.wrap(Arrays.copyOfRange(urb_packet, 12, 16)).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
        result.ioID = java.nio.ByteBuffer.wrap(Arrays.copyOfRange(urb_packet, 16, 24)).order(java.nio.ByteOrder.LITTLE_ENDIAN).getLong();
        result.device_location = java.nio.ByteBuffer.wrap(Arrays.copyOfRange(urb_packet, 24, 28)).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
        result.speed = java.nio.ByteBuffer.wrap(Arrays.copyOfRange(urb_packet, 28, 29)).order(java.nio.ByteOrder.LITTLE_ENDIAN).get();
        result.device_index = java.nio.ByteBuffer.wrap(Arrays.copyOfRange(urb_packet, 29, 30)).order(java.nio.ByteOrder.LITTLE_ENDIAN).get();
        result.endpoint_address = java.nio.ByteBuffer.wrap(Arrays.copyOfRange(urb_packet, 30, 31)).order(java.nio.ByteOrder.LITTLE_ENDIAN).get();
        result.isochronous = java.nio.ByteBuffer.wrap(Arrays.copyOfRange(urb_packet, 31, 32)).order(java.nio.ByteOrder.LITTLE_ENDIAN).get();
        result.packet_length = result.data_length + result.header_length + 28 * result.number_of_frames;

        int progress = result.header_length;
        for (int i = 0; i < result.number_of_frames; i++)
        {
            //if (i > 0)
            //    progress += result.frames.get(i-1).length + result.frames.get(i-1).header_length;
            /*try
            {
                System.out.println(progress + " :: " + java.nio.ByteBuffer.wrap(Arrays.copyOfRange(urb_packet, progress + 4, progress + 8)).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt() + " :: " + Subframe.bytesToHex(Arrays.copyOfRange(urb_packet, progress + 4, progress + 8)));
            }
            catch (ArrayIndexOutOfBoundsException ex)
            {
                System.out.println("thing");
            }*/

            //System.out.println(progress);
            //System.out.println(progress + java.nio.ByteBuffer.wrap(Arrays.copyOfRange(urb_packet, progress + 4, progress + 8)).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt());

            //System.out.println(i + " - progress: " + progress);

            Subframe subframe = Subframe.populate_subframe(Arrays.copyOfRange(urb_packet, progress, progress + java.nio.ByteBuffer.wrap(Arrays.copyOfRange(urb_packet, progress + 4, progress + 8)).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt() + 28));
            result.frames.add(subframe);

            // Stupid programming forces payload in 8 byte increments but real audio is in 6 byte increments so sometimes it doesn't work
            int offset = 0;
            if (subframe.length / 8 == (float)subframe.length / 8.0)
                offset = subframe.length;
            else
                offset = (subframe.length / 8 + 1) * 8;

            progress += offset + 28;
            //System.out.println(result.frames.get(i));
        }

        return result;
    }
}
