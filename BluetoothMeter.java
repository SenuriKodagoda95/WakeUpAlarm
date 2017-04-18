package com.example.shakila.wakeupalarmsystem;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class BluetoothMeter extends AppCompatActivity {

    public static final String PREFS_NAME = "ConnectedAddressFile";
    Button btnOn,btnOff,btnDis;
    SeekBar brightness;
    //String btDevicename = null;
    String address =null;
    private ProgressDialog progress;
    BluetoothAdapter myBT = null;
    BluetoothSocket btSocket = null;
    private boolean isBTConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate (Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_meter);

        //recieve address
        Intent intent = getIntent();
        address = intent.getStringExtra(BluetoothActivity.EXTRA_ADDRESS);

        /*if (intent.getExtras() == null) {
            address = getAddress();
        }
        Log.d("Addresssssssssss",address);
        if (address.equalsIgnoreCase("NoAddress")) {
            Log.d("Addresssssssssss","Innnnnnnnnnnnnnnnn");
            Intent i = new Intent(this, BluetoothActivity.class);
            startActivity(i);
            this.finish();
            return;
            Log.d("Addresssssssssss","Innnnnnnnnnnnnnnnnrrrrrrrrrrrr");
        }*/

        //widgets
        btnOn = (Button)findViewById(R.id.btnOn);
        btnOff = (Button)findViewById(R.id.btnOff);
        btnDis = (Button)findViewById(R.id.btnDis);
        brightness = (SeekBar)findViewById(R.id.barBrightness);

        ConnectBT conn = new ConnectBT();
        conn.execute();

        btnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOnLed();
            }
        });

        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOffLed();
            }
        });

        btnDis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect();
            }
        });
    }


    private class ConnectBT extends AsyncTask<Void,Void,Void> {//UI thread

        String fileName = PREFS_NAME;
        private boolean ConnectSuccess =true;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(BluetoothMeter.this, "Connecting... ", "Please Wait");
        }

        @Override
        protected Void doInBackground (Void...devices) {
            try {
                if (btSocket == null || !isBTConnected) {
                    myBT = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = myBT.getRemoteDevice(address);
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            }
            catch (IOException e) {
                ConnectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute (Void result) {

            super.onPostExecute(result);
            if (!ConnectSuccess) {
                Toast.makeText(getApplicationContext(), "Please Select your Device", Toast.LENGTH_LONG).show();
                SharedPreferences prefs = getSharedPreferences(fileName, 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("hasAddress", false);
                editor.putString("deviceName", null);
                editor.putString("address", null);
                editor.commit();
                goToMainActivity();
            } else {
                Toast.makeText(getApplicationContext(), "Connected :)", Toast.LENGTH_LONG).show();
                saveAddress();
                isBTConnected = true;
            }
            progress.dismiss();
        }

    }

    //launch MAIN ACTIVTTY
    private void goToMainActivity() {
        Intent i = new Intent(BluetoothMeter.this,MainActivity.class);
        startActivity(i);
        this.finish();
    }

    private void Disconnect() {
        //if bt socket busy
        if (btSocket != null) {
            try {
                btSocket.close(); //close conn
            }
            catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Error Closing Connection", Toast.LENGTH_LONG).show();
            }
            finish();//return
        }
    }

    private void turnOffLed() {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write("2".toString().getBytes());
            }
            catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Error Turning On.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void turnOnLed() {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write("3".toString().getBytes());
            }
            catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Error Turning Off.", Toast.LENGTH_LONG).show();
            }
        }
    }

    void saveAddress() {
        SharedPreferences sharedPref = getSharedPreferences("btAddress", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("address",address);
        editor.apply();
        Toast.makeText(this,"Saved address",Toast.LENGTH_SHORT).show();
    }

    String getAddress() {
        SharedPreferences sharedPref = getSharedPreferences("btAddress", Context.MODE_PRIVATE);
        String address = sharedPref.getString("btAddress",null);
        if (address == null) {
            address = "NoAddress";
        }
        return  address;
    }

}
