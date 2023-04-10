package com.example.pingpong.Group;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.pingpong.LoginActivity;
import com.example.pingpong.R;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MakeNewJoinActivity extends AppCompatActivity {
    EditText gameTitle, gameComment, gameDate, gameTime, gameTimeLimit, gameDateLimit, gamePeopleLimit;
    private String groupID, gameID;
    boolean modify;

    public static Activity SignActicity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_new_join);

        Toolbar myToolbar = findViewById(R.id.creategame_toolbar);
        myToolbar.setTitle("참가신청서");
        setSupportActionBar(myToolbar);//툴바달기
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        gameTitle = findViewById(R.id.create_game_title);
        gameComment = findViewById(R.id.create_game_comment);
        gameDate = findViewById(R.id.create_game_date);
        gameTime = findViewById(R.id.create_game_time);
        gameTimeLimit = findViewById(R.id.create_game_timelimit);
        gameDateLimit = findViewById(R.id.create_game_datelimit);
        gamePeopleLimit = findViewById(R.id.create_game_people_limit);

        Intent intent = getIntent();
        groupID = intent.getStringExtra("groupID");
        modify = intent.getBooleanExtra("isModify",false);
        if(modify){
            gameID = intent.getStringExtra("gameID");
            String title = intent.getStringExtra("gameName");
            String comment = intent.getStringExtra("gameComment");
            String date = intent.getStringExtra("gameDate");
            String deadLine = intent.getStringExtra("gameDeadLine");
            String peopleLimit = intent.getStringExtra("signList");

            String d = date.substring(8,16);
            d = "20"+d.substring(0,2)+"-"+d.substring(3,5)+"-"+d.substring(6,8);
            String t = date.substring(18);

            gameTitle.setText(title);
            gameComment.setText(comment);
            gameDate.setText(d);
            gameTime.setText(t);

            d = deadLine.substring(8,16);
            d = "20"+d.substring(0,2)+"-"+d.substring(3,5)+"-"+d.substring(6,8);
            t = deadLine.substring(18);

            gameTimeLimit.setText(t);
            gameDateLimit.setText(d);

            d = peopleLimit.substring(7);
            d = d.substring(0, d.length()-1);
            String[] s = d.split("/");
            gamePeopleLimit.setText(s[1]);


        }



        gameDate.setOnClickListener(view -> {
            @SuppressLint("SetTextI18n") DatePickerDialog.OnDateSetListener listener = (datePicker, i, i1, i2) -> {
                String s1, s2;
                if(i1+1 < 10) s1 = "0"+(i1+1);
                else s1 = (i1+1)+"";
                if(i2+1 < 10) s2 = "0"+(i2);
                else s2 = (i2)+"";
                gameDate.setText(i + "-" + s1 + "-" + s2);
            };
            Calendar c = Calendar.getInstance();
            DatePickerDialog pickerDialog = new DatePickerDialog(MakeNewJoinActivity.this, listener, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE));

            pickerDialog.show();
        });
        gameTime.setOnClickListener(view -> {
            @SuppressLint("SetTextI18n") TimePickerDialog.OnTimeSetListener listener = (timePicker, i, i1) -> {
                String s1, s2;
                if(i < 10) s1 = "0"+i;
                else s1 = i+"";
                if(i1+1 < 10) s2 = "0"+i1;
                else s2 = i1+"";
                gameTime.setText(s1 + ":" + s2);
            };
            Calendar c = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(MakeNewJoinActivity.this, listener, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
            timePickerDialog.show();
        });


        gamePeopleLimit.setOnClickListener(view -> {

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

            AlertDialog.Builder builder = new AlertDialog.Builder(MakeNewJoinActivity.this);
            final EditText et = new EditText(getApplicationContext());
            et.setInputType(InputType.TYPE_CLASS_NUMBER);
            builder.setTitle("참가인원 제한");
            builder.setView(R.layout.et_dialog);
            builder.setPositiveButton("확인", (dialogInterface, i) -> {
                        Dialog dialog = (Dialog) dialogInterface;
                        EditText input = (EditText) dialog.findViewById(R.id.et_dia);
                        gamePeopleLimit.setText(input.getText().toString());


                InputMethodManager immhide = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                immhide.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

            });
            builder.setNegativeButton("취소", (dialogInterface, i) -> {

                InputMethodManager immhide = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                immhide.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

            });
            builder.show();

        });
        gameDateLimit.setOnClickListener(view -> {
            @SuppressLint("SetTextI18n") DatePickerDialog.OnDateSetListener listener = (datePicker, i, i1, i2) -> {
                String s1, s2;
                if(i1+1 < 10) s1 = "0"+(i1+1);
                else s1 = (i1+1)+"";
                if(i2+1 < 10) s2 = "0"+(i2);
                else s2 = (i2)+"";
                gameDateLimit.setText(i + "-" + s1 + "-" + s2);
            };
            Calendar c = Calendar.getInstance();
            DatePickerDialog pickerDialog = new DatePickerDialog(MakeNewJoinActivity.this, listener, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE));

            pickerDialog.show();
        });
        gameTimeLimit.setOnClickListener(view -> {
            @SuppressLint("SetTextI18n") TimePickerDialog.OnTimeSetListener listener = (timePicker, i, i1) -> {
                String s1, s2;
                if(i < 10) s1 = "0"+i;
                else s1 = i+"";
                if(i1+1 < 10) s2 = "0"+i1;
                else s2 = i1+"";
                gameTimeLimit.setText(s1 + ":" + s2);

            };
            Calendar c = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(MakeNewJoinActivity.this, listener, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
            timePickerDialog.show();
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.complete, menu); //툴바에 메뉴 설정
        return true;

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_complete:
                String resultTitle = gameTitle.getText().toString();
                String resultComment = gameComment.getText().toString();
                String resultDate = gameDate.getText().toString();
                String resultTime = gameTime.getText().toString();
                String resultDateLimit = gameDateLimit.getText().toString();
                String resultTimeLimit = gameTimeLimit.getText().toString();
                String resultLimit = gamePeopleLimit.getText().toString();

                if (resultTitle.equals("") | resultComment.equals("") | resultDate.equals("") | resultTime.equals("") |
                        resultDateLimit.equals("") | resultTimeLimit.equals("") | resultLimit.equals("")) {

                    Toast myToast = Toast.makeText(getApplicationContext(), "정보를 모두 입력바랍니다.", Toast.LENGTH_SHORT);
                    myToast.show();
                } else {

                    String startDate = gameDate.getText().toString() + " " + gameTime.getText().toString() + ":00";
                    String deadLine = gameDateLimit.getText().toString() + " " + gameTimeLimit.getText().toString() + ":00";

                    if(modify) {
                        ModifyGame(resultTitle, resultComment, resultLimit, startDate, deadLine);
                        SignActicity.finish();
                    }else {
                        CreateGame(resultTitle, resultComment, resultLimit, startDate, deadLine);
                    }
                    this.finish();
                }


            case android.R.id.home:
                finish();
                return true;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);

    }
    private void ModifyGame(String resultTitle, String resultComment, String resultlimit, String startDate, String deadLine) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> game = new HashMap<>();
        game.put("gamename", resultTitle);
        game.put("comment", resultComment);
        game.put("state", 0);
        game.put("limit", resultlimit);
        System.out.println(startDate+"스타트데이트");
        Timestamp startdate = Timestamp.valueOf(startDate);
        game.put("startdate", startdate);
        Timestamp deadline = Timestamp.valueOf(deadLine);
        game.put("deadline", deadline);

        // db - 게임에 등록
        db.collection("Groups/" + groupID + "/game").document(gameID).update(game);

    }

    private void CreateGame(String resultTitle, String resultComment, String resultlimit, String startDate, String deadLine) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> game = new HashMap<>();
        game.put("gamename", resultTitle);
        game.put("comment", resultComment);
        game.put("state", 0);
        game.put("limit", resultlimit);

        Timestamp startdate = Timestamp.valueOf(startDate);
        game.put("startdate", startdate);
        Timestamp deadline = Timestamp.valueOf(deadLine);
        game.put("deadline", deadline);

        com.google.firebase.Timestamp now = com.google.firebase.Timestamp.now();
        Date d = now.toDate();
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        String gameID = startDate +"-"+   df.format(d);
        // db - 게임에 등록
        db.collection("Groups/" + groupID + "/game").document(gameID).set(game);

        // db - 공지에 등록
        Map<String, Object> notice = new HashMap<>();
        // 컨텐츠, 타임, 롸이터, 이즈게임
        String userUID = LoginActivity.userUID;
        notice.put("writerUID", userUID);
        notice.put("isGame", true);
        notice.put("time", com.google.firebase.Timestamp.now());
        notice.put("contents", gameTitle.getText().toString() + " 게임 참가신청서가 생성되었습니다.");
        notice.put("noticeTime", com.google.firebase.Timestamp.now());

        db.collection("Groups/" + groupID + "/notice").document(gameID).set(notice);
        new Thread(() -> {
            JSONObject json = makeFCMJson(gameTitle.getText().toString() + " 게임 참가신청서가 생성되었습니다.", userUID, gameID);
            new FCMMessage().sendJsonToFCM(json);
        }).start();
    }

    private JSONObject makeFCMJson(final String message, String managerUID, String gameID) {
        // FMC 메시지 생성 start
        // FMC 메시지 생성 start
        System.out.println("알림보내기");
        System.out.println(gameID+"<----------- 게임아이디");
        JSONObject root = new JSONObject();
        try {

            JSONObject notification = new JSONObject();
            notification.put("body", message);
            notification.put("title", getString(R.string.app_name));
            notification.put("type", "0");//0이 게임, 1이면 글,댓글
            notification.put("groupID", groupID);
            notification.put("managerUID", managerUID);
            notification.put("gameID", gameID);

            root.put("data", notification);
            root.put("to", "/topics/" + groupID);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  root;
    }


}