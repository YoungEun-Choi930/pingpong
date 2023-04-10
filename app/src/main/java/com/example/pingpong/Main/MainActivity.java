package com.example.pingpong.Main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.pingpong.Game.GameHomeActivity;
import com.example.pingpong.Group.GroupSignGameActivity;
import com.example.pingpong.Group.WritingCommentActivity;
import com.example.pingpong.LoginActivity;
import com.example.pingpong.MyFirebaseMessagingService;
import com.example.pingpong.NotificationFragment;
import com.example.pingpong.R;
import com.example.pingpong.SearchSetting.MainProfileFragment;
import com.example.pingpong.SearchSetting.MainSearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.RemoteMessage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private FragmentManager fragmentManager;
    private MainGroupFragment fragmentGroup;
    private MainSearchFragment fragmentSearch;
    private NotificationFragment fragmentNotice;
    private MainProfileFragment fragmentProfile;
    private FragmentTransaction transaction;
    String userUID;


    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userUID = LoginActivity.userUID;

        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);//툴바달기


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavi);
        bottomNavigationView.setOnItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.action_home:// =group
                    setFrag(0);
                    break;
                case R.id.action_search:
                    setFrag(1);
                    break;
                case R.id.action_notice:
                    setFrag(2);
                    break;
                case R.id.action_profile:
                    setFrag(3);
                    break;
            }
            return true;
        });

        fragmentManager = getSupportFragmentManager();

        fragmentGroup = new MainGroupFragment();
        fragmentSearch = new MainSearchFragment();
        fragmentNotice = new NotificationFragment();
        fragmentProfile = new MainProfileFragment();

        transaction = fragmentManager.beginTransaction();


        int flag = 0;
        if (MyFirebaseMessagingService.con) {
            flag = 2;
            intent();
        }

        setFrag(flag);//첫 프래그먼트 화면을 그룹 프래그먼트로

        if (flag == 2) {
            bottomNavigationView.setSelectedItemId(R.id.action_notice);
        }
    }


    private void setFrag(int n) {
        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        switch (n) {
            case 0:
                transaction.replace(R.id.frameLayout, fragmentGroup);
                transaction.commit();
                break;
            case 1:
                transaction.replace(R.id.frameLayout, fragmentSearch);
                transaction.commit();
                break;
            case 2:
                transaction.replace(R.id.frameLayout, fragmentNotice);
                transaction.commit();
                break;
            case 3:
                transaction.replace(R.id.frameLayout, fragmentProfile);
                transaction.commit();
                break;
        }

    }

    public void intent() {
        RemoteMessage remoteMessage = MyFirebaseMessagingService.message;
        String message = remoteMessage.getData().get("body");
        String type = remoteMessage.getData().get("type");


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
            if (message.contains("공지") || remoteMessage.getData().get("isNotice").equals("true"))
                intent.putExtra("isNotice", true);

            startActivity(intent);
        } else if (type.equals("0")) {    //게임 생성

            Intent intent = new Intent(this, GroupSignGameActivity.class);
            //userUID, gameID, groupID, managerUID
            intent.putExtra("gameID", remoteMessage.getData().get("gameID"));
            intent.putExtra("groupID", remoteMessage.getData().get("groupID"));
            intent.putExtra("managerUID", remoteMessage.getData().get("managerUID"));

            startActivity(intent);

        } else if (type.equals("2")) {   //그룹 신청 수락
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("flag", 2);
            startActivity(intent);


        } else if (type.equals("3")) { //대진표 생성
            Intent intent = new Intent(this, GameHomeActivity.class);
            intent.putExtra("gameID", remoteMessage.getData().get("gameID"));
            Long gameType = Long.parseLong(remoteMessage.getData().get("gameType"));
            intent.putExtra("gameType", gameType);
            intent.putExtra("groupID", remoteMessage.getData().get("groupID"));
            intent.putExtra("managerUID", remoteMessage.getData().get("managerUID"));

            startActivity(intent);

        }
    }


}