package com.example.pingpong.Game;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.pingpong.LoginActivity;
import com.example.pingpong.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MakeNewGameActivity extends AppCompatActivity implements View.OnClickListener {
    int type;
    RadioGroup styleRadio, typeRadio, typeTeam;
    LinearLayout recybtn, pnum, team;
    Button next, pay, all, pnumM, pnumP;
    TextView help, pnumhelp, pnumT, noresult;
    Spinner teamspinner;
    RecyclerView partici, teampartici;
    String strResultRadio, groupID, gameID;
    MakeGameSelectPeopleAdapter particiAdapter, teamparticiAdapter, teamAllAdapter;
    RequestManager requestManager;
    int resultType1, resultType2 = 1, resultType3 = 0, resultType5 = 1, teamNum = 1;

    List<HashMap<String, Object>> particiList, selectedList;
    List<HashMap<String, Object>>[] teamParticiList, teamItemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_new_game);

        Intent intent = getIntent();
        groupID = intent.getStringExtra("groupID");
        gameID = intent.getStringExtra("gameID");
        particiList = (List<HashMap<String, Object>>) intent.getSerializableExtra("particiList");

        Toolbar myToolbar = (Toolbar) findViewById(R.id.makegame_toolbar);
        myToolbar.setTitle("게임 생성하기");
        setSupportActionBar(myToolbar);//툴바달기
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        requestManager = Glide.with(this);
        help = findViewById(R.id.makegame_help);

        // 첫화면  개인, 단체
        styleRadio = findViewById(R.id.makegame_style);

        // 두번째 화면 토너먼트, 리그+토너먼트
        typeRadio = findViewById(R.id.makegame_type);
        typeRadio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedID) {
                if (checkedID == R.id.makegame_type1) {
                    resultType2 = 1;   // 토너먼트:1 , 리그+토너먼트:2
                    resultType3 = 0;
                    type2();
                } else {    //2
                    resultType2 = 2;
                    type3();
                }
            }
        });

        // 세번째 화면 리그+토너먼트를 선택한 경우, 한 조당 인원(pnum)선택
        pnum = findViewById(R.id.makegame_pnum);
        pnumhelp = findViewById(R.id.makegame_pnum_help);
        pnumT = findViewById(R.id.makegame_pnum_text);
        pnumM = findViewById(R.id.makegame_pnum_mbtn);
        pnumM.setOnClickListener(this);
        pnumP = findViewById(R.id.makegame_pnum_pbtn);
        pnumP.setOnClickListener(this);

        // 네번째 화면 참가자 선택
        partici = findViewById(R.id.makegame_recy);
        particiAdapter = new MakeGameSelectPeopleAdapter(particiList, requestManager, false);
        partici.setAdapter(particiAdapter);
        recybtn = findViewById(R.id.makegame_recybtn);
        pay = findViewById(R.id.makegame_pay);
        pay.setOnClickListener(this);
        all = findViewById(R.id.makegame_all);
        all.setOnClickListener(this);

        // 다섯번째 화면 조편성
        typeTeam = findViewById(R.id.team_type);
        typeTeam.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedID) {
                if (checkedID == R.id.team_type1) {
                    resultType5 = 1;   // 자동:1 , 수동:2
                    next.setText("완료");
                } else {    //2
                    resultType5 = 2;
                    type6();
                }
            }
        });
        team = findViewById(R.id.team_select);
        teampartici = findViewById(R.id.team_recy);
        teamparticiAdapter = new MakeGameSelectPeopleAdapter(new ArrayList<>(), requestManager, true);
        teampartici.setAdapter(teamparticiAdapter);
        teampartici.setLayoutManager(new LinearLayoutManager(this));
        teamparticiAdapter.setOnItemClickListener((v, pos) -> {


            HashMap<String, Object> item = teamparticiAdapter.particiList.remove(pos);
            teamparticiAdapter.notifyItemRemoved(pos);

            HashMap<String, Object> info = new HashMap<>();
            info.put("userUID", item.get("userUID"));
            info.put("degree", item.get("degree"));
            teamParticiList[teamNum - 1].remove(info);
            teamItemList[teamNum - 1].remove(item);

            if (teamItemList[teamNum - 1].size() == 0)
                noresult.setVisibility(View.VISIBLE);
            else
                noresult.setVisibility(View.GONE);


            teamAllAdapter.particiList.add(item);
            teamAllAdapter.notifyItemInserted(teamAllAdapter.particiList.size() - 1);


            System.out.println("partici list-------------------");
            System.out.println(particiAdapter.particiList);
        });
        noresult = findViewById(R.id.no_result);
        teamAllAdapter = new MakeGameSelectPeopleAdapter(new ArrayList<>(), requestManager, true);
        teamAllAdapter.setOnItemClickListener((v, pos) -> {
            HashMap<String, Object> item = teamAllAdapter.particiList.remove(pos);
            teamAllAdapter.notifyItemRemoved(pos);
            noresult.setVisibility(View.GONE);


            System.out.println("teampartici list-------------------");
            System.out.println(teamparticiAdapter.particiList);

            HashMap<String, Object> info = new HashMap<>();
            info.put("userUID", item.get("userUID"));
            info.put("degree", item.get("degree"));
            teamParticiList[teamNum - 1].add(info);
            teamItemList[teamNum - 1].add(item);


            System.out.println("teampartici list-------------------");
            System.out.println(teamparticiAdapter.particiList);

            teamparticiAdapter.particiList.add(item);
            teamparticiAdapter.notifyItemInserted(teamparticiAdapter.particiList.size() - 1);

            System.out.println("teampartici list-------------------");
            System.out.println(teamparticiAdapter.particiList);
        });
        teamspinner = findViewById(R.id.team_spinner);
        teamspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                teamNum = i + 1;
                System.out.println("스피너 셋리스트 " + teamNum);
                teamparticiAdapter.setList(teamItemList[i]);
                if (teamItemList[i].size() == 0)
                    noresult.setVisibility(View.VISIBLE);
                else
                    noresult.setVisibility(View.GONE);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // 다음, 완료 버튼
        next = findViewById(R.id.makegame_next);
        next.setOnClickListener(this);

        //처음들어오면 type1
        type1();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {     //뒤로가기 버튼
            if (type == 1) finish();
            else if (type == 2) type1();
            else if (type == 3) type1();
            else if (type == 4) {
                if (resultType3 == 0) type2();
                else type3();
            } else if (type == 5) type4();
            else if (type == 6) type5();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint({"NonConstantResourceId", "NotifyDataSetChanged", "SetTextI18n"})
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.makegame_pnum_pbtn:   //type3
                int pnum1 = Integer.parseInt(pnumT.getText().toString());
                if (pnum1 < 15) pnumT.setText(Integer.toString(pnum1 + 1));
                break;
            case R.id.makegame_pnum_mbtn:   //type3
                int pnum2 = Integer.parseInt(pnumT.getText().toString());
                if (pnum2 > 1) pnumT.setText(Integer.toString(pnum2 - 1));
                break;
            case R.id.makegame_pay:         //type4
                particiAdapter.setViewType(1);
                particiAdapter.notifyDataSetChanged();
                particiAdapter.setSelectedList(1);
                break;
            case R.id.makegame_all:         //type4
                particiAdapter.setViewType(2);
                particiAdapter.notifyDataSetChanged();
                particiAdapter.setSelectedList(2);
                break;
            case R.id.makegame_next:        //다음, 완료 버튼
                if (type == 1) {
                    //라디오 값 가져옴
                    int radioID = styleRadio.getCheckedRadioButtonId();
                    RadioButton resultRadio = (RadioButton) findViewById(radioID);

                    if (resultRadio == null) {
                        Toast.makeText(getApplicationContext(), "타입을 선택해 주세요.", Toast.LENGTH_SHORT).show();
                    } else {   // 단식:1 , 복식:2
                        strResultRadio = resultRadio.getResources().getResourceName(radioID);
                        strResultRadio = strResultRadio.substring(strResultRadio.length() - 1);
                        resultType1 = Integer.parseInt(strResultRadio);
                        if (resultType3 != 0) type3();
                        else type2();
                    }
                } else if (type == 2) {
                    //라디오 값 가져옴
                    int radioID = typeRadio.getCheckedRadioButtonId();
                    RadioButton resultRadio = (RadioButton) findViewById(radioID);

                    if (resultRadio == null) {
                        Toast.makeText(getApplicationContext(), "타입을 선택해 주세요.", Toast.LENGTH_SHORT).show();
                    } else {
                        type4();
                    }
                } else if (type == 3) {
                    resultType3 = Integer.parseInt(pnumT.getText().toString());
                    List spinnerList = new ArrayList();
                    teamParticiList = new List[resultType3];
                    teamItemList = new List[resultType3];
                    for (int i = 1; i <= resultType3; i++) {
                        spinnerList.add(i + "조");
                        teamParticiList[i - 1] = new ArrayList<>();
                        teamItemList[i - 1] = new ArrayList<>();
                    }
                    SpinnerAdapter roundSpinnerAdapter = new ArrayAdapter(this.getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, spinnerList);
                    teamspinner.setAdapter(roundSpinnerAdapter);


                    type4();
                } else if (type == 4) {
                    // 참여자 선택한거 들고오기
                    selectedList = particiAdapter.getSelectedList();

                    if (resultType1 == 2 & selectedList.size() % 2 == 1) {
                        Toast.makeText(getApplicationContext(), "단체 경기를 위해 참여자를 짝수로 선택해 주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    selectedList.sort(new DegreeComparator());     //부수 순서로 sort

                    System.out.println("1111111111111111111111111111111111111selectedList");
                    System.out.println(selectedList.size());
                    System.out.println(selectedList);


                    if (resultType2 == 1) { //토너먼트
                        //게임만들기
                        boolean result = makeGame(selectedList);
                        if (!result) {
                            Toast.makeText(getApplicationContext(), "참가 인원이 너무 적습니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            System.out.println("디비에다가 스테이트바꾸고 토스트창 띄움");
                            saveGame();
                        }
                    } else if (resultType1 == 2) { //복식+리그
                        //게임만들기
                        boolean result = makeGame(selectedList);
                        if (!result) {
                            Toast.makeText(getApplicationContext(), "참가 인원이 너무 적습니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            System.out.println("디비에다가 스테이트바꾸고 토스트창 띄움");
                            saveGame();
                        }
                    } else {  // 리그 조 편성 해야함
                        teamAllAdapter.setList(selectedList);
                        type5();
                    }

                } else if (type == 5) {
                    int radioID = typeTeam.getCheckedRadioButtonId();
                    RadioButton resultRadio = (RadioButton) findViewById(radioID);

                    if (resultRadio == null) {
                        Toast.makeText(getApplicationContext(), "타입을 선택해 주세요.", Toast.LENGTH_SHORT).show();
                    } else {   // 자동:1 , 수동:2
                        strResultRadio = resultRadio.getResources().getResourceName(radioID);
                        strResultRadio = strResultRadio.substring(strResultRadio.length() - 1);
                        resultType5 = Integer.parseInt(strResultRadio);
                        if (resultType5 == 2) type6();
                        else {  //자동이면
                            //게임만들기

                            boolean result = makeGame(selectedList);
//                    if (!result) {
//                        Toast.makeText(getApplicationContext(), "참가 인원이 너무 적습니다.", Toast.LENGTH_SHORT).show();
//
//                    } else {
                            saveGame();

                        }
                    }
                } else if (type == 6) {

                    if (teamAllAdapter.particiList.size() != 0) {
                        Toast.makeText(MakeNewGameActivity.this, "모든 참여자를 조에 편성해주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    for (int i = 0; i < teamParticiList.length; i++) {
                        if (teamParticiList[i].size() < 2) {
                            Toast.makeText(MakeNewGameActivity.this, (i + 1) + "조에 인원이 부족합니다.\n(2명 이상)", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    makeLeague(selectedList);
                    saveGame();

                }
        }
    }

    private void type1() {  //2->1, 3->1
        type = 1;

        styleRadio.setVisibility(View.VISIBLE);
        typeRadio.setVisibility(View.GONE);
        pnum.setVisibility(View.GONE);
        pnumhelp.setVisibility(View.GONE);
    }

    private void type2() {  //1->2,         4->2
        type = 2;
        next.setText("다음");
        help.setText("게임 타입을 선택해주세요.");

        styleRadio.setVisibility(View.GONE);
        typeRadio.setVisibility(View.VISIBLE);
        pnum.setVisibility(View.GONE);
        pnumhelp.setVisibility(View.GONE);
        partici.setVisibility(View.GONE);
        recybtn.setVisibility(View.GONE);

        if (resultType2 == 2) type3();
    }

    private void type3() {  //2->3, 1->3         4->3
        type = 3;
        next.setText("다음");
        help.setText("게임 타입을 선택해주세요.");

        styleRadio.setVisibility(View.GONE);
        typeRadio.setVisibility(View.VISIBLE);
        pnum.setVisibility(View.VISIBLE);
        pnumhelp.setVisibility(View.VISIBLE);
        partici.setVisibility(View.GONE);
        recybtn.setVisibility(View.GONE);
    }

    private void type4() {  //2->4, 3->4    5->4
        type = 4;
        if (resultType2 == 1)
            next.setText("완료");
        else
            next.setText("다음");

        help.setText("게임 참여자를 선택해주세요.");

        typeRadio.setVisibility(View.GONE);
        pnum.setVisibility(View.GONE);
        pnumhelp.setVisibility(View.GONE);
        partici.setVisibility(View.VISIBLE);
        recybtn.setVisibility(View.VISIBLE);
        particiAdapter.setViewType(0);
        partici.setAdapter(particiAdapter);

        teampartici.setVisibility(View.GONE);
        team.setVisibility(View.GONE);
        typeTeam.setVisibility(View.GONE);
    }

    private void type5() {  // 4->5   6->5
        type = 5;

        if (resultType5 == 2) next.setText("다음");
        else next.setText("완료");
        help.setText("게임 타입을 선택해주세요.");

        recybtn.setVisibility(View.GONE);
        partici.setVisibility(View.GONE);

        teampartici.setVisibility(View.GONE);
        team.setVisibility(View.GONE);
        typeTeam.setVisibility(View.VISIBLE);

    }

    private void type6() {  // 5->6     //수동선택 한경우
        type = 6;
        next.setText("완료");
        help.setText("게임 참여자를 선택해주세요.");

        teampartici.setVisibility(View.VISIBLE);
        partici.setVisibility(View.VISIBLE);
        partici.setAdapter(teamAllAdapter);

        team.setVisibility(View.VISIBLE);
        typeTeam.setVisibility(View.GONE);

    }


    private boolean makeGame(List<HashMap<String, Object>> particiList) {
        System.out.println("--------------------------------------result-------------------------------------");
        System.out.println(resultType1);
        System.out.println(resultType2);
        System.out.println(resultType3);

        if (resultType1 == 2) {  // (개인, 단체) 에서 단체이면.
            particiList = makeTeam(particiList);
            System.out.println("----------------------------------------단체 팀 조 짠 결과------------------------------------");
            System.out.println(particiList);
            if (particiList.size() /resultType3 == 1) return  false;
        }
        if (particiList.size() == 1) return false;
        Collections.shuffle(particiList);   //랜덤 순서
        if (resultType2 == 1) {  //토너먼트
            makeTournament(particiList);
        } else if (resultType2 == 2) {  //리그
            makeLeague(particiList);
        }

        return true;
    }

    private List<HashMap<String, Object>> makeTeam
            (List<HashMap<String, Object>> particiList) {
        List<HashMap<String, Object>> resultList = new ArrayList();
        //일단 이러면 젤 첨에 순서대로 만든 의미가 없긴 하지만!
        Collections.shuffle(particiList);   //랜덤 순서

        int length = particiList.size();
        for (int i = 0; i < length; i = i + 2) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("userUID1", (particiList.get(i)).get("userUID"));
            map.put("userUID2", (particiList.get(i + 1)).get("userUID"));
            //두명의 평균degree
            int d = (int) ((particiList.get(i)).get("degree")) + (int) ((particiList.get(i)).get("degree"));
            map.put("degree", d / 2);
            resultList.add(map);
        }
        return resultList;
    }

    private void makeLeague(List<HashMap<String, Object>> particiList) {

        List<List<HashMap<String, Object>>> leagueGroup = new ArrayList();
        for (int i = 0; i < resultType3; i++) {
            List<HashMap<String, Object>> list = new ArrayList();
            leagueGroup.add(list);
        }


        for (int i = 0; i < teamParticiList.length; i++) {
            System.out.println(i + "조 인원-------------");
            System.out.println(teamParticiList[i]);
        }


        int length = 0;
        if (resultType5 == 2) {
            for (int i = 0; i < teamParticiList.length; i++) {
                List<HashMap<String, Object>> list = leagueGroup.get(i);
                list.addAll(teamParticiList[i]);
                leagueGroup.set(i, list);

            }
        } else {
            length = particiList.size();
            // particiList에 있는 사람을 순서대로 1조, 2조, 3조 ... 로 넣음
            for (int j = 0; j < length; j = j + resultType3) {
                for (int i = 0; i < resultType3; i++) {
                    if (j + i + 1 > length) break;
                    List<HashMap<String, Object>> list = leagueGroup.get(i);
                    list.add(particiList.get(j + i));
                    leagueGroup.set(i, list);
                }
            }
        }

        System.out.println("-------------------------makeLeague--------------------------");
        System.out.println(leagueGroup);


        for (int i = 0; i < leagueGroup.size(); i++) {
            System.out.println((i + 1) + "<----리그그룹");
            System.out.println(leagueGroup.get(i));
        }

        // db에 저장
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collection = db.collection("Groups/" + groupID + "/game/" + gameID + "/league");


        length = leagueGroup.size();
        for (int i = 1; i <= length; i++) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("info", leagueGroup.get(i - 1));
            collection.document(i + "조").set(map);
            makeLeagueGame(leagueGroup.get(i - 1), i);
        }

    }


    public void makeLeagueGame(List<HashMap<String, Object>> info, int num) {
        List<HashMap<String, Object>> temp = info;
        int teamCount = temp.size();
        List<HashMap<String, Object>> result = new ArrayList();
        int gameNum = 1;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collection = db.collection("Groups/" + groupID + "/game/" + gameID + "/league");
        DocumentReference document = collection.document(num + "조");

        if (teamCount % 2 == 1) {
            HashMap<String, Object> hashmap = new HashMap<>();
            hashmap.put("userUID", "virtual");
            hashmap.put("degree", "0");
            temp.add(hashmap);
        }
        int[] id = new int[temp.size()];
        for (int i = 0; i < temp.size(); i++) {
            id[i] = i;
        }
        for (int i = 0; i < temp.size() - 1; i++) {///////////////짝수일때
            for (int j = 0; j < temp.size() / 2; j++) {
                HashMap<String, Object> map = new HashMap<>();
                String user1 = (String) (temp.get(id[j])).get("userUID");
                String user2 = (String) (temp.get(id[temp.size() - j - 1])).get("userUID");
                String name = num + "조-" + gameNum;

                if (user1 != null) {
                    map.put("user1UID", user1);
                    map.put("user2UID", user2);
                    map.put("gameName", name);
                } else {
                    user1 = (String) (temp.get(id[j])).get("userUID1");
                    user2 = (String) (temp.get(id[temp.size() - j - 1])).get("userUID1");

                    String user3 = (String) (temp.get(id[j])).get("userUID2");
                    String user4 = (String) (temp.get(id[temp.size() - j - 1])).get("userUID2");

                    map.put("user1UID", user1);
                    map.put("user2UID", user2);
                    map.put("user3UID", user3);
                    map.put("user4UID", user4);
                    map.put("gameName", name);

                }


                if (user1.equals("virtual") || user2.equals("virtual")) {
                    continue;
                }
                result.add(map);
                gameNum++;
            }
            for (int j = 0; j < temp.size() - 1; j++) {
                id[j] = (id[j] + 1) % (temp.size() - 1);
            }
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("game", result);
        document.update(map);

    }


    private void makeTournament(List<HashMap<String, Object>> particiList) {

        //depth가 몇인지 구한다.
        int num = particiList.size();
        int depth = 1;
        int n = 2;
        while (n < num) {
            n = n * 2;
            depth++;
        }

        List<HashMap<String, Object>> tournamentResult;
        if (resultType1 == 1) {  //개인이면
            tournamentResult = tournamentSingle(particiList, 2, 1, depth);
        } else { //단체면
            tournamentResult = tournamentDouble(particiList, 2, 1, depth);
        }

        System.out.println("-------------------------makeTournament--------------------------");
        num = tournamentResult.size();
        for (int i = 0; i < num; i++) {
            System.out.println(tournamentResult.get(i));
        }

        // db에 저장
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collection = db.collection("Groups/" + groupID + "/game/" + gameID + "/schedule");

        for (int i = 0; i < num; i++) {
            HashMap<String, Object> map = (HashMap<String, Object>) tournamentResult.get(i);
            String name = (String) map.remove("round");
            if (name != null) {
                collection.document(name).set(map);
            }
        }

    }

    private List<HashMap<String, Object>> tournamentSingle
            (List<HashMap<String, Object>> list, int round, int num, int index) {
        List<HashMap<String, Object>> resultList = new ArrayList();
        HashMap<String, Object> game = new HashMap<>();
        String name = "";
        if (round == 2) name = "결승전";
        else if (round == 4) name = "준결승전-" + num;
        else name = round + "강전-" + num;

        int size = list.size();
        System.out.println(list + "사람리스트");
        if (size == 2) {
            game.put("user1UID", (list.get(0)).get("userUID"));
            game.put("user2UID", (list.get(1)).get("userUID"));
            game.put("round", index + "-" + round + "-" + num);
            game.put("gameName", name);
            resultList.add(game);

            if (index == 2) {
                game = new HashMap<>();
                game.put("user1UID", (list.get(0)).get("userUID"));
                game.put("user2UID", "부전승");
                game.put("round", index - 1 + "-" + round * 2 + "-" + (num * 2 - 1));
                game.put("gameName", round * 2 + "강전-" + (num * 2 - 1));
                resultList.add(game);

                game = new HashMap<>();
                game.put("user1UID", (list.get(1)).get("userUID"));
                game.put("user2UID", "부전승");
                game.put("round", index - 1 + "-" + round * 2 + "-" + (num * 2));
                game.put("gameName", round * 2 + "강전-" + (num * 2));
                resultList.add(game);
            }
        } else if (size == 3) {
            game.put("user1UID", (list.get(0)).get("userUID"));
            game.put("user2UID", (list.get(1)).get("userUID"));
            game.put("round", index - 1 + "-" + round * 2 + "-" + (num * 2 - 1));
            game.put("gameName", round * 2 + "강전-" + (num * 2 - 1));
            resultList.add(game);

            game = new HashMap<>();
            game.put("user1UID", (list.get(2)).get("userUID"));
            game.put("user2UID", "부전승");
            game.put("round", index - 1 + "-" + round * 2 + "-" + (num * 2));
            game.put("gameName", round * 2 + "강전-" + (num * 2));
            resultList.add(game);

            game = new HashMap<>();
            game.put("user1UID", "none");
            game.put("user2UID", (list.get(2)).get("userUID"));
            game.put("round", index + "-" + round + "-" + num);
            game.put("gameName", name);
            resultList.add(game);
        } else {
            List<HashMap<String, Object>> left = list.subList(0, size / 2);
            List<HashMap<String, Object>> right = list.subList(size / 2, size);

            resultList = tournamentSingle(left, round * 2, num * 2 - 1, index - 1);
            resultList.addAll(tournamentSingle(right, round * 2, num * 2, index - 1));

            game.put("user1UID", "none");
            game.put("user2UID", "none");
            game.put("round", index + "-" + round + "-" + num);
            game.put("gameName", name);
            resultList.add(game);
        }


        return resultList;
    }

    private List<HashMap<String, Object>> tournamentDouble
            (List<HashMap<String, Object>> list, int round, int num, int index) {
        List<HashMap<String, Object>> resultList = new ArrayList();
        HashMap<String, Object> game = new HashMap<>();
        String name = "";
        if (round == 2) name = "결승전";
        else if (round == 4) name = "준결승전-" + num;
        else name = round + "강전-" + num;

        int size = list.size();
        if (size == 2) {
            game.put("user1UID", (list.get(0)).get("userUID1"));
            game.put("user3UID", (list.get(0)).get("userUID2"));
            game.put("user2UID", (list.get(1)).get("userUID1"));
            game.put("user4UID", (list.get(1)).get("userUID2"));
            game.put("round", index + "-" + round + "-" + num);
            game.put("gameName", name);
            resultList.add(game);

            if (index == 2) {
                game = new HashMap<>();
                game.put("user1UID", (list.get(0)).get("userUID1"));
                game.put("user3UID", (list.get(0)).get("userUID2"));
                game.put("user2UID", "부전승");
                game.put("round", index - 1 + "-" + round * 2 + "-" + (num * 2 - 1));
                if (round * 2 == 4) game.put("gameName", "준결승전-" + (num * 2 - 1));
                else game.put("gameName", round * 2 + "강전-" + (num * 2 - 1));
                resultList.add(game);

                game = new HashMap<>();
                game.put("user1UID", (list.get(1)).get("userUID1"));
                game.put("user3UID", (list.get(1)).get("userUID2"));
                game.put("user2UID", "부전승");
                game.put("round", index - 1 + "-" + round * 2 + "-" + (num * 2));
                if (round * 2 == 4) game.put("gameName", "준결승전-" + (num * 2));
                else game.put("gameName", round * 2 + "강전-" + (num * 2));
                resultList.add(game);
            }
        } else if (size == 3) {
            game.put("user1UID", (list.get(0)).get("userUID1"));
            game.put("user3UID", (list.get(0)).get("userUID2"));
            game.put("user2UID", (list.get(1)).get("userUID1"));
            game.put("user4UID", (list.get(1)).get("userUID2"));
            game.put("round", index - 1 + "-" + round * 2 + "-" + (num * 2 - 1));

            if (round * 2 == 4) game.put("gameName", "준결승전-" + (num * 2 - 1));
            else game.put("gameName", round * 2 + "강전-" + (num * 2 - 1));
            resultList.add(game);

            game = new HashMap<>();
            game.put("user1UID", (list.get(2)).get("userUID1"));
            game.put("user3UID", (list.get(2)).get("userUID2"));
            game.put("user2UID", "부전승");
            game.put("round", index - 1 + "-" + round * 2 + "-" + (num * 2));
            if (round * 2 == 4) game.put("gameName", "준결승전-" + (num * 2));
            else game.put("gameName", round * 2 + "강전-" + (num * 2));
            resultList.add(game);

            game = new HashMap<>();
            game.put("user1UID", "none");
            game.put("user2UID", (list.get(2)).get("userUID1"));
            game.put("user4UID", (list.get(2)).get("userUID2"));
            game.put("round", index + "-" + round + "-" + num);
            game.put("gameName", name);
            resultList.add(game);
        } else {
            List<HashMap<String, Object>> left = list.subList(0, size / 2);
            List<HashMap<String, Object>> right = list.subList(size / 2, size);

            resultList = tournamentDouble(left, round * 2, num * 2 - 1, index - 1);
            resultList.addAll(tournamentDouble(right, round * 2, num * 2, index - 1));

            game.put("user1UID", "none");
            game.put("user2UID", "none");
            game.put("round", index + "-" + round + "-" + num);
            game.put("gameName", name);
            resultList.add(game);
        }


        return resultList;
    }


    private static final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
    private static final String SERVER_KEY = "//";


    private void saveGame() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Groups").document(groupID).collection("game")
                .document(gameID).update("state", 1).addOnCompleteListener(task -> {
            Toast.makeText(MakeNewGameActivity.this, "게임이 시작되었습니다.", Toast.LENGTH_SHORT).show();
            finish();
        });
        db.collection("Groups").document(groupID).collection("game")
                .document(gameID).update("gameType", resultType2);///1이 토너먼트

        selectedList = particiAdapter.getSelectedsaveList();
        db.collection("Groups").document(groupID).collection("game")
                .document(gameID).update("participants", selectedList);

        db.collection("Groups").document(groupID).collection("notice").document(gameID).delete();
        db.collection("Groups").document(groupID).get().addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();
            String groupName = document.getString("name");

            db.collection("Groups").document(groupID).collection("game").document(gameID).get().addOnCompleteListener(task1 -> {
                DocumentSnapshot documentSnapshot = task1.getResult();
                String gameName = documentSnapshot.getString("gamename");
                String message = groupName + " 그룹에 " + gameName + " 게임의 대진표가 생성되었습니다.";
                new Thread(() -> {
                    sendPostToFCM(message, groupID, gameID, Long.toString(resultType2)); //댓글 달리면 글쓴이한테 알림
                }).start();
            });
        });
    }

    private void sendPostToFCM(final String message, String groupID, String gameID, String
            gameType) {
        // FMC 메시지 생성 start
        JSONObject root = new JSONObject();
        try {

            JSONObject notification = new JSONObject();
            notification.put("body", message);
            notification.put("title", getString(R.string.app_name));
            notification.put("type", "3");//0이 게임, 1이면 글,댓글 2면 그룹신청수락, 3이면 대진표생성알림
            notification.put("groupID", groupID);
            notification.put("gameID", gameID);
            notification.put("gameType", gameType);
            notification.put("managerUID", LoginActivity.userUID);

            root.put("data", notification);
            root.put("to", "/topics/" + groupID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // FMC 메시지 생성 end
        try {
            URL Url = new URL(FCM_MESSAGE_URL);
            HttpURLConnection conn = (HttpURLConnection) Url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.addRequestProperty("Authorization", "key=" + SERVER_KEY);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-type", "application/json");
            OutputStream os = conn.getOutputStream();
            os.write(root.toString().getBytes(StandardCharsets.UTF_8));
            os.flush();
            conn.getResponseCode();
            conn.setRequestMethod("POST");

        } catch (Exception ioException) {
            ioException.printStackTrace();
        }


    }


}


class DegreeComparator implements Comparator<HashMap> {
    @Override
    public int compare(HashMap h1, HashMap h2) {
        int d1, d2;
        if (h1.get("degree") != null) d1 = (int) h1.get("degree");
        else d1 = 10;
        if (h2.get("degree") != null) d2 = (int) h1.get("degree");
        else d2 = 10;

        if (d1 > d2) return 1;
        else if (d1 < d2) return -1;
        return 0;
    }
}
