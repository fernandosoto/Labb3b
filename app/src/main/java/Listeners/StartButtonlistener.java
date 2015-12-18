package Listeners;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.content.Intent;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.w3c.dom.Text;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Set;

import Model.Reader;

/**
 * Created by Fernando on 2015-12-18.
 */
public class StartButtonlistener implements View.OnClickListener{
    public static final int REQUEST_ENABLE_BT = 42;
    private Context ctx;
    private BluetoothAdapter adapter;
    private LineGraphSeries<DataPoint> lineGraphSeries;
    private GraphView graph;
    private BluetoothDevice noninDevice;
    private Reader reader;
    private Thread thread;
    private Handler pHandler;
    private TextView pulseView;
    public StartButtonlistener(Context ctx, BluetoothAdapter adapter, GraphView graph,
                               LineGraphSeries<DataPoint> lineGraphSeries, BluetoothDevice device,
                               Reader reader, Thread thread, Handler pHandler, TextView pulseView)
    {
        this.adapter = adapter;
        this.ctx = ctx;
        this.graph = graph;
        this.lineGraphSeries = lineGraphSeries;
        this.noninDevice = device;
        this.reader = reader;
        this.thread = thread;
        this.pHandler = pHandler;
        this.pulseView = pulseView;
    }
    @Override
    public void onClick(View v) {
        initGraph();
        setUp();
        if(noninDevice != null && noninDevice.getName().contains("Nonin")){
            pulseHandler();
            SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(ctx);
            try {
                InetAddress ip = InetAddress.getByName(sh.getString("pref_ipAddress", ""));
                int porten = Integer.parseInt(sh.getString("pref_port",""));
                reader = new Reader(noninDevice,adapter,ctx,pHandler,ip,porten);
                thread = new Thread(reader);
                thread.start();
            } catch (UnknownHostException e) {
                showToast("Ip address not found!");
            }
        }
        else
        {
            showToast("No device found!");
        }
    }

    public void setUp()
    {

        adapter = BluetoothAdapter.getDefaultAdapter();
        if(adapter == null)
            Log.d("bluetooth", "adapter is null");
        if (!adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            Activity a = (Activity) ctx;
            a.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            getDevice();
        }
    }



    private void getDevice()
    {
        noninDevice = null;
        Set<BluetoothDevice> pairedBTDevices = adapter.getBondedDevices();
        if(pairedBTDevices.size() > 0)
        {
            for (BluetoothDevice device : pairedBTDevices) {
                if(device.getName().contains("Nonin"))
                {
                    noninDevice = device;
                    Log.d("bluetooth","Paired the device");
                }
            }
        }
    }

    private void initGraph(){
        lineGraphSeries = new LineGraphSeries<>(new DataPoint[]{new DataPoint(0,0)});
        graph.addSeries(lineGraphSeries);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(20);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(250);
    }

    private void showToast(String msg) {
        Toast toast = Toast.makeText(ctx, msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void pulseHandler()
    {

        final Calendar cal = Calendar.getInstance();
        pHandler = new Handler(Looper.getMainLooper()){
            double graphX = 1;
            @Override
            public void handleMessage(Message inputMessage) {
                // Gets the image task from the incoming Message object.
                int pulseValue = (int) inputMessage.what;
                pulseView.setText(String.valueOf(pulseValue));
                int plethValue = (int) inputMessage.obj;
                double x = (Calendar.getInstance().getTimeInMillis()-cal.getTimeInMillis())/1000;
                lineGraphSeries.appendData(new DataPoint(graphX,plethValue),true,2000);
                graphX+= 0.33/25;
            }
        };

    }
}
