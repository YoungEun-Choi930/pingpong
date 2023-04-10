package com.example.pingpong.Game;

import static com.google.android.ads.mediationtestsuite.utils.DataStore.getContext;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import com.example.pingpong.Group.GroupMainActivity;
import com.example.pingpong.LoginActivity;
import com.example.pingpong.R;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ventura.bracketslib.model.ColomnData;
import com.ventura.bracketslib.model.CompetitorData;
import com.ventura.bracketslib.model.MatchData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class GameHomeActivity extends AppCompatActivity {
    GameInfoFragment fragmentInfo;
    GameTableFragment fragmentTable;
    boolean isManager = false;
    String groupID, gameID;
    long gameType;
    int curpage = 0;
    boolean first = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_home);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.game_toolbar);
        myToolbar.setTitle(GroupMainActivity.groupName);
        setSupportActionBar(myToolbar);//툴바달기
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        gameID = intent.getStringExtra("gameID");
        groupID = intent.getStringExtra("groupID");
        String manager = intent.getStringExtra("managerUID");
        gameType = intent.getLongExtra("gameType",0);
        String userUID = LoginActivity.userUID;
        isManager = userUID.equals(manager);

        fragmentInfo = new GameInfoFragment(groupID, gameID, isManager);
        fragmentTable = new GameTableFragment(groupID, gameID, gameType,isManager);


        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.add(R.id.game_frame, fragmentInfo);

        transaction.show(fragmentInfo);
        transaction.commit();



        TabLayout tabs = findViewById(R.id.game_tab);
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                if(position == 0){
                    transaction.hide(fragmentTable);
                    transaction.show(fragmentInfo);
                    transaction.commit();
                    fragmentInfo.onResume();

                    curpage = 0;

                }else if (position == 1){
                    if(first) {
                        transaction.add(R.id.game_frame, fragmentTable);
                        first = false;
                    }

                    transaction.hide(fragmentInfo);
                    transaction.show(fragmentTable);
                    transaction.commit();
                    curpage = 1;

                }else if (position == 2){
                    goTree();

                    Objects.requireNonNull(tabs.getTabAt(curpage)).select();

                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: //toolbar의 back키 눌렀을 때 동작
                // 액티비티 이동
                finish();
                break;
            case R.id.menu_setback:

                AlertDialog.Builder alt_bld = new AlertDialog.Builder(GameHomeActivity.this);
                alt_bld.setMessage("생성된 대진표를 삭제하고 게임을 신청단계로 되돌리겠습니까?").setCancelable(false)
                        .setPositiveButton("예", (dialogInterface, i) -> {
                            //db
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            ///////////스케쥴 삭제
                            db.collection("Groups").document(groupID).collection("game").document(gameID)
                                    .collection("schedule").get().addOnSuccessListener(queryDocumentSnapshots -> {
                                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                    if(documentSnapshot.getLong("resultscore1")!=null){

                                        //User 에서 win lose 삭제
                                        long result1 = documentSnapshot.getLong("resultscore1");
                                        long result2 = documentSnapshot.getLong("resultscore2");

                                        String user1UID = documentSnapshot.getString("user1UID");
                                        String user2UID = documentSnapshot.getString("user2UID");
                                        String user3UID, user4UID;
                                        if(result1>result2) {
                                            db.document("Users/" + user1UID).update("win", FieldValue.increment(-1));
                                            db.document("Users/" + user2UID).update("lose", FieldValue.increment(-1));
                                        }
                                        else if(result1<result2) {
                                            db.document("Users/" + user2UID).update("win", FieldValue.increment(-1));
                                            db.document("Users/" + user1UID).update("lose", FieldValue.increment(-1));
                                        }

                                        if (documentSnapshot.getString("user3UID") != null) {
                                            user3UID = documentSnapshot.getString("user3UID");
                                            user4UID = documentSnapshot.getString("user4UID");
                                            if(result1>result2) {
                                                db.document("Users/" + user3UID).update("win", FieldValue.increment(-1));
                                                db.document("Users/" + user4UID).update("lose", FieldValue.increment(-1));
                                            }
                                            else if(result1<result2) {
                                                db.document("Users/" + user4UID).update("win", FieldValue.increment(-1));
                                                db.document("Users/" + user3UID).update("lose", FieldValue.increment(-1));
                                            }
                                        }

                                    }

                                    db.collection("Groups").document(groupID).collection("game").document(gameID)
                                            .collection("schedule").document(documentSnapshot.getId()).delete();
                                }
                            });
                            ///////////리그 삭제
                            db.collection("Groups").document(groupID).collection("game").document(gameID)
                                    .collection("league").get().addOnSuccessListener(queryDocumentSnapshots -> {
                                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                    List<HashMap> infoList = (List) documentSnapshot.get("info");

                                    //User 에서 win lose 삭제
                                    for(HashMap infomap : infoList) {
                                        if(infomap.containsKey("userUID")){
                                            String userUID = (String) infomap.get("userUID");

                                            if(infomap.containsKey("win")) {
                                                long win = (long) infomap.get("win");
                                                if(win != 0) {
                                                    db.document("Users/" + userUID).update("win", FieldValue.increment((-1)*win));
                                                }
                                            }
                                            if(infomap.containsKey("lose")) {
                                                long lose = (long) infomap.get("lose");
                                                if(lose != 0) {
                                                    db.document("Users/" + userUID).update("lose", FieldValue.increment((-1)*lose));
                                                }
                                            }
                                        }else {
                                            String userUID1 = (String)infomap.get("userUID1");
                                            String userUID2 = (String)infomap.get("userUID2");
                                            if(infomap.containsKey("win")) {
                                                long win = (long) infomap.get("win");
                                                if(win != 0) {
                                                    System.out.println("윈점수:"+win);
                                                    db.document("Users/" + userUID1).update("win", FieldValue.increment((-1)*win));
                                                    db.document("Users/" + userUID2).update("win", FieldValue.increment((-1)*win));
                                                }
                                            }
                                            if(infomap.containsKey("lose")) {
                                                long lose = (long) infomap.get("lose");
                                                if(lose != 0) {
                                                    System.out.println(lose+"루즈점수");
                                                    db.document("Users/" + userUID1).update("lose", FieldValue.increment((-1)*lose));
                                                    db.document("Users/" + userUID2).update("lose", FieldValue.increment((-1)*lose));
                                                }
                                            }
                                        }

                                    }

                                    db.collection("Groups").document(groupID).collection("game").document(gameID)
                                            .collection("league").document(documentSnapshot.getId()).delete();
                                }
                            });
                            ///승자, 상태
                            db.collection("Groups").document(groupID).collection("game").document(gameID).update("winnerUID",null);
                            db.collection("Groups").document(groupID).collection("game").document(gameID).update("winnerUID2",null);
                            db.collection("Groups").document(groupID).collection("game").document(gameID).update("state",0);

                            //화면 끝
                            this.finish();
                        }).setNegativeButton("아니오", (dialogInterface, i) -> dialogInterface.cancel());
                AlertDialog alert = alt_bld.create();
                alert.setTitle("게임 되돌리기");
                alert.show();

                //되돌리고 게임프래그먼트로

                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.setback, menu); //툴바에 메뉴 설정
        MenuItem m = menu.findItem(R.id.menu_setback);
        if(isManager) m.setVisible(true);
        return true;

    }

    public void deleteGame(){
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(GameHomeActivity.this);
        builder.setMessage("삭제하시겠습니까?");

        builder.setPositiveButton("확인", (dialogInterface, i) -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();


            ///////////스케쥴 삭제
            db.collection("Groups").document(groupID).collection("game").document(gameID)
                    .collection("schedule").get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    db.collection("Groups").document(groupID).collection("game").document(gameID)
                            .collection("schedule").document(documentSnapshot.getId()).delete();
                }
            });
            ///////////리그 삭제
            db.collection("Groups").document(groupID).collection("game").document(gameID)
                    .collection("league").get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    db.collection("Groups").document(groupID).collection("game").document(gameID)
                            .collection("league").document(documentSnapshot.getId()).delete();
                }
            });
            ///게임 삭제
            db.collection("Groups").document(groupID).collection("game").document(gameID).delete();
            Toast.makeText(GameHomeActivity.this, "삭제 완료",
                    Toast.LENGTH_SHORT).show();
            finish();
        });
        builder.setNegativeButton("취소", (dialogInterface, i) -> { });
        builder.show();
    }


    private void goTree() {
        List<List<MatchData>> matchList = new ArrayList<>();
        String userUID = LoginActivity.userUID;
        HashMap<String, String> nameMap = GameInfoFragment.nameMap;
        System.out.println("----------------------matchList 초기화------------------------------");
        System.out.println("----------------------db에 정보가지러 간다------------------------------");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Groups/"+groupID+"/game/"+gameID+"/schedule").get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                QuerySnapshot query = task.getResult();

                System.out.println("----------------------정보 받아왔다------------------------------");
                System.out.println("------------document 갯수: -------" + query.size());
                String nextmatch = "";

                for (DocumentSnapshot document : query.getDocuments()) {
                    String round = document.getId();       //round 앞의 번호 추출. -1해줌
                    round = ((String[]) round.split("-"))[0];
                    int r = Integer.parseInt(round);
                    r = r - 1;

                    String user1UID = document.getString("user1UID");
                    String user2UID = document.getString("user2UID");
                    String user3UID = document.getString("user3UID");
                    String user4UID = document.getString("user4UID");
                    if (user1UID == null) user1UID = "";
                    if (user2UID == null) user2UID = "";
                    if (user3UID == null) user3UID = "";
                    if (user4UID == null) user4UID = "";


                    String result1 = "결과없음";
                    String result2 = "결과없음";
                    if (document.getLong("resultscore1") != null) {
                        long resultscore1 = document.getLong("resultscore1");
                        long resultscore2 = document.getLong("resultscore2");

                        result1 = Long.toString(resultscore1);
                        result2 = Long.toString(resultscore2);
                    }

                    // 이름 넣기
                    String name1, name2;
                    if (user1UID.equals("none")) name1 = "none";
                    else name1 = nameMap.get(user1UID);
                    if (!user3UID.equals("")) name1 += "," + nameMap.get(user3UID);

                    if (user2UID.equals("부전승")) {
                        name2 = "부전승";
                    } else if (user2UID.equals("none")) name2 = "none";
                    else {
                        name2 = nameMap.get(user2UID);
                        if (!user4UID.equals(""))
                            name2 += "," + nameMap.get(user4UID);
                    }


                    // 다음 대전 상대 찾기
                    if(userUID.equals(user1UID) || userUID.equals(user3UID)) {
                        if(result1.equals("결과없음") && !user2UID.equals("부전승"))
                            nextmatch = "당신의 다음 대결 상대는 "+ name2+" 입니다.";
                    }
                    else if(userUID.equals(user2UID) || userUID.equals(user4UID)) {
                        if(result1.equals("결과없음") && !user2UID.equals("부전승"))
                            nextmatch = "당신의 다음 대결 상대는 "+ name1+" 입니다.";
                    }

                    System.out.println(name1 + "이름1");
                    CompetitorData c1 = new CompetitorData(name1, result1);
                    CompetitorData c2 = new CompetitorData(name2, result2);
                    MatchData m1 = new MatchData(c1, c2);

                    List<MatchData> list;
                    if (matchList.size() == r) {
                        list = new ArrayList<>();
                        list.add(m1);
                        matchList.add(list);
                    } else {
                        list = matchList.get(r);
                        list.add(m1);
                        matchList.set(r, list);
                    }
                    System.out.println("-------------------matchList ++ --------------------");
                    System.out.println(r + "-" + m1);

                }
                List<ColomnData> cList = new ArrayList<>();

                System.out.println("-------------------------------for end --------------------------");
                int matchLength = matchList.size();
                System.out.println("----matchList size: " + matchLength);
                for (int i = 0; i < matchLength; i++) {
                    List<MatchData> mlist = matchList.get(i);
                    ColomnData c = new ColomnData(mlist);
                    cList.add(c);
                }

                System.out.println("-------------------intent--------------------------------");
                Intent treeintent = new Intent(getApplicationContext(), GameTreeActivity.class);
                treeintent.putExtra("list", (Serializable) cList);
                treeintent.putExtra("nextmatch", nextmatch);
                startActivity(treeintent);

            }
        });

    }
}