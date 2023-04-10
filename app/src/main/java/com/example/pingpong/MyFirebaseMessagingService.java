package com.example.pingpong;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.pingpong.Game.GameHomeActivity;
import com.example.pingpong.Group.GroupSignGameActivity;
import com.example.pingpong.Group.WritingCommentActivity;
import com.example.pingpong.Main.MainActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static boolean con;
    public static RemoteMessage message;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData() != null) {
            sendNotification(remoteMessage);

        }
    }

    private void sendNotification(RemoteMessage remoteMessage) {
        this.con = true;
        this.message = remoteMessage;
        String title = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("body");


        final String CHANNEL_ID = "ChannerID";
        NotificationManager mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final String CHANNEL_NAME = "ChannerName";
            final String CHANNEL_DESCRIPTION = "ChannerDescription";
            final int importance = NotificationManager.IMPORTANCE_HIGH;

            // add in API level 26
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            mChannel.setDescription(CHANNEL_DESCRIPTION);
            mChannel.enableLights(true);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 100, 200});
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            mManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setAutoCancel(true);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.mipmap.ic_main);
        builder.setContentTitle(title);
        builder.setContentText(message);

        System.out.println("------------------> 알림받았어요");
        System.out.println(remoteMessage.getData());

        // 메인화면으로 이동
        Intent intent = new Intent(this, SplashActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT); //1회만 사용되는 펜딩인텐트
        builder.setContentIntent(pendingIntent);

        // db에 저장
        String type = remoteMessage.getData().get("type");
        Timestamp now = Timestamp.now();
        Date dt = now.toDate();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String st = df.format(dt);

        if (type.equals("1")) {
//            "title TEXT NOT NULL, body TEXT NOT NULL, type TEXT NOT NULL, groupID TEXT NOT NULL, " +
//            "postID TEXT, writerUID TEXT, name TEXT, text TEXT, timestamp TEXT, gameID TEXT, managerUID TEXT,time TEXT);";
            saveMessageinSQL("'" + title + "','" + message + "','" + type + "','" +
                    remoteMessage.getData().get("groupID") + "','" + remoteMessage.getData().get("postID") + "','" +
                    remoteMessage.getData().get("writerUID") + "','" + remoteMessage.getData().get("name") + "','" +
                    remoteMessage.getData().get("text") + "','" + remoteMessage.getData().get("timestamp") + "','" +
                    remoteMessage.getData().get("isNotice")+"','','" + st + "'");
        } else if (type.equals("0")) {    //게임 생성
            saveMessageinSQL("'" + title + "','" + message + "','" + type + "','" +
                    remoteMessage.getData().get("groupID") + "','','','','','','" +
                    remoteMessage.getData().get("gameID") + "','" + remoteMessage.getData().get("managerUID") + "','" + st + "'");
        } else if (type.equals("2")) {   //그룹 신청 수락
            saveMessageinSQL("'" + title + "','" + message + "','" + type + "','" +
                    remoteMessage.getData().get("groupID") + "','','','','','','','','" + st + "'");
        } else if(type.equals("3")){ //대진표 생성
            saveMessageinSQL("'" + title + "','" + message + "','" + type + "','" +
                    remoteMessage.getData().get("groupID") + "','"+remoteMessage.getData().get("gameType")+"','','','','','" +
                    remoteMessage.getData().get("gameID") + "','" + remoteMessage.getData().get("managerUID") + "','" + st + "'");
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setContentTitle(title);
            builder.setVibrate(new long[]{500, 500});
        }
        SharedPreferences checked = getApplicationContext().getSharedPreferences(remoteMessage.getData().get("groupID"), Activity.MODE_PRIVATE);
        Boolean switch_state = checked.getBoolean("switch_state", true);
        System.out.println(switch_state + "왜안오니알림");
        if (switch_state) {
            mManager.notify(0, builder.build());
        }

    }

    public void intent() {
        RemoteMessage remoteMessage = message;
        String title = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("body");
        String type = remoteMessage.getData().get("type");

        Timestamp now = Timestamp.now();
        Date dt = now.toDate();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String st = df.format(dt);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "ChannerID");

        if (type.equals("1")) {

            HashMap map = new HashMap();
            map.put("name", remoteMessage.getData().get("name"));
            map.put("text", remoteMessage.getData().get("text"));

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = null;
            try {
                date = formatter.parse(remoteMessage.getData().get("timestamp"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            com.google.firebase.Timestamp stamp = new com.google.firebase.Timestamp(date);

            map.put("timestamp", stamp);

            Intent intent = new Intent(this, WritingCommentActivity.class);
            intent.putExtra("groupID", remoteMessage.getData().get("groupID"));
            intent.putExtra("postID", remoteMessage.getData().get("postID"));
            intent.putExtra("writerUID", remoteMessage.getData().get("writerUID"));
            intent.putExtra("postInfo", map);
            if(message.contains("공지") || remoteMessage.getData().get("isNotice").equals("true"))
                intent.putExtra("isNotice",true);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT); //1회만 사용되는 펜딩인텐트

            builder.setContentIntent(pendingIntent);
//            "title TEXT NOT NULL, body TEXT NOT NULL, type TEXT NOT NULL, groupID TEXT NOT NULL, " +
//            "postID TEXT, writerUID TEXT, name TEXT, text TEXT, timestamp TEXT, gameID TEXT, managerUID TEXT,time TEXT);";
            saveMessageinSQL("'" + title + "','" + message + "','" + type + "','" +
                    remoteMessage.getData().get("groupID") + "','" + remoteMessage.getData().get("postID") + "','" +
                    remoteMessage.getData().get("writerUID") + "','" + remoteMessage.getData().get("name") + "','" +
                    remoteMessage.getData().get("text") + "','" + remoteMessage.getData().get("timestamp") + "','" +
                    remoteMessage.getData().get("isNotice")+"','','" + st + "'");
        } else if (type.equals("0")) {    //게임 생성

            Intent intent = new Intent(this, GroupSignGameActivity.class);
            //userUID, gameID, groupID, managerUID
            intent.putExtra("gameID", remoteMessage.getData().get("gameID"));
            intent.putExtra("groupID", remoteMessage.getData().get("groupID"));
            intent.putExtra("managerUID", remoteMessage.getData().get("managerUID"));

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT); //1회만 사용되는 펜딩인텐트

            builder.setContentIntent(pendingIntent);

            saveMessageinSQL("'" + title + "','" + message + "','" + type + "','" +
                    remoteMessage.getData().get("groupID") + "','','','','','','" +
                    remoteMessage.getData().get("gameID") + "','" + remoteMessage.getData().get("managerUID") + "','" + st + "'");
        } else if (type.equals("2")) {   //그룹 신청 수락
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("flag", 2);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT); //1회만 사용되는 펜딩인텐트
            builder.setContentIntent(pendingIntent);

            saveMessageinSQL("'" + title + "','" + message + "','" + type + "','" +
                    remoteMessage.getData().get("groupID") + "','','','','','','','','" + st + "'");
        } else if(type.equals("3")){ //대진표 생성
            Intent intent = new Intent(this, GameHomeActivity.class);
            intent.putExtra("gameID",remoteMessage.getData().get("gameID"));
            Long gameType = Long.parseLong(remoteMessage.getData().get("gameType"));
            intent.putExtra("gameType",gameType);
            intent.putExtra("groupID",remoteMessage.getData().get("groupID"));
            intent.putExtra("managerUID",remoteMessage.getData().get("managerUID"));

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT); //1회만 사용되는 펜딩인텐트
            builder.setContentIntent(pendingIntent);

            saveMessageinSQL("'" + title + "','" + message + "','" + type + "','" +
                    remoteMessage.getData().get("groupID") + "','"+remoteMessage.getData().get("gameType")+"','','','','','" +
                    remoteMessage.getData().get("gameID") + "','" + remoteMessage.getData().get("managerUID") + "','" + st + "'");

        }
    }


    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        sendTokenToServer(s);

    }

    public void sendTokenToServer(String s) {
        //토큰저장장
        // fcm 토큰 받아오기
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task1 -> {

            // Get new FCM registration token
            String token = task1.getResult();

            // db에 token 저장
            if(LoginActivity.userUID != null) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("Users").document(LoginActivity.userUID).update("token", token);
            }

        });
    }

    private void saveMessageinSQL(String query) {
        SQLiteDB sqLiteDB = new SQLiteDB(getApplicationContext());
        SQLiteDatabase db = sqLiteDB.getWritableDatabase();

        db.execSQL("INSERT INTO Message VALUES(" + query + ");");
    }

}
