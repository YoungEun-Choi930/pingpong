package com.example.pingpong.Group;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.pingpong.Game.MakeNewGameActivity;
import com.example.pingpong.LoginActivity;
import com.example.pingpong.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class GroupSignGameActivity extends AppCompatActivity {
    TextView gameName, gameDate, gameDeadline, gameComment, signList;
    RequestManager requestManager;
    RecyclerView particiRecy;
    Button signbtn, makebtn;
    ImageView help;
    LinearLayout signGame;
    TextView nonetxt;
    GroupGameParticiAdapter adapter;
    List<HashMap<String, Object>> particiList;
    private String userUID, gameID, groupID, manager, strGameName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_sign_game);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.sign_game_toolbar);
        myToolbar.setTitle(GroupMainActivity.groupName);
        setSupportActionBar(myToolbar);//툴바달기
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        requestManager = Glide.with(this);

        Intent intent = getIntent();
        gameID = intent.getStringExtra("gameID");
        groupID = intent.getStringExtra("groupID");
        manager = intent.getStringExtra("managerUID");

        gameName = findViewById(R.id.sign_game_name);
        gameDate = findViewById(R.id.sign_game_date);
        gameDeadline = findViewById(R.id.sign_game_deadline);
        gameComment = findViewById(R.id.sign_game_comment);
        signbtn = findViewById(R.id.sign_game_btn);
        particiRecy = findViewById(R.id.sign_game_recy);
        signList = findViewById(R.id.tv_sign_list);

        signGame = findViewById(R.id.sign_game_view);
        nonetxt = findViewById(R.id.sign_game_none_txt);

        while(true) {
            if(LoginActivity.userUID != null) break;
        }
        userUID = LoginActivity.userUID;

        boolean ismanager = userUID.equals(manager);


        particiList = new ArrayList<>();
        adapter = new GroupGameParticiAdapter(particiList, ismanager, groupID, gameID, false, requestManager);
        particiRecy.setAdapter(adapter);
        makebtn = findViewById(R.id.sign_game_make_btn);
        help = findViewById(R.id.game_partici_help);

        if (ismanager) {
            makebtn.setVisibility(View.VISIBLE);
            help.setVisibility(View.VISIBLE);
            help.setOnClickListener(v -> {
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(v.getContext());
                alt_bld.setMessage("관리자는 참여자의 게임 참가비 지불 여부를 저장할 수 있습니다.").setCancelable(false)
                        .setNegativeButton("확인", (dialogInterface, i) -> dialogInterface.cancel());
                AlertDialog alert = alt_bld.create();
                alert.setTitle("도움말");
                alert.show();
            });
        }

        //신청 버튼 누르면 확인창 띄우기
        signbtn.setOnClickListener(view -> {
            //이미 신청했는지 확인
            HashMap<String, Object> info = new HashMap<>();
            info.put("pay", false);
            info.put("userUID", userUID);

            HashMap<String, Object> info2 = new HashMap<>();
            info2.put("pay", true);
            info2.put("userUID", userUID);

            AlertDialog.Builder alt_bld = new AlertDialog.Builder(view.getContext());
            if (particiList.contains(info) | particiList.contains(info2)) { //신청이면 신청취소
                alt_bld.setMessage("신청을 취소합니다.").setCancelable(false)
                        .setPositiveButton("네", (dialogInterface, i) -> {
                            //db
                            particiList.remove(info);
                            particiList.remove(info2);

                            HashMap<String, Object> map = new HashMap<>();
                            map.put("participants", particiList);

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("Groups/" + groupID + "/game").document(gameID).update(map);
                            upDateParticiList();
                            //화면
                            //adapter.setList(particiList);
                            // adapter.notifyDataSetChanged();
                        }).setNegativeButton("아니오", (dialogInterface, i) -> dialogInterface.cancel());
                AlertDialog alert = alt_bld.create();
                alert.setTitle("참여 신청 취소");
                alert.show();
            } else { //신청 안했으면 신청
                alt_bld.setMessage("신청을 보냅니다.").setCancelable(false)
                        .setPositiveButton("네", (dialogInterface, i) -> {
                            //db에 신청
                            particiList.add(info);

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("Groups").document(groupID).collection("game").document(gameID).get()
                                    .addOnCompleteListener(task -> {
                                        DocumentSnapshot document = task.getResult();
                                        String people = (String) document.getData().get("limit");
                                        if (Integer.parseInt(people) >= particiList.size()) {

                                            HashMap<String, Object> map = new HashMap<>();
                                            map.put("participants", particiList);
                                            db.collection("Groups/" + groupID + "/game").document(gameID).update(map);
                                            upDateParticiList();
                                        } else
                                            Toast.makeText(GroupSignGameActivity.this, "인원이 다 찼습니다.", Toast.LENGTH_SHORT).show();
                                    });

                            //화면에 추가
                            //adapter.setList(particiList);
                            //adapter.notifyItemInserted(particiList.size());
                        }).setNegativeButton("아니오", (dialogInterface, i) -> dialogInterface.cancel());
                AlertDialog alert = alt_bld.create();
                alert.setTitle("참여 신청");
                alert.show();
            }

        });
        upDateParticiList();
        Intent gameIntent = new Intent(this, MakeNewGameActivity.class);

        makebtn.setOnClickListener(view -> {

            gameIntent.putExtra("groupID", groupID);
            gameIntent.putExtra("gameID", gameID);
            gameIntent.putExtra("particiList", (Serializable) particiList);
            startActivity(gameIntent);
            finish();
        });
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    public void upDateParticiList() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        System.out.println(gameID+"게임아이디가져온거");
        db.collection("Groups").document(groupID).collection("game").document(gameID).get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot document = task.getResult();
                    if (!document.exists()) {
                        signGame.setVisibility(View.GONE);
                        nonetxt.setVisibility(View.VISIBLE);
                        return;
                    }
                    long state = document.getLong("state");
                    if (state != 0) {
                        signGame.setVisibility(View.GONE);
                        nonetxt.setText("신청이 마감된 게임입니다.");
                        nonetxt.setVisibility(View.VISIBLE);
                        return;
                    }
                    signGame.setVisibility(View.VISIBLE);
                    nonetxt.setVisibility(View.GONE);

                    strGameName = document.getString("gamename");
                    gameName.setText(strGameName);

                    Timestamp date = (Timestamp) document.getTimestamp("startdate");
                    Date d = Objects.requireNonNull(date).toDate();
                    @SuppressLint("SimpleDateFormat") DateFormat format = new SimpleDateFormat("yy/MM/dd  HH:mm");
                    String strdate = format.format(d);
                    gameDate.setText("게임 시작일: " + strdate);

                    date = (Timestamp) document.getData().get("deadline");
                    d = date.toDate();
                    strdate = format.format(d);
                    if (date.compareTo(Timestamp.now()) <= 0) {
                        signbtn.setEnabled(false);
                    }

                    gameDeadline.setText("신청 마감일: " + strdate);

                    gameComment.setText((String) document.getData().get("comment"));

                    particiList = (List) document.getData().get("participants");
                    if (particiList == null) {
                        particiList = new ArrayList();
                    }
                    adapter.setList(particiList);
                    String people = (String) document.getData().get("limit");
                    if (people == null)
                        people = "0";
                    signList.setText("참여자 목록(" + particiList.size() + "/" + people + ")");
                    adapter.notifyDataSetChanged();

                });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.delete, menu); //툴바에 메뉴 설정
        MenuItem m = menu.findItem(R.id.menu_delete_game);
        MenuItem m2 = menu.findItem(R.id.menu_modify_game);
        if (manager.equals(userUID)){
            m.setVisible(true);
            m2.setVisible(true);

        }

        return true;

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home://뒤로가기 버튼
                finish();
                return true;
            case R.id.menu_delete_game:
                deleteGame();
                break;
            case R.id.menu_modify_game:
                Intent intent = new Intent(this,MakeNewJoinActivity.class);
                intent.putExtra("groupID",groupID);
                intent.putExtra("isModify",true);
                intent.putExtra("gameName",gameName.getText().toString());
                intent.putExtra("gameID",gameID);
                intent.putExtra("gameComment",gameComment.getText().toString());
                intent.putExtra("gameDate",gameDate.getText().toString());
                intent.putExtra("gameDeadLine",gameDeadline.getText().toString());
                intent.putExtra("signList",signList.getText().toString());

                MakeNewJoinActivity.SignActicity = this;
                startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }

    public void deleteGame() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(GroupSignGameActivity.this);
        builder.setMessage("삭제하시겠습니까?");

        builder.setPositiveButton("확인", (dialogInterface, i) -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Groups").document(groupID).collection("game").document(gameID).delete();
            db.collection("Groups").document(groupID).collection("notice").document(gameID).delete();
            Toast.makeText(GroupSignGameActivity.this, "삭제 완료", Toast.LENGTH_SHORT).show();
            finish();
        });
        builder.setNegativeButton("취소", (dialogInterface, i) -> {
        });
        builder.show();
    }


}