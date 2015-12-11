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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private Boolean running;
    public static final int REQUEST_ENABLE_BT = 42;
    private static final UUID STANDARD_SPP_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final byte[] FORMAT_8 = { 0x02, 0x70, 0x04, 0x02, 0x02, 0x00, (byte) 0x78, 0x03 };
    private static final byte ACK = 0x06;
    private static final byte[] NACK = {0x15};

    public Reader(BluetoothDevice device, BluetoothAdapter adapter, Context context, Handler mHandler, InetAddress ip, int port)
    {
        this.noninDevice = device;
        this.adapter = adapter;
        this.context = context;
        this.mHandler = mHandler;
        this.IPaddress = ip;
        this.porten = port;
        running = true;
        adapter.cancelDiscovery();
    }

    public void setRunning(Boolean running) {
        this.running = running;
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

                while(input.read(packet) != -1 && running)
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
                        mHandler.obtainMessage(val,plethValue).sendToTarget();
                        Log.d("bluetooth", outputString);
                        pw.println(String.valueOf(plethValue));

                    }
                }
            }
        } catch (IOException e) {
            
        } finally {
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
            sendDataToServer();
        }

    }

    private int unsignedByteToInt(byte b) {
        return (int) b & 0xFF;
    }

    private int getLeastBitFromByte(byte b)
    {
        return b & 0x01;
    }

    private int porten = 7344;;
    private static Socket clientSocket;
    private InetAddress IPaddress;
    private PrintWriter write;
    public void sendDataToServer() {
        try {
            Log.d("bluetooth","asda");
            ArrayList<String> data = loadData();
            Log.i("data","Size: " + data.size());
            //IPaddress  = InetAddress.getByName("130.229.174.233");
            clientSocket = new Socket(IPaddress, porten);
            Log.d("bluetooth","" + clientSocket.getInetAddress());
            write = new PrintWriter(clientSocket.getOutputStream(), true);


            for(int i = 0; i < data.size(); i++){
                write.println(data.get(i));
                write.flush();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(write != null)
                write.close();
            try {
                if(clientSocket != null)
                    clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private ArrayList<String> loadData() throws Exception {
        ArrayList<String> tmp = new ArrayList<String>();
        FileInputStream fis = null;
        BufferedReader br = null;
        DataInputStream in = null;

        try {
            fis = context.openFileInput("data.txt");

            br = new BufferedReader(new InputStreamReader(fis));
            String date = br.readLine();
            Log.i("data","" + date);
            String line;
            while ((line = br.readLine()) != null) {
                Log.i("data",line);
                tmp.add(line);
            }

        }catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        finally {
            if (br != null)
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if (in != null) {
                in.close();
            }
            if (fis != null)
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return tmp;
    }

    @Override
    public void run() {
        readDate();
    }
}
