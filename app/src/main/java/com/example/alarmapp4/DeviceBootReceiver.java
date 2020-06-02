package com.example.alarmapp4;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Objects;

public class DeviceBootReceiver extends BroadcastReceiver {

    //재부팅하고나서 알람 울릴 수 있도록 설정.
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //objects.equals가 kitkat 버전에서 사용가능해서 if문으로 버전 설정해주었다.
            if (Objects.equals(intent.getAction(), "android.intent.action.BOOT_COMPLETED")){
                Intent alarmIntent = new Intent(context, AlarmReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0,alarmIntent,PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                SharedPreferences sharedPreferences  =context.getSharedPreferences("drug alarm",Context.MODE_PRIVATE);
                long millis= sharedPreferences.getLong("nextNotifyTime", Calendar.getInstance().getTimeInMillis());

                Calendar current_calendar = Calendar.getInstance();
                Calendar nextNotifyTime = new GregorianCalendar();
                nextNotifyTime.setTimeInMillis(sharedPreferences.getLong("nextNotifyTime", millis));

                if(current_calendar.after(nextNotifyTime)){
                    nextNotifyTime.add(Calendar.DATE,1);
                }

                Date currentDateTime = nextNotifyTime.getTime();
                String date_next = new SimpleDateFormat("yyyy년 MM월 dd일 EE요일 hh시 mm분", Locale.getDefault()).format(currentDateTime);
                Toast.makeText(context.getApplicationContext(),"[재부팅후] 다음 알림은"+date_next+"으로 설정이 되었습니다.",Toast.LENGTH_SHORT).show();

                if(manager!=null){
                    manager.setRepeating(AlarmManager.RTC_WAKEUP, nextNotifyTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
                }
            }
        }
    }
}
