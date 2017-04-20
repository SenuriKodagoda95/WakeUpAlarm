package com.example.shakila.wakeupalarmsystem;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

    public static Boolean alarmRunning = false;

    private static Ringtone ringtone = null;

    @Override
    public void onReceive(Context context, Intent intent) {

        CountDownTimer timer = new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                stopRingtone();
            }
        };

        Toast.makeText(context, "Alarm! Wake up! Wake up!", Toast.LENGTH_LONG).show();
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        ringtone = RingtoneManager.getRingtone(context, alarmUri);
        ringtone.play();
        alarmRunning = true;
        timer.start();
    }

    public static void stopRingtone() {
        ringtone.stop();
    }

}