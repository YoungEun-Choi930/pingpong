package com.example.pingpong.SearchSetting;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.pingpong.LoginActivity;
import com.example.pingpong.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainProfileFragment extends Fragment implements View.OnClickListener {
    TextView changeProfile, myWriting, myApplication, name, email, degree, history;
    ImageView image;
    Button btn_logout;
    RequestManager requestManager;
    public static String userName, userDegree, userGender, userTeam;
    public String userEmail;
    public static Uri userImage;

    boolean isDone1 = false, isDone2 = false, isDone3 = false, isDone4 = false, isDone5 = false;
    List<HashMap> groupList;

    private MainProfileFragment.ProgressDialog progress;


    public MainProfileFragment() {
        String userUID = LoginActivity.userUID;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(userUID).get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    userName = document.getString("name");
                    userEmail = document.getString("email");
                    userDegree = document.getString("degree");
                    userGender = document.getString("gender");
                    userTeam = document.getString("team");
                    userImage = Uri.parse(document.getString("image"));

                });

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestManager = Glide.with(this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_profile, container, false);

        name = view.findViewById(R.id.profile_name);
        name.setText(userName);
        email = view.findViewById(R.id.profile_email);
        email.setText(userEmail);
        degree = view.findViewById(R.id.profile_degree);
        degree.setText(userDegree);

        changeProfile = view.findViewById(R.id.btn_change_profile);
        changeProfile.setOnClickListener(this);
        myWriting = view.findViewById(R.id.btn_my_writing);
        myWriting.setOnClickListener(this);
        myApplication = view.findViewById(R.id.btn_my_application);
        myApplication.setOnClickListener(this);
        btn_logout = view.findViewById(R.id.profile_btn_logout);
        btn_logout.setOnClickListener(this);
        history = view.findViewById(R.id.btn_my_history);
        history.setOnClickListener(this);
        //사진
        image = view.findViewById(R.id.profile_image);
        requestManager.load(userImage).into(image);

        return view;
    }

    private void signOut() {
        GoogleSignInClient googleSignInClient;
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();
        googleSignInClient = GoogleSignIn.getClient(this.getActivity().getBaseContext(), gso);
        googleSignInClient.signOut().addOnCompleteListener(this.getActivity(), task -> {
            FirebaseAuth.getInstance().signOut();
            Intent setupIntent = new Intent(getActivity().getBaseContext(), LoginActivity.class);
            setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(setupIntent);
            getActivity().finish();
        });

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.profile_btn_logout:
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(view.getContext());
                alt_bld.setMessage("로그아웃 하시겠습니까?").setCancelable(false)
                        .setPositiveButton("네", (dialogInterface, i) -> signOut())
                        .setNegativeButton("아니오", (dialogInterface, i) -> dialogInterface.cancel());
                AlertDialog alert = alt_bld.create();
                alert.setTitle("로그아웃");
                alert.show();
                break;
            case R.id.btn_change_profile:
                Intent profileintent = new Intent(getActivity(), MainProfileChangeActivity.class);
                profileintent.putExtra("userName", userName);
                profileintent.putExtra("userGender", userGender);
                profileintent.putExtra("userDegree", userDegree);
                profileintent.putExtra("userImage", userImage);
                profileintent.putExtra("userTeam", userTeam);

                startActivity(profileintent);
                break;
            case R.id.btn_my_writing:
                Intent writingintent = new Intent(getActivity(), MyWritingActivity.class);
                writingintent.putExtra("userName", userName);
                writingintent.putExtra("userImage", userImage);
                startActivity(writingintent);
                break;
            case R.id.btn_my_application:
                Intent applicationIntent = new Intent(getActivity(), MyApplicationActivity.class);
                startActivity(applicationIntent);
                break;
            case R.id.btn_my_history:

                isDone1 = false;
                isDone2 = false;
                isDone3 = false;
                isDone4 = false;
                isDone5 = false;


                //로딩창       -- 중복해서 클릭하는거 막아야하니까..? 일단 예전에 썻었던 다이얼로그 빼겨옴
                progress = new MainProfileFragment.ProgressDialog(getContext());
                progress.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));  //배경 투명
                progress.show();

                goToHistory();
        }
    }

    @Override
    public void onResume() {
        // 내정보 수정하고난담에 왔을때 리줌해야해
        super.onResume();
        name.setText(userName);
        degree.setText(userDegree);
        requestManager.load(userImage).into(image);

    }


    static class ProgressDialog extends Dialog {
        public ProgressDialog(Context context)
        {
            super(context);
            requestWindowFeature(Window.FEATURE_NO_TITLE);// 다이얼 로그 제목을 안보이게...
            setContentView(R.layout.dialog_progress);
            setCanceledOnTouchOutside(false);   //주변터치방지
            setCancelable(false);   // 뒤로가기 방지
        }
    }

    private void goToHistory() {


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ArrayList<HashMap> pList = new ArrayList<>();
        HashMap<String, Object> map = new HashMap();
        map.put("userUID", LoginActivity.userUID);
        map.put("pay", false);
        pList.add(map);
        map = new HashMap();
        map.put("userUID", LoginActivity.userUID);
        map.put("pay", true);
        pList.add(map);

        String userUID = LoginActivity.userUID;


        List<HashMap> resultscheduleList = new ArrayList<>();
        List<HashMap> resultleagueList = new ArrayList<>();
        groupList = new ArrayList<>();
        HashMap groupMap = new HashMap();
        groupMap.put("groupName","그룹");
        groupMap.put("groupID","전체");
        groupList.add(groupMap);
        new Thread(() -> {
            while (true) {
                System.out.println(isDone1 + ",,,,,,," + isDone2 + ",,,,,,," + isDone3 + ",,,,,,," + isDone4 + ",,,,,,," + isDone5);
                if (isDone1 && isDone2 && isDone3 && isDone4 && isDone5) {
                    System.out.println(isDone1 + ",,,,,,," + isDone2 + ",,,,,,," + isDone3 + ",,,,,,," + isDone4 + ",,,,,,," + isDone5);
                    break;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("와일 끝");
            progress.dismiss();     //로딩창 끝


            //인텐트
            Intent intent = new Intent(getActivity(), MyHistoryActivity.class);
            intent.putExtra("scheduleList", (Serializable) resultscheduleList);
            intent.putExtra("leagueList", (Serializable) resultleagueList);
            intent.putExtra("userImage", userImage);
            intent.putExtra("userName", userName);
            intent.putExtra("groupList", (Serializable) groupList);
            startActivity(intent);
        }).start();



        // 내가 속한 게임 들고옴
        db.collectionGroup("Groups").getFirestore().collectionGroup("game")
                .whereArrayContainsAny("participants", pList).get().addOnCompleteListener(task -> {
            QuerySnapshot query = task.getResult();
            int gameNum = query.size();
            HashMap<String, HashMap<String, Object>> infoMap = new HashMap();

            System.out.println("내가 속한 게임 들고옴````````````````````````" + gameNum);

            if(gameNum == 0) {
                isDone1 = true;
                isDone2 = true;
                isDone3 = true;
                isDone4 = true;
                isDone5 = true;
            }

            //info map에 id를 key로 저장
            for (int n = 0;n<query.size();n++) {
                HashMap<String, Object> info = new HashMap();
                DocumentSnapshot document = query.getDocuments().get(n);

                String grouppath = document.getReference().getPath();
                String groupID = grouppath.split("/")[1];

                db.document("Groups/"+groupID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        HashMap hashMap = new HashMap();
                        hashMap.put("groupID",groupID);
                        hashMap.put("groupName",documentSnapshot.getString("name"));
                        if(!groupList.contains(hashMap))
                            groupList.add(hashMap);

                    }
                });


                String gameName = document.getString("gamename");
                Timestamp gameDate = document.getTimestamp("startdate");
                Long gameType = document.getLong("gameType");
                String gameid = document.getId();
                info.put("gameName", gameName);
                info.put("gameDate", gameDate);
                info.put("gameType", gameType);
                infoMap.put(gameid, info);
                System.out.println(gameid + " map에 저장해 놓음");
                CollectionReference d = document.getReference().collection("league"); //리그 있는지 확인

                d.get().addOnCompleteListener(task12 -> {
                    System.out.println("리그 결과 들고옴````````````````````````");

                    if (!task12.getResult().isEmpty()) { //있으면
                        QuerySnapshot query12 = task12.getResult();

                        for (DocumentSnapshot documentSnapshot : query12.getDocuments()) { //몇조에 들어있는지
                            List<HashMap<String, Object>> list = (List<HashMap<String, Object>>) documentSnapshot.get("info");
                            String path = documentSnapshot.getReference().getPath();
                            String p[] = path.split("/");
                            String id = p[3];  //Groups/groupID/game/gameID
                            boolean iscon = false;
                            System.out.print(path + " infoMap에서 가져오려함legue");

                            for (int i = 0; i < list.size(); i++) {
                                if((list.get(i).containsKey("userUID"))){
                                    if ((list.get(i).get("userUID")).equals(userUID)) {
                                        iscon = true;
                                        break;
                                    }
                                }else {
                                    if ((list.get(i).get("userUID1")).equals(userUID) ||(list.get(i).get("userUID2")).equals(userUID)) {
                                        iscon = true;
                                        break;
                                    }
                                }

                            }

                            if (iscon) {
                                List<HashMap<String, Object>> gameList = (List<HashMap<String, Object>>) documentSnapshot.get("game");
                                for (int i = 0; i < gameList.size(); i++) {
                                    HashMap<String, Object> map1 = gameList.get(i);
                                    String user1UID = (String) map1.get("user1UID");
                                    String user2UID = (String) map1.get("user2UID");
                                    String user3UID = (String) map1.get("user3UID");
                                    String user4UID = (String) map1.get("user4UID");
                                    map1.put("gamescheduleName", map1.get("gameName"));
                                    if ((user1UID.equals(userUID)) || user2UID.equals(userUID)) {
                                        if (map1.get("resultscore1") == null) continue;
                                        HashMap<String, Object> infomap = infoMap.get(id);
                                        map1.put("gameName", infomap.get("gameName"));
                                        map1.put("gameType", infomap.get("gameType"));
                                        map1.put("gameDate", infomap.get("gameDate"));
                                        map1.put("path", path);
                                        resultleagueList.add(map1);
                                    }
                                    if (user3UID != null) {
                                        if (user3UID.equals(userUID) || user4UID.equals(userUID)) {
                                            if (map1.get("resultscore1") == null) continue;
                                            HashMap<String, Object> infomap = infoMap.get(id);
                                            map1.put("gameName", infomap.get("gameName"));
                                            map1.put("gameType", infomap.get("gameType"));
                                            map1.put("gameDate", infomap.get("gameDate"));
                                            map1.put("path", path);
                                            resultleagueList.add(map1);
                                        }
                                    }

                                }
                                break;
                            }
                        }
                    }

                    isDone1 = true;

                });

            }



            // 이미지화질올리기?

            OnCompleteListener scheduleListener = task1 -> {

            };
            DocumentSnapshot document = query.getDocuments().get(0); ////이게 대체 왜 됨...?
            document.getReference().getFirestore().collectionGroup("schedule") //토너먼트
                    .whereEqualTo("user1UID", userUID)
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    QuerySnapshot query1 = (QuerySnapshot) task.getResult();
                    System.out.println("토너먼트 1 들고옴````````````````````````");

                    for (DocumentSnapshot document1 : query1.getDocuments()) {

                        String path = document1.getReference().getPath();
                        String p[] = path.split("/");
                        String id = p[3];  //Groups/groupID/game/gameID

                        HashMap<String, Object> map12 = (HashMap) document1.getData();
                        System.out.print(path + " infoMap에서 가져오려함1");

                        String gamescheduleName = document1.getString("gameName");

                        System.out.println("uid1" + map12);
                        if (map12.get("user2UID").equals("부전승")) continue;
                        if (map12.get("resultscore1") == null) continue;

                        HashMap<String, Object> infomap = infoMap.get(id);
                        map12.put("gameName", infomap.get("gameName"));
                        map12.put("gameType", infomap.get("gameType"));
                        map12.put("gameDate", infomap.get("gameDate"));
                        map12.put("gamescheduleName", gamescheduleName);

                        map12.put("path", path);
                        resultscheduleList.add(map12);

                    }

                    isDone2 = true;

                }
            });
            document.getReference().getFirestore().collectionGroup("schedule") //토너먼트
                    .whereEqualTo("user2UID", userUID)
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    QuerySnapshot query1 = (QuerySnapshot) task.getResult();
                    System.out.println("토너먼트 2 들고옴````````````````````````");

                    for (DocumentSnapshot document1 : query1.getDocuments()) {

                        String path = document1.getReference().getPath();
                        String p[] = path.split("/");
                        String id = p[3];  //Groups/groupID/game/gameID
                        HashMap<String, Object> map12 = (HashMap) document1.getData();
                        System.out.print(path + " infoMap에서 가져오려함2");

                        String gamescheduleName = document1.getString("gameName");

                        System.out.println("uid2" + map12);
                        if (map12.get("user2UID").equals("부전승")) continue;
                        if (map12.get("resultscore1") == null) continue;

                        HashMap<String, Object> infomap = infoMap.get(id);
                        map12.put("gameName", infomap.get("gameName"));
                        map12.put("gameType", infomap.get("gameType"));
                        map12.put("gameDate", infomap.get("gameDate"));
                        map12.put("gamescheduleName", gamescheduleName);

                        map12.put("path", path);
                        resultscheduleList.add(map12);

                    }

                    isDone3 = true;

                }
            });
            document.getReference().getFirestore().collectionGroup("schedule") //토너먼트
                    .whereEqualTo("user3UID", userUID)
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    QuerySnapshot query1 = (QuerySnapshot) task.getResult();
                    System.out.println("토너먼트 3 들고옴````````````````````````");

                    for (DocumentSnapshot document1 : query1.getDocuments()) {

                        String path = document1.getReference().getPath();
                        String p[] = path.split("/");
                        String id = p[3];  //Groups/groupID/game/gameID
                        HashMap<String, Object> map12 = (HashMap) document1.getData();
                        System.out.print(path + " infoMap에서 가져오려함3");

                        String gamescheduleName = document1.getString("gameName");

                        System.out.println("uid3" + map12);
                        if (map12.get("user2UID").equals("부전승")) continue;
                        if (map12.get("resultscore1") == null) continue;

                        HashMap<String, Object> infomap = infoMap.get(id);
                        map12.put("gameName", infomap.get("gameName"));
                        map12.put("gameType", infomap.get("gameType"));
                        map12.put("gameDate", infomap.get("gameDate"));
                        map12.put("gamescheduleName", gamescheduleName);

                        map12.put("path", path);
                        resultscheduleList.add(map12);

                    }

                    isDone4 = true;

                }
            });
            document.getReference().getFirestore().collectionGroup("schedule") //토너먼트
                    .whereEqualTo("user4UID", userUID)
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    QuerySnapshot query1 = (QuerySnapshot) task.getResult();
                    System.out.println("토너먼트 4 들고옴````````````````````````");

                    for (DocumentSnapshot document1 : query1.getDocuments()) {

                        String path = document1.getReference().getPath();
                        String p[] = path.split("/");
                        String id = p[3];  //Groups/groupID/game/gameID
                        HashMap<String, Object> map12 = (HashMap) document1.getData();
                        System.out.print(path + " infoMap에서 가져오려함4");

                        String gamescheduleName = document1.getString("gameName");

                        System.out.println("uid4" + map12);
                        if (map12.get("user2UID").equals("부전승")) continue;
                        if (map12.get("resultscore1") == null) continue;

                        HashMap<String, Object> infomap = infoMap.get(id);
                        map12.put("gameName", infomap.get("gameName"));
                        map12.put("gameType", infomap.get("gameType"));
                        map12.put("gameDate", infomap.get("gameDate"));
                        map12.put("gamescheduleName", gamescheduleName);

                        map12.put("path", path);
                        resultscheduleList.add(map12);

                    }

                    isDone5 = true;

                }
            });

        });
    }
}