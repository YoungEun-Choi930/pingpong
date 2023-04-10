package com.example.pingpong.Group;

import android.util.Log;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
/*
게시글, 공지글 작성/삭제/알림에 관한 (디비 및 알림) 클래스
*/
public class FCMMessage {
    private static final String TAG = "Post";

    //알림 상수
    private static final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
    private static final String SERVER_KEY = "AAAABx2zXoU:APA91bEQ2jkZkYmqVY_bgTa6n3Ks8lcf-DX7yAJg5sPz2T3pctFLIAUIbLZ-gXZOsztxfxdCuDwJ2n93bLadEnGBkOmRy9MQZPZ_oEfOz_imlO5YliNI6abK4gbv4on38xsrR9KRy2tb";

    public void sendJsonToFCM(JSONObject json) {
        // FMC 메시지 전송 메소드
        try {
            URL Url = new URL(FCM_MESSAGE_URL);
            HttpURLConnection conn = (HttpURLConnection) Url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.addRequestProperty("Authorization", "key=" + SERVER_KEY);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-type", "application/json");
            conn.setRequestMethod("POST");
            OutputStream os = conn.getOutputStream();
            os.write(json.toString().getBytes(StandardCharsets.UTF_8));
            os.flush();
            Log.d(TAG, "sendJsonToFCM - finish");
            Log.v(TAG, "ResponseCode: "+conn.getResponseCode());

        } catch (Exception ioException) {
            Log.e(TAG, "sendJsonToFCM - "+ioException.toString());
        }

    }

}
