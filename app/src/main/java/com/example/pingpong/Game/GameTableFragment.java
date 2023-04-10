package com.example.pingpong.Game;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pingpong.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameTableFragment extends Fragment {
    String groupID, gameID;
    boolean ismanager;
    List<HashMap<String, Object>> scheduleList, rankList;
    List<HashMap<String, Object>> leagueList;
    long gametype = 1; ////2가 리그+토너먼트, 1이 그냥 토너먼트

    Button btnRank, btnLeague, btnTournament;
    RecyclerView tableRecy;
    GameTableAdapter scheduleAdapter, leagueAdapter;
    LeagueRankAdapter rankAdapter;
    View isleague;
    View makeTournament;

    Spinner spinner;
    ArrayAdapter teamSpinnerAdapter, roundSpinnerAdapter;
    String[] teamList, roundList;
    String selected_item;
    int currentView = 0;////0이 조, 1이 리그, 2가 토너먼트

    public GameTableFragment(String groupID, String gameID, Long gameType, boolean ismanager) {
        this.groupID = groupID;
        this.gameID = gameID;
        this.ismanager = ismanager;
        this.gametype = gameType;
        scheduleList = new ArrayList();
        rankList = new ArrayList();
        leagueList = new ArrayList();
        if(gametype == 1) currentView = 2;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Groups/" + groupID + "/game/" + gameID + "/league").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot query = task.getResult();
                if(query != null) {
                    if (query.getDocuments() == null) gametype = 0;
                }
            }
        });
        ///////////////토너먼트////////////////////
        db.collection("Groups/" + groupID + "/game/" + gameID + "/schedule").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot query = task.getResult();
                        String round = "";
                        if (query != null) {
                            for (DocumentSnapshot document : query.getDocuments()) {
                                round = document.getId();
                                HashMap<String, Object> map = (HashMap<String, Object>) document.getData();
                                map.put("round", round);
                                scheduleList.add(map);
                            }
                        }
                        System.out.println(round+"마지막라운드");

                        round = (round.split("-")[0]);

                        if(!round.equals("")){
                            int r = Integer.parseInt(round);
                            roundList = new String[r+1];
                            roundList[0] = "전체보기";

                            int j = 8;
                            for(int i = 1; i < r+1; i++) {
                                if(i == 1) roundList[1] = "결승전";
                                else if(i == 2) roundList[2] = "준결승전";
                                else {
                                    roundList[i] = j + "강전";
                                    j = j * 2;
                                }
                            }
                        }

                    }

                });

        if (gametype == 2) {

            ///////////////////////////리그///////////////////////
            db.collection("Groups/" + groupID + "/game/" + gameID + "/league").get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot query = task.getResult();
                            teamList = new String[query.getDocuments().size()+1];
                            teamList[0] = "전체보기";
                            int j=1;
                            for (DocumentSnapshot document : query.getDocuments()) {
                                HashMap<String, Object> map;
                                List<HashMap<String, Object>> list = (List<HashMap<String, Object>>) document.get("game");
                                teamList[j++] = document.getId();
                                if (list != null) {
                                    for (int i = 0; i < list.size(); i++) {
                                        map = list.get(i);
                                        map.put("gameName", map.get("gameName"));
                                        leagueList.add(map);
                                    }
                                }

                            }
                            for (String s : teamList) {
                                System.out.println(s + "0000000000");
                            }
                        }
                    });

        }


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_table, container, false);

        btnRank = view.findViewById(R.id.game_table_btn_rank);

        btnLeague = view.findViewById(R.id.game_table_league_btn);
        btnTournament = view.findViewById(R.id.game_table_tournament_btn);
        tableRecy = view.findViewById(R.id.game_table_recy);
        isleague = view.findViewById(R.id.game_table_isleague);
        makeTournament = view.findViewById(R.id.make_tournament);
        scheduleAdapter = new GameTableAdapter(scheduleList, ismanager, groupID, gameID, true);
        leagueAdapter = new GameTableAdapter(leagueList, ismanager, groupID, gameID, false);
        tableRecy.setAdapter(scheduleAdapter);

        rankAdapter = new LeagueRankAdapter(getContext(), rankList, ismanager, groupID, gameID);



        ////////////////////필터 스피너
        spinner = view.findViewById(R.id.spinner);
        if(teamList!=null)
          teamSpinnerAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_dropdown_item, teamList);
        if(roundList!=null)
           roundSpinnerAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_dropdown_item, roundList);




//////////////////순위버튼

        btnRank.setOnClickListener(view1 -> {
            currentView = 0;
            spinner.setVisibility(View.GONE);
            btnRank.setBackgroundColor(Color.parseColor("#4BAFFF"));
            btnLeague.setBackgroundColor(Color.parseColor("#97CDF8"));
            btnTournament.setBackgroundColor(Color.parseColor("#97CDF8"));
            makeTournament.setVisibility(GONE);

            //////////////////////////리그 #순위# //////////////////////////
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Groups/" + groupID + "/game/" + gameID + "/league").get()
                    .addOnCompleteListener(task -> {
                        rankList = new ArrayList();

                        if (task.isSuccessful()) {
                            QuerySnapshot query = task.getResult();

                            if (query != null) {
                                for (DocumentSnapshot document : query.getDocuments()) {
                                    String team = document.getId();
                                    HashMap<String, Object> map = (HashMap<String, Object>) document.getData();
                                    if(map != null) {
                                        map.put("teamName", team);
                                        rankList.add(map);
                                    }
                                }
                            }
                        }
                        rankAdapter.setList(rankList);
                        tableRecy.setAdapter(rankAdapter);

                    });
        });
///////////////////예선(리그)버튼
        btnLeague.setOnClickListener(view12 -> {
            spinner.setVisibility(View.VISIBLE);
            spinner.setAdapter(teamSpinnerAdapter);
            currentView = 1;
            makeTournament.setVisibility(GONE);
            btnRank.setBackgroundColor(Color.parseColor("#97CDF8"));
            btnLeague.setBackgroundColor(Color.parseColor("#4BAFFF"));
            btnTournament.setBackgroundColor(Color.parseColor("#97CDF8"));
            tableRecy.setAdapter(leagueAdapter);
            btnRank.setVisibility(View.VISIBLE);

        });
/////////////////////////본선(토너먼트)버튼
        btnTournament.setOnClickListener(view13 -> {
            spinner.setVisibility(View.VISIBLE);
            spinner.setAdapter(roundSpinnerAdapter);
            currentView = 2;
            btnRank.setBackgroundColor(Color.parseColor("#97CDF8"));
            btnLeague.setBackgroundColor(Color.parseColor("#97CDF8"));
            btnTournament.setBackgroundColor(Color.parseColor("#4BAFFF"));

            if (scheduleList.size() != 0) {
                makeTournament.setVisibility(GONE);
                tableRecy.setAdapter(scheduleAdapter);
            } else {
                makeTournament.setVisibility(View.VISIBLE);
            }

        });

        //////처음 화면에 보여질때-------------------
        if (gametype == 2) {//리그+토너먼트

            isleague.setVisibility(View.VISIBLE);
            btnRank.setBackgroundColor(Color.parseColor("#4BAFFF"));
            btnRank.setVisibility(View.VISIBLE);
            tableRecy.setAdapter(rankAdapter);
            spinner.setAdapter(teamSpinnerAdapter);

            btnRank.callOnClick();


        } else if (gametype == 1) {///토너먼트
            isleague.setVisibility(GONE);
            tableRecy.setAdapter(scheduleAdapter);
            makeTournament.setVisibility(GONE);
            spinner.setAdapter(roundSpinnerAdapter);

        }
        Button btn_make_tournament = view.findViewById(R.id.btn_make_tournament);
        makeTournament.setVisibility(GONE);
        btn_make_tournament.setOnClickListener(view14 -> {

            List<List> list = makeTournamentGame();
            makeTournament.setVisibility(GONE);
            for(List resultlist : list){
                scheduleList.addAll(resultlist);
            }
            scheduleAdapter.setList(scheduleList);
            scheduleAdapter.notifyDataSetChanged();
            tableRecy.setAdapter(scheduleAdapter);
            spinner.setVisibility(View.VISIBLE);
            roundSpinnerAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_dropdown_item, roundList);
            spinner.setAdapter(roundSpinnerAdapter);

        });


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(currentView ==1){
                    ((TextView) view).setText(teamList[i]);
                    selected_item = teamList[i];
                    leagueAdapter.getFilter().filter(selected_item);
                }
                else if(currentView ==2){
                    System.out.println("spinner setOnItemSelected=-------------");
                    System.out.println(roundList[i]);
                    System.out.println(view);
                    ((TextView) view).setText(roundList[i]);
                    selected_item = roundList[i];
                    scheduleAdapter.getFilter().filter(selected_item);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });


        return view;
    }

    private List makeTournamentGame() {
        HashMap<Integer, Object> rankInfo = rankAdapter.getRankinfo(); ///조별로 순위리스트가져옴
        System.out.println(rankInfo);
        System.out.println("랭크인포");
        List<List> resultList = new ArrayList();
        List firstList = new ArrayList();
        List temp = (List) rankInfo.get(0);
        int max = temp.size();///몇등까지인지
        List<HashMap<Integer, Object>>[] rankList = new List[max]; ///순위별로 사람들을 모아놓을 리스트
        for (int i = 0; i < rankList.length; i++)
            rankList[i] = new ArrayList();////초기화

        for (int i = 0; i < rankInfo.size(); i++) {
            List<HashMap<Integer, Object>> list = (List<HashMap<Integer, Object>>) rankInfo.get(i);
            for (int j = 0; j < list.size(); j++) {
                HashMap<Integer, Object> map = (HashMap<Integer, Object>) list.get(j);
                map.remove("winningPoint");
                map.remove("win");
                map.remove("lose");
                map.remove("degree");
                rankList[j].add(map); //이름만 남겨서 집어넣음
            }

        }

        int cntPeople = 0;
        for (int i = 0; i < rankList.length; i++)//// 총 인원 수
            cntPeople += rankList[i].size();

        int cntWalkOver; //부전승인 사람 수
        int num;
        int depth = 0;
        for (num = 2; ; num = num * 2) {
            depth++;
            if (num >= cntPeople)
                break;
        }

        cntWalkOver = num - cntPeople;
        System.out.println("총 인원:"+num+ " 카운트피플 : "+cntPeople);
        int curDepth = 1;//n-*-*
        //Math.pow(2,depth) *-n-*
        num = 1;//*-*-n

        List people = new ArrayList();
        for (int i = 0; i < rankList.length; i++) {
            Collections.shuffle(rankList[i]);
            for (int j = 0; j < rankList[i].size(); j++) {
                people.add(rankList[i].get(j));//순위높은순으로 일렬로 세움
            }
        }

        List<HashMap<String, Object>> tempList = new ArrayList(people);


        for (int j = 0; j < cntWalkOver; j++) {//높은순위사람들 부전승으로 올림
            HashMap<String, Object> game = new HashMap<>();
            HashMap<String, Object> map = (HashMap<String, Object>) tempList.get(j);
            String name;
            if (curDepth == depth)
                name = "결승전";
            else if (depth - curDepth == 1)
                name = "준결승전-"+num;
            else
                name = (int)Math.pow(2, depth-curDepth+1) + "강전-"+num;


            if(map.get("userUID")!=null){//단식

                game.put("user1UID", map.get("userUID"));
                game.put("user2UID", "부전승");
            }else{//복식
                game.put("user1UID", map.get("userUID1"));
                game.put("user3UID",map.get("userUID2"));
                game.put("user2UID", "부전승");
                game.put("user4UID","부전승");
            }
            int round = (int) Math.pow(2, depth);
            game.put("round", curDepth + "-" + round + "-" + num);
            game.put("gameName",name);
            people.remove(tempList.get(j));
            firstList.add(game);

            num++;

        }
       // resultList.add(firstList);
        List notWalkOver = new ArrayList(people);
        System.out.println("부전승올리고 남은사람들");
        System.out.println(notWalkOver);
        System.out.println(depth+"뎁스"+curDepth);
        while (curDepth <= depth) {//1-*-*
                if (people.size() != 0) {   //1-*-*일떄
                    HashMap<String, Object> game;
                    for (int i = 0; i<(notWalkOver.size())/2; i++) {
                        String name;

                        if (curDepth == depth)
                            name = "결승전";
                        else if (depth - curDepth == 1)
                            name = "준결승전-"+num;
                        else
                            name = (int)Math.pow(2, depth-curDepth+1) + "강전-"+num;


                        HashMap<String, Object> map1 = (HashMap<String, Object>) notWalkOver.get(i);//첫번째친구랑
                        HashMap<String, Object> map2 = (HashMap<String, Object>) notWalkOver.get(notWalkOver.size()-1 - i);//마지막친구랑 붙임
                        game = new HashMap();

                        if(map1.get("userUID")!=null){//단식
                            game.put("user1UID", map1.get("userUID"));
                            game.put("user2UID", map2.get("userUID"));
                        }else{//복식
                            game.put("user1UID", map1.get("userUID1"));
                            game.put("user3UID",map1.get("userUID2"));
                            game.put("user2UID", map2.get("userUID1"));
                            game.put("user4UID", map2.get("userUID2"));
                        }
                        System.out.println(game.get("user1UID")+"유저1"+game.get("user2UID")+"유저2");
                        people.remove(map1);
                        people.remove(map2);

                        game.put("round", curDepth + "-" + (int)Math.pow(2, depth) + "-" + num);
                        game.put("gameName",name);
                        firstList.add(game);
                        num++;
                    }

                } else if(curDepth == 2) {
                    firstList = resultList.get(0);
                    List gameList = new ArrayList();
                    String name;
                    for(int i=0;i<Math.pow(2, depth-1)/2;i++ ){

                        if (curDepth == depth)
                            name = "결승전";
                        else if (depth - curDepth == 1)
                            name = "준결승전-"+num;
                        else
                            name = (int)Math.pow(2, depth-1) + "강전-"+num;

                        System.out.println(firstList+"펄스트리스트");
                        System.out.println(i+" ("+Math.pow(2, depth-1)+")");
                        System.out.println("get "+ i*2);
                        HashMap<String, Object> game= new HashMap<>();

                        HashMap<String, Object> map1 = (HashMap<String, Object>) firstList.get(i*2);
                        HashMap<String, Object> map2 = (HashMap<String, Object>) firstList.get(i*2+1);

                        System.out.println(map1+"맵1");
                        System.out.println(map2+"맵2");

                        if(map1.get("user3UID")==null){//단식
                            if(map1.get("user2UID").equals("부전승"))
                                game.put("user1UID",map1.get("user1UID"));
                            else game.put("user1UID", "none");
                            if(map2.get("user2UID").equals("부전승"))
                                game.put("user2UID",map2.get("user1UID"));
                            else game.put("user2UID", "none");
                        }else{//복식
                            if(map1.get("user2UID").equals("부전승")){
                                game.put("user1UID",map1.get("user1UID"));
                                game.put("user3UID",map1.get("user3UID"));
                            }
                            else game.put("user1UID", "none");

                            if(map2.get("user2UID").equals("부전승")){
                                game.put("user2UID",map2.get("user1UID"));
                                game.put("user4UID",map2.get("user3UID"));
                            }
                            else game.put("user2UID", "none");
                        }


                        game.put("round", 2 + "-" + (int)Math.pow(2, depth-1) + "-" + num);
                        game.put("gameName", name);
                        gameList.add(game);
                        System.out.println(game);
                        num++;
                    }

                    firstList = gameList;

                } else {
                    firstList = new ArrayList();
                    String name;

                    if(curDepth!=1){ //첫 와일문에서 people이 0인 경우(부전승 올리고 남은사람이 0일때)가 발생해서 추가안되게 조건 추가
                        for(int i=0;i<Math.pow(2, depth-curDepth+1)/2;i++ ){

                            if (curDepth == depth)
                                name = "결승전";
                            else if (depth - curDepth == 1)
                                name = "준결승전-"+num;
                            else
                                name = (int)Math.pow(2, depth-curDepth+1) + "강전-"+num;

                            HashMap<String, Object> game= new HashMap<>();
                            game.put("user1UID", "none");
                            game.put("user2UID", "none");
                            game.put("round", curDepth + "-" + (int)Math.pow(2, depth-curDepth+1) + "-" + num);
                            game.put("gameName", name);
                            firstList.add(game);
                            num++;
                        }
                    }

                }
            num = 1;
            curDepth++;
            resultList.add(firstList);

        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collection = db.collection("Groups/"+groupID+"/game/"+gameID+"/schedule");





        for(List list : resultList) {
            for(int i = 0; i < list.size(); i++) {
                HashMap<String, Object> map = new HashMap<>();
                map.putAll((Map) list.get(i));
                String name = (String) map.remove("round");
                collection.document(name).set(map);


                name = (name.split("-")[0]);
                System.out.println(name+"몇강전이죠?");
                int r = Integer.parseInt(name);
                roundList = new String[r+1];
                roundList[0] = "전체보기";
                int j = 8;
                for(int k = 1; k < r+1; k++) {
                    if(k == 1) roundList[1] = "결승전";
                    else if(k == 2) roundList[2] = "준결승전";
                    else {
                        roundList[k] = j + "강전";
                        j = j * 2;
                    }
                }

            }
        }

        return resultList;

    }




}