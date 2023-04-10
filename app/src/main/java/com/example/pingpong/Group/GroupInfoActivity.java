package com.example.pingpong.Group;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.pingpong.LoginActivity;
import com.example.pingpong.Main.MainGroupFragment;
import com.example.pingpong.R;
import com.example.pingpong.SearchSetting.MainSearchFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class GroupInfoActivity extends AppCompatActivity implements OnMapReadyCallback {
    ImageView image;
    TextView groupName, groupComment, groupOpeningDate, groupMember, groupBName, groupBOwner,
            groupBAddress, groupBPhone, groupBNumber;
    RecyclerView manager;
    Button joinBtn;
    MapView naverMap;
    GroupGameParticiAdapter adapter;
    String groupID;
    double latitude, longitude;

    RequestManager requestManager;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        Intent intent = getIntent();
        HashMap<String, Object> info = (HashMap<String, Object>) intent.getSerializableExtra("groupInfo");
        boolean state = intent.getBooleanExtra("state", false); //false면 search에서 들어옴
        String userUID = LoginActivity.userUID;

        requestManager = Glide.with(this);


        groupID = (String) info.get("groupID");
        String name = (String) info.get("groupName");
        Uri uri = (Uri) info.get("Uri");

        long member = (long) info.get("member");
        String managerUID = (String) info.get("manager");
        long jointype = (long) info.get("type");

        String comment = (String) info.get("comment");


        Timestamp date = (Timestamp) info.get("openingDate");
        Date d = Objects.requireNonNull(date).toDate();
        @SuppressLint("SimpleDateFormat") DateFormat format = new SimpleDateFormat("yyyy년 MM월 dd일");
        String strdate = format.format(d);


        Toolbar myToolbar = findViewById(R.id.group_info_toolbar);
        myToolbar.setTitle(name);
        setSupportActionBar(myToolbar);//툴바달기
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        image = findViewById(R.id.group_info_image);
        groupName = findViewById(R.id.group_info_groupName);
        groupMember = findViewById(R.id.group_info_member);
        manager = findViewById(R.id.group_info_manager);
        joinBtn = findViewById(R.id.group_info_join_btn);

        groupComment = findViewById(R.id.group_info_comment);
        groupComment.setText(comment);
        groupOpeningDate = findViewById(R.id.group_info_openingDate);
        groupOpeningDate.setText(strdate);

        ScrollView scroll = findViewById(R.id.scroll);

        naverMap = findViewById(R.id.naverMap);
        ///맵 터치
        naverMap.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_MOVE: //누르고 움직였을 때
                    scroll.requestDisallowInterceptTouchEvent(true);
                    return false;
                case MotionEvent.ACTION_UP: //누른걸 땠을 때
                    scroll.requestDisallowInterceptTouchEvent(false);
                    return true;
                case MotionEvent.ACTION_DOWN: //처음 눌렸을 때
                    scroll.requestDisallowInterceptTouchEvent(true);
                    return false;
                default:
                    return true;
            }
        });


        if(info.get("buisnessName")!=null){
            String buisnessName = (String) info.get("buisnessName");
            String buisnessOwner = (String) info.get("buisnessOwner");
            String buisnessAddress = (String) info.get("buisnessAddress");
            String buisnessPhonenum = (String) info.get("buisnessPhonenum");
            String buisnessNumber = (String) info.get("buisnessNumber");

            latitude = (double) info.get("latitude");
            longitude = (double) info.get("longitude");

            groupBName = findViewById(R.id.group_info_buisnessName);
            groupBName.setText(buisnessName);
            groupBOwner = findViewById(R.id.group_info_buisnessOwner);
            groupBOwner.setText(buisnessOwner);
            groupBAddress = findViewById(R.id.group_info_buisnessAddress);
            groupBAddress.setText(buisnessAddress);
            groupBPhone = findViewById(R.id.group_info_buisnessPhone);
            groupBPhone.setText(buisnessPhonenum);
            groupBNumber = findViewById(R.id.group_info_buisnessNumber);
            groupBNumber.setText(buisnessNumber);
            naverMap.setVisibility(View.VISIBLE);
            naverMap.getMapAsync(this);

        }


        Glide.with(this).load(uri).into(image);
        groupName.setText(name);
        String txt = "멤버 " + member + "명";
        groupMember.setText(txt);


        //대표자
        List<String> list = new ArrayList();
        list.add(managerUID);
        adapter = new GroupGameParticiAdapter(list, false, groupID, "", true, requestManager);
        adapter.setManager(managerUID);
        manager.setAdapter(adapter);


        if (state) {
            joinBtn.setVisibility(View.GONE);
        }


        //신청 버튼 누르면 확인창 띄우기
        joinBtn.setOnClickListener(view -> {
            if (MainGroupFragment.groupStrList.contains(groupID)) {
                Toast myToast = Toast.makeText(getApplicationContext(), "이미 가입한 그룹입니다.", Toast.LENGTH_SHORT);
                myToast.show();
            } else if (MainSearchFragment.userWaitingList.contains(groupID)) {
                Toast myToast = Toast.makeText(getApplicationContext(), "이미 신청한 그룹입니다.", Toast.LENGTH_SHORT);
                myToast.show();
            } else {
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(view.getContext());
                alt_bld.setMessage("신청을 보냅니다.").setCancelable(false)
                        .setPositiveButton("네", (dialogInterface, i) -> {
                            //db에 신청
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            DocumentReference groupDocument = db.document("Groups/" + groupID);
                            DocumentReference userDocument = db.collection("Users").document(userUID);

                            if (jointype == 0) {
                                groupDocument.update("people", FieldValue.arrayUnion(userUID));
                                groupDocument.update("member", FieldValue.increment(1));
                                userDocument.update("groupList", FieldValue.arrayUnion(groupID));
                                Toast myToast = Toast.makeText(getApplicationContext(),
                                        "가입이 완료되었습니다.", Toast.LENGTH_SHORT);
                                myToast.show();

                                // 관리자에게 알림
                                db.document("Users/"+managerUID).get().addOnCompleteListener(task -> {
                                    DocumentSnapshot document = task.getResult();
                                    String token = document.getString("token");

                                    new Thread(() -> {
                                        JSONObject json = makeFCMJson(token, name+"그룹에 새로운 멤버가 가입하였습니다.", groupID);
                                        new FCMMessage().sendJsonToFCM(json);
                                    }).start();
                                });

                                FirebaseMessaging.getInstance().subscribeToTopic("groupID")
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                Toast.makeText(GroupInfoActivity.this, "구독", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            } else if (jointype == 1) {
                                groupDocument.update("waiting", FieldValue.arrayUnion(userUID));
                                userDocument.update("waitingList", FieldValue.arrayUnion(groupID));
                                Toast myToast = Toast.makeText(getApplicationContext(),
                                        "신청이 완료되었습니다.", Toast.LENGTH_SHORT);
                                myToast.show();

                                // 관리자에게 알림
                                db.document("Users/"+managerUID).get().addOnCompleteListener(task -> {
                                    DocumentSnapshot document = task.getResult();
                                    String token = document.getString("token");

                                    new Thread(() -> {
                                        JSONObject json = makeFCMJson(token, name+"그룹에 가입신청자가 있습니다.", groupID);
                                        new FCMMessage().sendJsonToFCM(json);
                                    }).start();
                                });

//
                                MainSearchFragment.userWaitingList.add(groupID);
                            }
                        }).setNegativeButton("아니오", (dialogInterface, i) -> dialogInterface.cancel());
                AlertDialog alert = alt_bld.create();
                alert.setTitle("그룹 가입");
                alert.show();

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {//toolbar의 back키 눌렀을 때 동작
            // 액티비티 이동
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private JSONObject makeFCMJson(String token, final String message, String groupID) {
        // FMC 메시지 생성 start
        JSONObject root = new JSONObject();
        try {

            JSONObject notification = new JSONObject();
            notification.put("body", message);
            notification.put("title", getString(R.string.app_name));
            notification.put("type", "2");//0이 게임, 1이면 글,댓글
            notification.put("groupID", groupID);

            root.put("data", notification);
            root.put("to", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return root;
    }


    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        Marker marker = new Marker();

        marker.setPosition(new LatLng(latitude, longitude));
        marker.setMap(naverMap);

        CameraPosition cameraPosition = new CameraPosition(new LatLng(latitude, longitude), 16, 0, 0);
        naverMap.setCameraPosition(cameraPosition);
    }
}