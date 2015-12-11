package Model;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.UUID;


/**
 * Created by Fernando on 2015-12-10.
 */
public class Reader implements Runnable{
    private BluetoothAdapter adapter;
    private BluetoothDevice noninDevice;
    private Context context;
    private Handler mHandler;
    public static final int REQUEST_ENABLE_BT = 42;
    private static final UUID STANDARD_SPP_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final byte[] FORMAT_8 = { 0x02, 0x70, 0x04, 0x02, 0x02, 0x00, (byte) 0x78, 0x03 };
    private static final byte ACK = 0x06;
    private static final byte[] NACK = {0x15};

    public Reader(BluetoothDevice device, BluetoothAdapter adapter, Context context, Handler mHandler)
    {
        this.noninDevice = device;
        this.adapter = adapter;
        this.context = context;
        this.mHandler = mHandler;
        adapter.cancelDiscovery();
    }

    public void readDate()
    {

        Log.d("bluetooth","read date");
        String outputString="";

        BluetoothSocket socket = null;
        DateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd");

        FileOutputStream fos = null;
        PrintWriter pw = null;

        try {

            Log.d("bluetooth","Before socket");
            socket = noninDevice
                    .createRfcommSocketToServiceRecord(STANDARD_SPP_UUID);
            socket.connect();
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();

            Log.d("bluetooth","After socket");
            output.write(FORMAT_8);
            output.flush();
            byte[] reply = new byte[1];
            input.read(reply);
            byte[] packet = new byte[5];
            if(reply[0] == ACK)
            {
                Log.d("bluetooth","Read reply");
                String FILE_NAME = "data.txt";
                fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
                pw = new PrintWriter(fos);
                pw.println(simpleDate.format(new Date()));
                int plethValue = 0, pulseValue=0;
                byte msb = 0,lsb = 0;
                String lsbStr, res2, res3;
                int val;

                while(input.read(packet) != -1)
                {
                    plethValue = unsignedByteToInt(packet[2]);
                    if(getLeastBitFromByte(packet[1]) == 1)
                    {
                        msb = (byte) (packet[3] & 0x03);
                        String msbStr = Integer.toBinaryString(msb);

                        input.read(packet);
                        lsb = (byte) (packet[3] & 0x7F);
                        lsbStr = String.format("%16s", Integer.toBinaryString(lsb)).replace(" ", "0");
                        res2 = lsbStr.substring(lsbStr.length() - 7);
                        res3 = msbStr+res2;
                        val = Integer.parseInt(res3,2);
                        outputString = "Pleth: " + plethValue + " Pulse: " + pulseValue + " PulseStr: " + val;
                        mHandler.obtainMessage(val).sendToTarget();
                        Log.d("bluetooth", outputString);
                        pw.println(plethValue);
                    }
                }
            }
        } catch (IOException e) {
            
        }
        finally {
            if (pw != null) {
                pw.close();
            }
            try {
                if (fos != null) {
                    fos.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private int unsignedByteToInt(byte b) {
        return (int) b & 0xFF;
    }

    private int getLeastBitFromByte(byte b)
    {
        return b & 0x01;
    }

    @Override
    public void run() {
        readDate();
    }
}
