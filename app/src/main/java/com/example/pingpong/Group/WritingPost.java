package com.example.pingpong.Group;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.pingpong.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class WritingPost extends AppCompatActivity {
    EditText contents;
    private String groupID;
    private String userUID;
    private String postId;
    CheckBox checkBox;
    Boolean modify;
    Toolbar myToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writing_post);

        Intent intent = getIntent();
        groupID = intent.getStringExtra("groupID");
        userUID = intent.getStringExtra("userUID");

        String manager = intent.getStringExtra("manager");
        if (manager == null) {
            manager = "";
        }
        myToolbar = findViewById(R.id.writing_post_toolbar);
        myToolbar.setTitle(GroupMainActivity.groupName);
        setSupportActionBar(myToolbar);//툴바달기
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        checkBox = findViewById(R.id.writing_post_ck_notice);

        if (manager.equals(userUID)) {
            checkBox.setVisibility(View.VISIBLE);
        }

        contents = findViewById(R.id.writing_post_content);
        contents.requestFocus();
        contents.setSelection(contents.length());

        modify = intent.getBooleanExtra("modify", false);
        if (modify) {
            String text = intent.getStringExtra("text");
            postId = intent.getStringExtra("postID");
            contents.setText(text);
        }


        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
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

                boolean result;


                if (checkBox.isChecked())
                    result = uploadNotice(groupID, userUID);
                else
                    result = uploadPost(groupID, userUID);
                ///////
                if (result) {
                    //키보드 내리기
                    InputMethodManager immhide = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    immhide.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                    String sendText = contents.getText().toString();
                    Intent intentR = new Intent(this, WritingCommentActivity.class);
                    intentR.putExtra("sendText", sendText); //사용자에게 입력받은값 넣기
                    setResult(RESULT_OK, intentR); //결과를 저장
                    finish();//액티비티 종료

                    break;
                } else {
                    Toast.makeText(getApplicationContext(), "내용을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                }

            case android.R.id.home: {
                //키보드 내리기
                InputMethodManager immhide = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                immhide.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                finish();
                return true;
            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);

    }


    public boolean uploadNotice(String groupID, String userUID) {
        if (contents == null)
            return false;
        else {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Timestamp time = Timestamp.now();
            Map<String, Object> post = new HashMap<>();
            post.put("contents", contents.getText().toString());
            post.put("writerUID", userUID);
            post.put("time", time);
            post.put("isGame", false);
            post.put("noticeTime", time);

            Date dtime = time.toDate();
            @SuppressLint("SimpleDateFormat") DateFormat f = new SimpleDateFormat("yyMMddhhmmss");
            String stime = f.format(dtime);

            @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String timestamp = df.format(dtime);
            String noticeID = userUID.substring(0, 8) + stime;

            if (!modify) {//아예 처음부터 공지인 글
                postId = noticeID;
                db.collection("Groups").document(groupID).collection("notice").document(postId)
                        .set(post)
                        .addOnSuccessListener(unused -> {
                        })
                        .addOnFailureListener(thr -> {
                        });
                db.collection("Groups").document(groupID).collection("post").document(postId)
                        .set(post)
                        .addOnSuccessListener(unused -> {
                        })
                        .addOnFailureListener(thr -> {
                        });
                String path = "/Groups/" + groupID + "/post/" + noticeID;
                DocumentReference washingtonRef = db.collection("Users").document(userUID);
                washingtonRef.update("postList", FieldValue.arrayUnion(path));

            }else {//수정해서 공지가 된 글?
                db.collection("Groups").document(groupID).collection("post").document(postId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot document = task.getResult();
                        Timestamp timestamp1 = document.getTimestamp("time");
                        post.put("time",timestamp1);
                        db.collection("Groups").document(groupID).collection("notice").document(postId).set(post);
                    }
                });

                DocumentReference Ref = db.collection("Groups").document(groupID).collection("post").document(postId);
                Ref.update("contents", contents.getText().toString());
                DocumentReference Ref2 = db.collection("Groups").document(groupID).collection("notice").document(postId);
                Ref2.update("contents", contents.getText().toString());
            }
            db.collection("Users").document(userUID).get().addOnCompleteListener(task -> {
                DocumentSnapshot document = task.getResult();
                if (document != null) {
                    String name = (String) document.get("name");
                    String groupname = Objects.requireNonNull(Objects.requireNonNull(getSupportActionBar()).getTitle()).toString();
                    new Thread(() -> {
                        JSONObject json = makeFCMJson(groupname + "그룹에 새로운 공지가 등록되었습니다.", postId, name, timestamp);
                        new FCMMessage().sendJsonToFCM(json);
                    }).start();
                }
            });

            return true;
        }
    }

    public boolean uploadPost(String groupID, String userUID) {
        if (contents == null)
            return false;
        else {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Timestamp time = Timestamp.now();
            Map<String, Object> post = new HashMap<>();
            post.put("contents", contents.getText().toString());
            post.put("writerUID", userUID);
            post.put("time", time);

            Date dtime = time.toDate();
            @SuppressLint("SimpleDateFormat") DateFormat f = new SimpleDateFormat("yyMMddhhmmss");
            String stime = f.format(dtime);
            String postID = userUID.substring(0, 8) + stime;
            String path = "/Groups/" + groupID + "/post/" + postID;
            Map<String, Object> postpath = new HashMap<>();
            postpath.put("path", path);


            if (!modify) {////새로운 게시글을 작성하는 경우

                db.collection("Groups").document(groupID).collection("post").document(postID)
                        .set(post)
                        .addOnSuccessListener(unused -> {
                        })
                        .addOnFailureListener(thr -> {
                        });


                DocumentReference washingtonRef = db.collection("Users").document(userUID);
                washingtonRef.update("postList", FieldValue.arrayUnion(path));

            } else {////게시글을 수정하는 경우
                DocumentReference washingtonRef = db.collection("Groups").document(groupID).collection("post").document(postId);
                washingtonRef.update("contents", contents.getText().toString());
                DocumentReference washingtonRef2 = db.collection("Groups").document(groupID).collection("notice").document(postId);
                washingtonRef2.update("contents", contents.getText().toString());
            }


            return true;
        }

    }

    private JSONObject makeFCMJson(final String message, String noticeID, String name, String timestamp) {
        // FMC 메시지 생성 start
        // FMC 메시지 생성 start
        System.out.println("알림보내기");
        JSONObject root = new JSONObject();
        try {

            JSONObject notification = new JSONObject();
            notification.put("body", message);
            notification.put("title", getString(R.string.app_name));
            notification.put("type", "1");//0이 게임, 1이면 글,댓글
            notification.put("groupID", groupID);
            notification.put("postID", noticeID);
            notification.put("writerUID", userUID);

            notification.put("name", name);
            notification.put("text", contents.getText().toString());

            notification.put("timestamp", timestamp);

            root.put("data", notification);
            root.put("to", "/topics/" + groupID);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return root;
    }




    @Override
    public void onBackPressed() {
        super.onBackPressed();
        InputMethodManager immhide = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        immhide.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

    }
}