package com.example.fernando.labb3b;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
    public static final int REQUEST_ENABLE_BT = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        pulseView = (TextView) findViewById(R.id.pulseTextView);
        startButton = (Button) findViewById(R.id.startButton);
        stopButton = (Button) findViewById(R.id.stopButton);
        startButton.setOnClickListener(new StartButtonlistener(this.getApplicationContext()));
        stopButton.setOnClickListener(new StopButtonListener());
        setUp();
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
            if(noninDevice != null){
                pulseHandler();
                reader = new Reader(noninDevice,adapter,ctx,pHandler);
                thread = new Thread(reader);
                thread.start();
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
            return true;
        }

        return super.onOptionsItemSelected(item);
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
            @Override
            public void handleMessage(Message inputMessage) {
                // Gets the image task from the incoming Message object.
                int pulseValue = (int) inputMessage.what;
                pulseView.setText(String.valueOf(pulseValue));
            }
        };

    }

    private void showToast(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        toast.show();
    }
}
