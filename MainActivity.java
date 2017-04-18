package com.example.shakila.wakeupalarmsystem;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    TimePicker alarmTimePicker;
    PendingIntent pendingIntent;
    AlarmManager alarmManager;

    private BluetoothDevice mDevice;
    private ConnectThread mConnectThread;
    private ArduinoData mConnectedThread;
    private BluetoothSocket mmSocket;

    private Button btnAlarmStop;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alarmTimePicker = (TimePicker) findViewById(R.id.timePicker);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Button btnBluetooth = (Button) findViewById(R.id.btnBT);
        btnBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(Intent.ACTION_MAIN, null);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.bluetooth.BluetoothSettings");
                intent.setComponent(cn);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        btnAlarmStop = (Button) findViewById(R.id.btnAlarmStop) ;
        btnAlarmStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                loadAlarmActivity();
            }
        });

        Button btnBT = (Button) findViewById(R.id.btnBT);
        btnBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                Log.i("clicks", "You Clicked");
                loadBluetoothActivity();
            }
        });

        Button btnArduino = (Button) findViewById(R.id.btnArduino);
        btnArduino.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                //SensorLog();
            }
        });

        Button btnViewLog = (Button) findViewById(R.id.btnViewLog);
        btnViewLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logActivity();
            }
        });

        /*TextView txtLog = (TextView) findViewById(R.id.txtViewLog);
        txtLog.setText(finalLog1);*/

        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    };

    public void loadBluetoothActivity() {
        Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
        startActivity(intent);
        this.finish();
    }

    public void loadAlarmActivity() {
        AlarmReceiver.stopRingtone();
    }

    public void logActivity() {
        Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
        startActivity(intent);
        this.finish();
    }

    public void OnToggleClicked (View view) {

        long time;
        if (((ToggleButton) view).isChecked()) {

            Toast.makeText(MainActivity.this, "ALARM ON", Toast.LENGTH_SHORT).show();
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, alarmTimePicker.getCurrentHour());
            calendar.set(Calendar.MINUTE, alarmTimePicker.getCurrentMinute());

            Intent intent = new Intent(this, AlarmReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

            time = (calendar.getTimeInMillis() - (calendar.getTimeInMillis() % 60000));
            if (System.currentTimeMillis() > time) {
                if (calendar.AM_PM == 0)
                    time = time + (1000 * 60 * 60 * 12);
                else
                    time = time + (1000 * 60 * 60 * 24);
            }
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time, 60000, pendingIntent);
        } else {
            alarmManager.cancel(pendingIntent);
            Toast.makeText(MainActivity.this, "ALARM OFF", Toast.LENGTH_SHORT).show();
        }
    }

    /*public static void SensorLog() {

        int[] Array = {1, 0};
        String detector = "Motion Detected";

        String finalLog1 = Array[0] + detector;
        String finalLog2 = Array[1] + detector;

    }*/

}

