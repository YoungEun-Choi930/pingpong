package com.example.pingpong.SearchSetting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.pingpong.LoginActivity;
import com.example.pingpong.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.ResourceDescriptor;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class MyHistoryActivity extends AppCompatActivity {
    List<HashMap> scheduleList, leagueList, resultList, groupList;
    Uri userImage;
    Spinner groupspinner, teamspinner, typespinner, winspinner;
    TextView totalP, singleP, teamP;
    ArrayAdapter groupspinnerAdapter, teamspinnerAdapter, typespinnerAdapter, winspinnerAdapter;
    String strgroup = "전체", strteam = "전체", strtype = "전체", strwin = "전체";
    HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_history);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        myToolbar.setTitle("게임 전적");
        setSupportActionBar(myToolbar);//툴바달기
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        scheduleList = (List<HashMap>) intent.getSerializableExtra("scheduleList");
        leagueList = (List<HashMap>) intent.getSerializableExtra("leagueList");
        groupList = (List<HashMap>) intent.getSerializableExtra("groupList");

        userImage = intent.getParcelableExtra("userImage");
        String userName = intent.getStringExtra("userName");
        RequestManager requestManager = Glide.with(this);


        System.out.println("--------------history activity -----------------");
        System.out.println("--------------schedule -----------------");

        for (HashMap map : scheduleList) {
            System.out.println(map);
        }
        System.out.println("--------------league -----------------");

        for (HashMap map : leagueList) {
            System.out.println(map);
        }
        resultList = new ArrayList<>();
        resultList.addAll(leagueList);
        resultList.addAll(scheduleList);
        resultList.sort(new HistoryComparator()); //날짜순으로 정렬

        TextView no_result = findViewById(R.id.no_result);
        if (resultList.size() == 0) no_result.setVisibility(View.VISIBLE);

        RecyclerView recyclerView = findViewById(R.id.recy_history);
        adapter = new HistoryAdapter(resultList, userImage, userName, requestManager);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
//-----------------------------------------스피너
        groupspinner = findViewById(R.id.history_group_spinner);
        teamspinner = findViewById(R.id.history_team_spinner);
        winspinner = findViewById(R.id.history_win_spinner);
        typespinner = findViewById(R.id.history_type_spinner);

        //스피너 리스트 만
        String groupName[] = new String[groupList.size()];
        String type[] = new String[]{"게임타입", "리그", "토너먼트"};
        String team[] = new String[]{"단식/복식", "단식", "복식"};
        String winlose[] = new String[]{"승/패", "이긴 게임", "진 게임"};


        for (int i = 0; i < groupList.size(); i++) {
            groupName[i] = (String) groupList.get(i).get("groupName");
        }

/////////////////그룹스피너
        groupspinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, groupName);

        groupspinner.setAdapter(groupspinnerAdapter);
        groupspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                strgroup = (String) groupList.get(i).get("groupID");
                adapter.filteredList = adapter.list;
                adapter.getFilter().filter(strteam + strwin + strgroup);


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        ///////////리그토너먼트
        typespinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, type);
        typespinner.setAdapter(typespinnerAdapter);
        typespinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                strtype = type[i];
                if (strtype.equals("리그")) {
                    adapter = new HistoryAdapter(leagueList, userImage, userName, requestManager);
                } else if (strtype.equals("토너먼트")) {
                    adapter = new HistoryAdapter(scheduleList, userImage, userName, requestManager);
                } else {
                    adapter = new HistoryAdapter(resultList, userImage, userName, requestManager);
                }

                recyclerView.setAdapter(adapter);

                adapter.filteredList = adapter.list;
                adapter.getFilter().filter(strteam + strwin + strgroup);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        ///////////진게임이긴게임
        winspinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, winlose);
        winspinner.setAdapter(winspinnerAdapter);
        winspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                strwin = winlose[i];
                adapter.filteredList = adapter.list;
                adapter.getFilter().filter(strteam + strwin + strgroup);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        ///////////////단식복식
        teamspinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, team);
        teamspinner.setAdapter(teamspinnerAdapter);
        teamspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                strteam = team[i];
                if(i==0) strteam = "전체";
                adapter.filteredList = adapter.list;
                adapter.getFilter().filter(strteam + strwin + strgroup);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
//------------------------------------------------------------스피너 끝

        totalP = findViewById(R.id.total_percent);
        singleP = findViewById(R.id.single_percent);
        teamP = findViewById(R.id.team_percent);
        CalPercent();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.document("Users/" + LoginActivity.userUID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                long win = 0;
                long lose = 0;
                if (document.getLong("win") != null) {
                    win = document.getLong("win");
                    lose = document.getLong("lose");
                }
                double percent;
                if (win == 0 && lose == 0) percent = 0.0;
                else percent = (double) win / (lose + win);
                String strpercent = String.valueOf(percent * 100);
                if (strpercent.length() > 5) strpercent = strpercent.substring(0, 5);
                else if (strpercent.equals("NaN")) strpercent = "0.0";

                totalP.setText(strpercent + "%  (" + win + "승 " + lose + "패)");
            }
        });


        //단식복식, 리그토너먼트 선택도 해야함
        ////걍 필터다이얼로그를 넣을까? ---이것도 괜찮은데??

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


    private void CalPercent() {
        int s_win = 0, s_lose = 0, t_win = 0, t_lose = 0; //s=싱글(단식) t=팀(복식)
        double s_percent, t_percent;
        String userUID = LoginActivity.userUID;
        for (int i = 0; i < resultList.size(); i++) {
            String user1UID = (String) resultList.get(i).get("user1UID");
            String user2UID = (String) resultList.get(i).get("user2UID");
            String user3UID = (String) resultList.get(i).get("user3UID");
            String user4UID = (String) resultList.get(i).get("user4UID");

            long resultscore1 = (long) resultList.get(i).get("resultscore1");
            long resultscore2 = (long) resultList.get(i).get("resultscore2");

            if (user3UID == null) {//단식
                if (user1UID.equals(userUID)) {//내가 유저1
                    if (resultscore1 > resultscore2) s_win++; //이긴 경우
                    else s_lose++; // 진 경우
                } else { //내가 유저2
                    if (resultscore1 < resultscore2) s_win++;//이긴 경우
                    else s_lose++;// 진 경우
                }

            } else {//복식
                if (user1UID.equals(userUID) || user3UID.equals(userUID)) {//내가 유저1 또는 유저3이면
                    if (resultscore1 > resultscore2) t_win++;
                    else t_lose++;
                } else {//내가 유저2 또는 유저4이면
                    if (resultscore1 < resultscore2) t_win++;
                    else t_lose++;
                }
            }
        }
        s_percent = ((double) s_win / (s_win + s_lose)) * 100;
        t_percent = ((double) t_win / (t_win + t_lose)) * 100;
        String str_s_percent = Double.toString(s_percent);
        String str_t_percent = Double.toString(t_percent);
        if (str_s_percent.length() > 5) str_s_percent = str_s_percent.substring(0, 5);
        else if (str_s_percent.equals("NaN")) str_s_percent = "0.0";

        if (str_t_percent.length() > 5) str_t_percent = str_t_percent.substring(0, 5);
        else if (str_t_percent.equals("NaN")) str_t_percent = "0.0";

        singleP.setText(str_s_percent + "%  (" + s_win + "승 " + s_lose + "패)");
        teamP.setText(str_t_percent + "%  (" + t_win + "승 " + t_lose + "패)");

    }

    //날짜 순으로 정렬 - 지금은 최근께 위로감
    class HistoryComparator implements Comparator<HashMap> {
        @Override
        public int compare(HashMap h1, HashMap h2) {
            Timestamp t1 = (Timestamp) h1.get("gameDate");
            Timestamp t2 = (Timestamp) h2.get("gameDate");

            int result = t1.compareTo(t2);
            if (result < 0) return 1;
            else if (result > 0) return -1;
            return 0;
        }
    }
}