package com.example.pingpong.Group;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.pingpong.LoginActivity;
import com.example.pingpong.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class WritingCommentActivity extends AppCompatActivity {
    private String groupID, userUID, postID, writerUID;
    public static String writeCommentGroup = "", writeCommentUser = "";
    CircleImageView profile;
    TextView name, time, text, count;
    RecyclerView recy_comment;
    List commentList, commentIDList;
    HashMap<String, List> replyList;
    CommentAdapter adapter;
    ImageView btn_add_comment, btn_more;
    EditText et_comment;
    boolean isManager, isWriter, isNotice;
    HashMap<String, Object> map;
    RequestManager requestManager;
    private ActivityResultLauncher<Intent> resultLauncher;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writing_comment);

        requestManager = Glide.with(this);
/////////툴바////////////////
        Toolbar myToolbar = findViewById(R.id.main_toolbar);
        myToolbar.setTitle(GroupMainActivity.groupName);
        setSupportActionBar(myToolbar);//툴바달기
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        commentList = new ArrayList();
        commentIDList = new ArrayList();
        replyList = new HashMap<>();
        Intent intent = getIntent();
        groupID = intent.getStringExtra("groupID");
        userUID = LoginActivity.userUID;
        postID = intent.getStringExtra("postID");
        writerUID = intent.getStringExtra("writerUID");
        map = (HashMap<String, Object>) intent.getSerializableExtra("postInfo");
        isNotice = intent.getBooleanExtra("isNotice", false);
        isManager = intent.getBooleanExtra("isManager", false);
        isWriter = writerUID.equals(userUID);

        name = findViewById(R.id.writing_comment_postwriter);
        time = findViewById(R.id.writing_comment_posttime);
        count = findViewById(R.id.writing_comment_count);
        recy_comment = findViewById(R.id.recy_writing_comment);
        profile = findViewById(R.id.writing_comment_postprofile);
        text = findViewById(R.id.writing_comment_posttext);
        btn_add_comment = findViewById(R.id.btn_writing_comment);
        btn_more = findViewById(R.id.writing_comment_btn_more);
        et_comment = findViewById(R.id.et_add_comment);


        //액티비티 콜백 함수
        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>()
                {
                    @Override
                    public void onActivityResult(ActivityResult result)
                    {
                        if (result.getResultCode() == RESULT_OK)
                        {
                            String txt = result.getData().getStringExtra("sendText");
                            text.setText(txt);
                        }
                    }
                });


        SwipeRefreshLayout mSwipeRefreshLayout = findViewById(R.id.comment_swipe);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            if(!isNotice) checkPostExist();
            else checkNoticeExist();
            updateComment();
            final Handler handler = new Handler();
            handler.postDelayed(() -> mSwipeRefreshLayout.setRefreshing(false), 500);
        });


        if (isNotice) {
            System.out.println("노티스체크");
            checkNoticeExist();
        } else {
            System.out.println("포스트체크");
            checkPostExist();
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(writerUID).get().addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();
            assert document != null;
            Uri uri = Uri.parse(document.getString("image"));
            Glide.with(WritingCommentActivity.this).load(uri).into(profile);
        });

        name.setText((String) map.get("name"));
        Timestamp timestamp = ((Timestamp) map.get("timestamp"));
        assert timestamp != null;
        Date dtime = timestamp.toDate();
        @SuppressLint("SimpleDateFormat") DateFormat f = new SimpleDateFormat("yyyy-MM-dd  HH:mm");
        String stime = f.format(dtime);
        time.setText(stime);
        text.setText((String) map.get("text"));
///////////////////댓글리사이클러뷰//////////////////////////
        adapter = new CommentAdapter(commentList, replyList, userUID, groupID, postID,requestManager, false, isManager);
        recy_comment.setAdapter(adapter);


        recy_comment.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recy_comment.addItemDecoration(dividerItemDecoration);
//////////////DB에서 댓글가져오기///////////////////
        updateComment();

//////////////////////댓글작성///////////////

        btn_add_comment.setOnClickListener(view -> {
            ////키보드내리기
            InputMethodManager immhide = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            immhide.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

            if (et_comment.getText().toString().equals("")) return;
            Timestamp time = Timestamp.now();
            Map<String, Object> comment = new HashMap<>();

            comment.put("content", et_comment.getText().toString());
            comment.put("time", time);

            comment.put("writerUID", userUID);

            Date dt = time.toDate();
            @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("yyMMddhhmmss");
            String st = df.format(dt);
            String commentID = userUID.substring(0, 8) + st;
            if (writeCommentGroup.equals("")) {
                comment.put("group", commentID);
                comment.put("type", 0);
            } else {
                comment.put("group", writeCommentGroup);
                comment.put("type", 1);
            }


            db.collection("Groups").document(groupID)
                    .collection("post").document(postID).collection("comment").document(commentID)
                    .set(comment)
                    .addOnSuccessListener(unused -> {
                    })
                    .addOnFailureListener(thr -> {
                    });

            ;//그룹포스트의 코멘트에 추가

            String commentPath = "/Groups/" + groupID + "/post/" + postID + "/comment/" + commentID;
            Map<String, Object> commentpath = new HashMap<>();
            commentpath.put("path", commentPath);

            DocumentReference washingtonRef = db.collection("Users").document(userUID);//유저의 코멘트리스트에 추가
            // String path = "/Groups/"+groupID+"/post/"+postID+"/"+commentID;
            washingtonRef.update("commentList", FieldValue.arrayUnion(commentPath));//????????????????put한거랑 여기랑 다른건데?

            et_comment.setText(null);
            updateComment();


            if (!writerUID.equals(userUID)) {
                db.collection("Users").document(writerUID).get().addOnCompleteListener(task -> {
                    DocumentSnapshot document = task.getResult();
                    String token = (String) document.get("token");
                    String message = "작성한 글에 댓글이 달렸습니다.";
                    new Thread(() -> {
                        JSONObject json = makeFCMJson(token, message); //댓글 달리면 글쓴이한테 알림
                        new FCMMessage().sendJsonToFCM(json);
                    }).start();
                });
            }
            if (!userUID.equals(writeCommentUser)) {
                System.out.println(writeCommentUser + "댓글쓴이uid");
                if (!writeCommentGroup.equals("")) { //대댓글이면 댓글작성자에게 알림
                    db.collection("Users").document(writeCommentUser).get().addOnCompleteListener(task -> {
                        DocumentSnapshot document = task.getResult();
                        String token = (String) document.get("token");
                        String message = "작성한 댓글에 대댓글이 달렸습니다.";
                        new Thread(() -> {
                            JSONObject json = makeFCMJson(token, message); //댓글 달리면 글쓴이한테 알림
                            new FCMMessage().sendJsonToFCM(json);
                        }).start();
                    });
                }
            }


        });
////////////////////////////더보기버튼///////////////////////////////////
        if (!isManager && !isWriter) {
            btn_more.setVisibility(View.GONE);
        }
        btn_more.setOnClickListener(view -> {
            String[] more = new String[0];
            if (isNotice) {
                if (isManager) {
                    if (isWriter)
                        more = new String[]{"게시글 수정", "게시글 삭제"};
                    else
                        more = new String[]{"게시글 삭제"};
                }
            } else {
                if (isManager) {
                    if (isWriter)
                        more = new String[]{"게시글 수정", "게시글 삭제", "공지 등록"};
                    else
                        more = new String[]{"게시글 삭제", "공지 등록"};
                } else {
                    more = new String[]{"게시글 수정", "게시글 삭제"};
                }
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(WritingCommentActivity.this);
            builder.setItems(more, (dialogInterface, i) -> {
                if (isManager && !isWriter)
                    i++;
                switch (i) {

                    case 0: ////수정하는경우
                        Intent intent1 = new Intent(getApplicationContext(), WritingPost.class);
                        intent1.putExtra("text", text.getText().toString());
                        intent1.putExtra("groupID", groupID);
                        intent1.putExtra("postID", postID);
                        intent1.putExtra("userUID", userUID);
                        intent1.putExtra("modify", true);
                        resultLauncher.launch(intent1);

                        break;
                    case 1: ///삭제하는경우
                        new Post().deletePost(groupID, postID, writerUID);
                        InputMethodManager immhide = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                        immhide.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                        Toast.makeText(WritingCommentActivity.this, "게시글 삭제 완료", Toast.LENGTH_SHORT).show();
                        finish();


                        break;
                    case 2:// 공지 등록
                        FirebaseFirestore db1 = FirebaseFirestore.getInstance();
                        Map<String, Object> post = new HashMap<>();

                        post.put("contents", text.getText().toString());
                        post.put("writerUID", writerUID);
                        post.put("time", (Timestamp) map.get("timestamp"));
                        post.put("isGame", false);
                        post.put("noticeTime", Timestamp.now());

                        db1.collection("Groups").document(groupID).collection("notice").document(postID)
                                .set(post)
                                .addOnSuccessListener(unused -> Toast.makeText(getApplicationContext(), "공지로 등록되었습니다.", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(thr -> {
                                });
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        });
    
        adapter.setOnItemClickListener((v, pos) -> {
            ///키보드 올리기
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            int i = adapter.selectedPosition;
            adapter.selectedPosition = pos;
            adapter.notifyItemChanged(pos);
            adapter.notifyItemChanged(i);

            HashMap<String, Object> comment = (HashMap<String, Object>) commentList.get(pos);
            writeCommentGroup = (String) comment.get("group");
            writeCommentUser = (String) comment.get("writerUID");


            LinearLayoutManager linearLayoutManager = (LinearLayoutManager)recy_comment.getLayoutManager();

            if(linearLayoutManager != null)
                linearLayoutManager.scrollToPositionWithOffset(pos, 0);



        });
        ////댓글 길게 클릭 = 댓글삭제
        adapter.setOnItemLongClickListener((v, pos) -> {

            HashMap<String, Object> comment = (HashMap<String, Object>) commentList.get(pos);
            Timestamp time = (Timestamp) comment.get("time");
            Date dtime1 = time.toDate();
            @SuppressLint("SimpleDateFormat") DateFormat f1 = new SimpleDateFormat("yyMMddhhmmss");
            String stime1 = f1.format(dtime1);
            String writerUID = (String) comment.get("writerUID");
            String commentID = writerUID.substring(0, 8) + stime1;

            if (writerUID.equals(userUID) || isManager) {
                AlertDialog.Builder builder = new AlertDialog.Builder(WritingCommentActivity.this);
                builder.setTitle("댓글 삭제");
                builder.setMessage("댓글을 삭제하시겠습니까?");
                builder.setPositiveButton("삭제", (dialog, which) ->
                        db.collection("Groups/" + groupID + "/post/" + postID + "/comment") ////대댓글 포함한 도큐먼트
                                .whereEqualTo("group", commentID).get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                QuerySnapshot query = task.getResult(); //
                                assert query != null;
                                for (DocumentSnapshot document : query.getDocuments()) {
                                    String id = document.getId();//코멘트문서아이디
                                    db.document("Groups/" + groupID + "/post/" + postID + "/comment/" + id).delete();//그룹댓글문서에서삭제

                                    String writerUID1 = (String) document.get("writerUID");///유저유아이디
                                    DocumentReference reference = db.collection("Users").document(writerUID1);
                                    String commentpath = "/Groups/" + groupID + "/post/" + postID + "/comment/" + id;
                                    reference.update("commentList", FieldValue.arrayRemove(commentpath)); ///유저코멘트리스트에서삭제

                                }
                                commentList.remove(pos);
                                adapter.notifyDataSetChanged();
                                updateComment();
                            }
                        }));

                builder.setNegativeButton("취소", null);
                builder.show();
            } else
                Toast.makeText(getApplicationContext(), "본인의 댓글이 아닙니다.", Toast.LENGTH_SHORT).show();

        });


    }


    public void updateComment() {
        commentList = new ArrayList();
        HashMap<String, List> replyList = new HashMap<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Groups").document(groupID).collection("post")
                .document(postID).collection("comment").orderBy("time").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot query = task.getResult(); //

                    assert query != null;
                    count.setText(" 댓글 수 : " + query.size());

                    for (DocumentSnapshot document : query.getDocuments()) {
                        String writer = (String) document.getData().get("writerUID");
                        String contents = (String) document.getData().get("content");
                        Timestamp time = (Timestamp) document.getData().get("time");
                        Long type = (Long) document.getData().get("type");
                        String group = (String) document.getData().get("group");

                        HashMap<String, Object> map = new HashMap<>();
                        map.put("writerUID", writer);
                        map.put("contents", contents);
                        map.put("time", time);
                        map.put("type", type);
                        map.put("group", group);

                        if (replyList.get(group) == null) {  //첫댓글은 댓글에
                            List list = new ArrayList();
                            replyList.put(group, list);
                            commentList.add(map);
                        } else {     //이미 같은 group이 한번 들어갔으니까 대댓글에
                            List list = replyList.get(group);
                            list.add(map);
                            replyList.put(group, list);
                        }
                        System.out.println(map);

                    }
                    adapter.setList(commentList, replyList);
                    adapter.notifyDataSetChanged();

                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        if(writeCommentGroup.equals(""))
            super.onBackPressed();

        else {
            writeCommentGroup = "";
            writeCommentUser = "";

            int i = adapter.selectedPosition;
            adapter.selectedPosition = -1;
            adapter.notifyItemChanged(i);
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {//뒤로가기 버튼
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(et_comment.getWindowToken(), 0);

            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private JSONObject makeFCMJson(String token, final String message) {
        // FMC 메시지 생성 start
        JSONObject root = new JSONObject();
        try {

            JSONObject notification = new JSONObject();
            notification.put("body", message);
            notification.put("title", getString(R.string.app_name));
            notification.put("type", "1");//0이 게임, 1이면 글,댓글
            notification.put("groupID", groupID);
            notification.put("postID", postID);
            notification.put("writerUID", writerUID);
            notification.put("isNotice", isNotice);

            notification.put("name", map.get("name"));
            notification.put("text", map.get("text"));

            Timestamp timestamp = ((Timestamp) map.get("timestamp"));
            System.out.println(timestamp.toString() + "스트링");
            Date dtime = timestamp.toDate();
            @SuppressLint("SimpleDateFormat") DateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String stime = f.format(dtime);
            notification.put("timestamp", stime);


            root.put("data", notification);
            root.put("to", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return root;
    }

    private void checkNoticeExist(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        System.out.println(postID+"r공지포스트아이디-----");
        db.document("Groups/" + groupID + "/notice/" + postID).get().addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();
            if (document != null) {
                if (!document.exists()) {
                    View deleted = findViewById(R.id.Postdeleted);
                    deleted.setVisibility(View.VISIBLE);
                    View commentView = findViewById(R.id.commentView);
                    commentView.setVisibility(View.GONE);
                } else {
                    View isExist = findViewById(R.id.isExist);
                    isExist.setVisibility(View.VISIBLE);
                    View commentView = findViewById(R.id.commentView);
                    commentView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void checkPostExist() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.document("Groups/" + groupID + "/post/" + postID).get().addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();
            if (document != null) {
                if (!document.exists()) {
                    View deleted = findViewById(R.id.Postdeleted);
                    deleted.setVisibility(View.VISIBLE);
                    View commentView = findViewById(R.id.commentView);
                    commentView.setVisibility(View.GONE);
                } else {
                    View isExist = findViewById(R.id.isExist);
                    isExist.setVisibility(View.VISIBLE);
                    View commentView = findViewById(R.id.commentView);
                    commentView.setVisibility(View.VISIBLE);
                    et_comment.requestFocus();
                }
            }
        });

    }



}