package com.example.fernando.labb3b;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Set;

import Model.Reader;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter adapter;
    private BluetoothDevice noninDevice = null;
    public static final int REQUEST_ENABLE_BT = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setUp();
        Log.d("bluetooth", "checking if device!");
        if(noninDevice != null){
            Log.d("bluetooth","New thread!");
            new Thread(new Reader(noninDevice,adapter,this.getApplicationContext())).start();
        }
        else
            Log.d("bluetooth","No Pair the device");
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
}