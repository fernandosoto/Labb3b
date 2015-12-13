package com.example.fernando.labb3b;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;

import Model.Reader;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter adapter;
    private BluetoothDevice noninDevice = null;
    private Handler pHandler;
    private TextView pulseView;
    private Button startButton;
    private Button stopButton;
    private Reader reader;
    private Thread thread;
    private GraphView graph;
    private boolean inSettings;
    private LineGraphSeries<DataPoint> lineGraphSeries;
    public static final int REQUEST_ENABLE_BT = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inSettings = false;
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        pulseView = (TextView) findViewById(R.id.pulseTextView);
        startButton = (Button) findViewById(R.id.startButton);
        stopButton = (Button) findViewById(R.id.stopButton);
        graph = (GraphView) findViewById(R.id.graphView);
        startButton.setOnClickListener(new StartButtonlistener(this.getApplicationContext()));
        stopButton.setOnClickListener(new StopButtonListener());
        Log.d("bluetooth", "checking if device!");
    }

    protected class StartButtonlistener implements View.OnClickListener{

        private Context ctx;
        public StartButtonlistener(Context ctx)
        {
            this.ctx = ctx;
        }
        @Override
        public void onClick(View v) {
            setUp();
            if(noninDevice != null){
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
    }

    protected class StopButtonListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            pulseView.setText("");
            reader.setRunning(false);
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        lineGraphSeries = new LineGraphSeries<>(new DataPoint[]{new DataPoint(0,0)});
        graph.addSeries(lineGraphSeries);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(20);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(200);
        super.onResume();
    }

    @Override
    protected void onPause() {
        pulseView.setText("");
        if(reader!=null)
            reader.setRunning(false);
        try {
            if(thread != null)
                thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new SettingsFragment())
                    .addToBackStack("settings")
                    .commit();
            inSettings = true;
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        if(inSettings)
        {
            backFromSettings();
            return;
        }
        super.onBackPressed();
    }

    private void backFromSettings()
    {
        inSettings = false;
        getFragmentManager().popBackStack();
    }



    public void setUp()
    {

        adapter = BluetoothAdapter.getDefaultAdapter();
        if(adapter == null)
            Log.d("bluetooth", "adapter is null");
        if (!adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
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

    private void pulseHandler()
    {
        pHandler = new Handler(Looper.getMainLooper()){
            int graphX = 1;
            @Override
            public void handleMessage(Message inputMessage) {
                // Gets the image task from the incoming Message object.
                int pulseValue = (int) inputMessage.what;
                pulseView.setText(String.valueOf(pulseValue));
                int plethValue = (int) inputMessage.obj;
                lineGraphSeries.appendData(new DataPoint(graphX,plethValue),true,50);
                graphX++;
            }
        };

    }

    private void showToast(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        toast.show();
    }
}
