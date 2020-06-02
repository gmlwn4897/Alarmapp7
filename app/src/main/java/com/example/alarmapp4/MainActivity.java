package com.example.alarmapp4;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.nio.channels.ConnectionPendingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    //AlarmManager alarmManager;
    private EditText contentText;
    private int notificationId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final TimePicker picker = (TimePicker)findViewById(R.id.timepicker);
        picker.setIs24HourView(false); //오전,오후

        //sharedPreference는 데이터를 파일로 저장을하여 편리하게 관리,사용한다.
        final SharedPreferences sharedPreferences = getSharedPreferences("drug alarm",MODE_PRIVATE);
        //getInMillis는 객체 시간을 1/1000초 단위로 변경하여 반환한다. getInstance메소드로 현재 날짜와 시간의 객체를 얻어온다.
        long millis = sharedPreferences.getLong("nextNotifyTime", Calendar.getInstance().getTimeInMillis());

        Calendar nextNotifyTime = new GregorianCalendar();
        nextNotifyTime.setTimeInMillis(millis);

        //calendar 객체가 저장하고있는 시간을 data형태로 리턴
      /*  Date nextDate = nextNotifyTime.getTime();
        String data_text = new SimpleDateFormat("yyy년 MM월 dd일 EE요일 hh시 mm분", Locale.getDefault()).format(nextDate);
        Toast.makeText(getApplicationContext(), "[처음실행시] 다음 알림은 "+data_text+"으로 알림이 설정되었습니다!",Toast.LENGTH_SHORT).show();

        //이전 설정값으로 timepicker 초기화
        Date currentTime = nextNotifyTime.getTime();
        SimpleDateFormat HourFormat = new SimpleDateFormat("kk", Locale.getDefault());
        SimpleDateFormat MinuteFormat = new SimpleDateFormat("mm", Locale.getDefault());

        int pre_hour = Integer.parseInt(HourFormat.format(currentTime));
        int pre_minute = Integer.parseInt(MinuteFormat.format(currentTime));

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.P){
            picker.setHour(pre_hour);
            picker.setMinute(pre_minute);
        }else{
            picker.setCurrentHour(pre_hour);
            picker.setCurrentMinute(pre_minute);
        }*/


        //알림저장 버튼을 click하면 알림이 실행되게 구현
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour,minute;
                String ap_pm;
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.P){
                    hour=picker.getHour();
                    minute = picker.getMinute();
                }
                else{
                    hour = picker.getCurrentHour();
                    minute = picker.getCurrentMinute();
                }

                //timepicker에서 가져온 시간값을 calendar에 설정해준다.
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND,0);
                calendar.set(Calendar.MILLISECOND,0);

                //만약에 설정한 시간이 이미 지난 시간이라면 내일 그시간으로 설정을 한다.
                if(calendar.before(Calendar.getInstance())){
                    calendar.add(Calendar.DATE,1);
                }

                Date currentDateTime = calendar.getTime();
                String data_text = new SimpleDateFormat("yyyy년 MM월 dd일 EE요일 hh시 mm분", Locale.getDefault()).format(currentDateTime);
                //현재 activity 정보가 담겨있는것,
                Toast.makeText(getApplicationContext(), data_text+"으로 알림이 설정되었습니다.", Toast.LENGTH_SHORT).show();

                SharedPreferences.Editor editor = getSharedPreferences("drug alarm", MODE_PRIVATE).edit();
                editor.putLong("nextNotifyTime",(long)calendar.getTimeInMillis());
                editor.apply();

                diaryNotification(calendar);

            }
        });
        //취소 버튼을 click하면 알림이 취소되게 구현
        Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlarmManager cancelmanager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent alarmIntent = new Intent(MainActivity.this, AlarmReceiver.class);
                //AlarmReceiver에 notificationId를 전달한다.
                alarmIntent.putExtra("notificationId", notificationId);
                alarmIntent.putExtra("todo",contentText.getText().toString());
                //FLAG_UPDATE_CURRENT 는 이미 생성된 pendingintent가 존재하면 해당 intent의 내용을 변경한다.
                PendingIntent pI = PendingIntent.getBroadcast(MainActivity.this,0,alarmIntent,PendingIntent.FLAG_UPDATE_CURRENT);
                cancelmanager.cancel(pI);
                Toast.makeText(getApplicationContext(), "알림이 취소되었습니다. ", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //매일 알림을 해주도록 설정하는것.
    private void diaryNotification(Calendar calendar) {

        contentText = (EditText)findViewById(R.id.editText);

        Boolean dailyNotify = true;

        PackageManager pm = this.getPackageManager();
        ComponentName receiver = new ComponentName(this, DeviceBootReceiver.class);
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        alarmIntent.putExtra("notificationId", notificationId);
        alarmIntent.putExtra("todo",contentText.getText().toString());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0,alarmIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        //알람매니저를 불러온다.
        AlarmManager alarmManager =(AlarmManager)getSystemService(Context.ALARM_SERVICE);


        if(dailyNotify){
            if(alarmManager!=null){
                //set(알람타입(RTC_WAkEUP은 절전모드,슬립모드일때 안드로이드를 깨움), 알람이 실행되는 시간,하루마다 반복하는 주기, 알람이 실행될때 발생하는 pendingintent)
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.P){
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),pendingIntent);
                }
            }
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }

    }

}
