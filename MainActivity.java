package com.example.shakila.wakeupalarmsystem;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    TimePicker alarmTimePicker;
    PendingIntent pendingIntent;
    AlarmManager alarmManager;
    FirebaseDatabase database;
    DatabaseReference myRef;
    ConnectedThread thread;


    private BluetoothDevice mDevice;
    private ConnectThread mConnectThread;
    private ArduinoData mConnectedThread;
    private BluetoothSocket btSocket;
    BluetoothAdapter myBT = null;
    private String address;
    private boolean isBTConnected;
    public static final String PREFS_NAME = "ConnectedAddressFile";
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private Button btnAlarmSnooze;
    private Button btnAlarmStop;
    public ToggleButton toggle;
    private ProgressDialog progress;
    private Button viewStats;
    private Switch bulb;

    private InputStream in;
    private OutputStream out;
    private boolean alarmRunning = false;
    private boolean alarmOn = false;
    private String alarmSession = "";
    private boolean correct;
    private AlertDialog.Builder builder;
    private int correctCount = 0;


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        loadBluetoothActivity();
        bulb = (Switch) findViewById(R.id.bulb);
        bulb.setChecked(false);
        bulb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(bulb.isChecked()) thread.write("3");
                else thread.write("2");
            }
        });
        alarmTimePicker = (TimePicker) findViewById(R.id.timePicker);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        toggle = (ToggleButton) findViewById(R.id.toggleButton);
        viewStats = (Button) findViewById(R.id.btnViewStats);
        viewStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(alarmSession != "" || alarmSession != null){
                    Intent intent = new Intent(MainActivity.this, Stats.class);
                    intent.putExtra("alarmSession", alarmSession);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(),"No Sessions Done!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button btnBluetooth = (Button) findViewById(R.id.btnBT);
        btnBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadBluetoothActivity();
            }
        });

        btnAlarmSnooze = (Button) findViewById(R.id.btnAlarmSnooze) ;
        btnAlarmSnooze.setVisibility(View.INVISIBLE);
        btnAlarmSnooze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                loadAlarmActivity();
            }
        });

        btnAlarmStop = (Button) findViewById(R.id.btnAlarmStop) ;
        btnAlarmStop.setVisibility(View.INVISIBLE);
        btnAlarmStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                stopAlarm();
            }
        });

    };

    public void loadBluetoothActivity() {
        Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
        startActivityForResult(intent, 1);

    }



    public void loadAlarmActivity() {
        alarmRunning = true;
        AlarmReceiver.stopRingtone();
        long seconds = System.currentTimeMillis();
        alarmManager.cancel(pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, seconds+60000, 60000, pendingIntent);
    }

    public void getDataFromBM() {
        ConnectBT con = new ConnectBT();
        con.execute();
    }

    public void stopAlarm(){

            if(alarmRunning){
                bulb.setChecked(false);
                AlarmReceiver.stopRingtone();
            }
            alarmManager.cancel(pendingIntent);
            alarmRunning = false;
            alarmOn = false;
            toggle.setChecked(false);

    }

    public boolean generateEquation() {
        correct = false;
        while (true) {

            if(correctCount == 2){
                return true;
            }

            int num1 = (int)Math.random() * 100;
            int num2 = (int)Math.random() * 100;
            int answer = num1 * num2;
            builder = new AlertDialog.Builder(getApplicationContext());
            builder.setMessage(num1+" * "+num2);
            builder.setPositiveButton(answer+"", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {
                    correct = true;
                    correctCount++;
                }
            });
            builder.setNegativeButton(((int)Math.random() * 50+answer)+"", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            correct = false;
                            correctCount = 0;
                            finish();
                        }
                    });
         builder.create();

            return false;
        }

        //return false;

    }

    public void logActivity() {
        Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
        startActivity(intent);
        this.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1){
            if(resultCode==RESULT_OK){
                address = data.getStringExtra(BluetoothActivity.EXTRA_ADDRESS);
                getDataFromBM();
            }
        }
    }

    public void initialiseBulb(BluetoothSocket btSocket) {
        if(isBTConnected){
            try {
                out = btSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String text = "2";
            byte[] msgBuffer = text.getBytes();           //converts entered String into bytes
        try {
            out.write(msgBuffer);                //write bytes over BT connection via outstream
        } catch (IOException e) {
            //if you cannot write, close the application
            Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
            finish();

        }
        }
    }

    public void OnToggleClicked (View view) {

        long time;
        if (((ToggleButton) view).isChecked()) {

            Toast.makeText(MainActivity.this, "ALARM ON", Toast.LENGTH_SHORT).show();
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, alarmTimePicker.getCurrentHour());
            calendar.set(Calendar.MINUTE, alarmTimePicker.getCurrentMinute());
            DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
            String date = df.format(Calendar.getInstance().getTime());
            alarmOn = true;
            alarmSession = "New session : "+date;
            myRef.child(alarmSession).setValue("");
            Intent intent = new Intent(this, AlarmReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

            time = (calendar.getTimeInMillis() - (calendar.getTimeInMillis() % 60000));
            if (System.currentTimeMillis() > time) {
                if (calendar.AM_PM == 0)
                    time = time + (1000 * 60 * 60 * 12);
                else
                    time = time + (1000 * 60 * 60 * 24);
            }
            alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
            btnAlarmSnooze.setVisibility(View.VISIBLE);
            btnAlarmStop.setVisibility(View.VISIBLE);
            //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time, 60000, pendingIntent);
        } else {
            alarmManager.cancel(pendingIntent);
            Toast.makeText(MainActivity.this, "ALARM OFF", Toast.LENGTH_SHORT).show();
        }
    }

    private class ConnectBT extends AsyncTask<Void,Void,Void> {//UI thread

        String fileName = PREFS_NAME;
        private boolean ConnectSuccess =true;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(MainActivity.this, "Connecting... ", "Please Wait");
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

            byte[] buffer = new byte[1024];
            int bytes;

            super.onPostExecute(result);
            if (!ConnectSuccess) {
                Toast.makeText(getApplicationContext(), "Please Select your Device", Toast.LENGTH_LONG).show();
                SharedPreferences prefs = getSharedPreferences(fileName, 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("hasAddress", false);
                editor.putString("deviceName", null);
                editor.putString("address", null);
                editor.commit();
            } else {
                Toast.makeText(getApplicationContext(), "Connected :)", Toast.LENGTH_LONG).show();
                isBTConnected = true;
                initialiseBulb(btSocket);
                thread = new ConnectedThread(btSocket);
                thread.start();
            }

            progress.dismiss();
        }

    }

    private class ConnectedThread extends Thread {
        private String msgReceived = "";

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {

            try {
                in = btSocket.getInputStream();
                out = btSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {

            byte[] buffer = new byte[256];
            int bytes;
            int count = 0;
            // Keep looping to listen for received messages
            while (true) {
                try {
                    alarmRunning = AlarmReceiver.alarmRunning;
                    bytes = in.read(buffer);
                    String strReceived = new String(buffer, 0, bytes);
                    msgReceived = msgReceived + strReceived;
                    System.out.println("============"+msgReceived.trim());
                    String[] array = msgReceived.split("\n");
                    System.out.println("Array value 1 "+array[array.length-1]);
                    if(array[array.length-1].trim().equalsIgnoreCase("no motion")){
                        System.out.println("No motion detected so far!");
                        count = 0;
                    } else if (array[array.length-1].trim().equalsIgnoreCase("motion detected")){
                        System.out.println("motion detected!!");
                        if(alarmOn){
                            DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                            String date = df.format(Calendar.getInstance().getTime());
                            myRef.child(alarmSession).child(date).setValue("Motion Detected");
                        }
                        count++;
                        if(alarmRunning && count > 5){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    stopAlarm();
                                }
                            });

                        }
                    }

                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                out.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                finish();

            }
        }


    }



}

