package com.example.alarmapp4;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        //mainactivity에서 notificationId와 text를 가져오기.
        int notificationId = intent.getIntExtra("notificationId",0);
        String text = intent.getStringExtra("todo");

        NotificationManager notificationManager =(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(context, MainActivity.class);

        //FLAG_ACTIVITY_CLEAR_TOP은 호출하는 activity가 스택에 있을 경우, 해당 activity를 최상위로 올리면서 그 위에 있던 activity들을 모두 삭제하는 flag
        //FALG_ACTIVITY_SINGLE_TOP은 호출되는 activity가 최상위에있을때 해당 activity를 다시 생성하지 않고 있던 activity를 사용한다.
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        //notificationId값을 pendingIntent에 넣어주기.
        PendingIntent pending1 = PendingIntent.getActivity(context,notificationId,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"default");

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.P) {
            builder.setSmallIcon(R.drawable.ic_drug_icon);

            String channelName = "약 복용 알람 채널";
            String description = "매일 정해진 시간에 알림합니다.";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("default", channelName, importance);
            channel.setDescription(description);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
            else
            builder.setSmallIcon(R.mipmap.ic_launcher);

            builder.setAutoCancel(true)
                    .setWhen(System.currentTimeMillis())
                    //notification이 발생하면 진동이 울리게
                    .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                    .setContentTitle("약쏙")
                    //mainactivity에서 가져온 text를 넣기.
                    .setContentText(text + "을(를) 복용해주세요:)")
                    .setContentInfo("INFO")
                    .setContentIntent(pending1);



            if(notificationManager !=null){
                notificationManager.notify(notificationId,builder.build());

                Calendar nextNotifyTime = Calendar.getInstance();

                nextNotifyTime.add(Calendar.DATE,1);

                SharedPreferences.Editor editor = context.getSharedPreferences("drug alarm",Context.MODE_PRIVATE).edit();
                editor.putLong("nextNotifyTime",nextNotifyTime.getTimeInMillis());
                editor.apply();

                //다음 이시간에 알림도 자동으로 설정
                Date currentDateTime = nextNotifyTime.getTime();
                String date_next = new SimpleDateFormat("yyyy년 MM월 dd일 EE요일 hh시 mm분", Locale.getDefault()).format(currentDateTime);
                Toast.makeText(context.getApplicationContext(),"다음 알림은"+date_next+"으로 설정되었습니다.", Toast.LENGTH_SHORT).show();

            }


        }


    }

