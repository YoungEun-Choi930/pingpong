package com.example.pingpong.Game;

import static android.view.View.GONE;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pingpong.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class LeagueRankAdapter extends RecyclerView.Adapter<LeagueRankAdapter.ItemViewHolder> {
    View view;
    List<HashMap<String, Object>> list;
    boolean isManager;
    Context context;
    String groupID, gameID;
    List<HashMap<String, Object>> leagueinfo;
    HashMap<Integer, Object> Rankinfo;

    LeagueRankAdapter(Context context, List list, boolean isManager, String groupID, String gameID) {
        this.list = list;
        this.isManager = isManager;
        this.context = context;
        this.groupID = groupID;
        this.gameID = gameID;
        Rankinfo = new HashMap<>();
    }

    @NonNull
    @Override
    public LeagueRankAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_table_league, parent, false);
        return new LeagueRankAdapter.ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeagueRankAdapter.ItemViewHolder holder, int position) {
        if (list.size() == 0)
            holder.tab.setVisibility(GONE);

        holder.noresult.setVisibility(View.GONE);

        HashMap<String, Object> info = (HashMap<String, Object>) list.get(position);
        String teamName = (String) info.get("teamName");
        System.out.println(teamName+"***********teamNAme");
        holder.number.setText(teamName);
        leagueinfo = (List<HashMap<String, Object>>) info.get("info"); ///조에 들어있는 인원들의 정보

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        List<HashMap<String, Object>> temp = leagueinfo;
        int win,lose;
        for (int i = 0; i < temp.size(); i++) {
            HashMap<String, Object> infomap = (HashMap<String, Object>) temp.get(i);
            if(infomap.get("win")!=null) win= Math.toIntExact((Long) infomap.get("win"));
            else win = 0;
            if(infomap.get("lose")!=null) lose = Math.toIntExact((Long) infomap.get("lose"));
            else lose = 0;
            System.out.println( win * 2 + lose+"위닝포인트  " + infomap.get("userUID")+"이름");
            infomap.put("winningPoint", win * 2 + lose);
            temp.set(i, infomap);
        }
        AtomicReference<RankComparator> rankComparator = new AtomicReference<>();
        db.collection("Groups/" + groupID + "/game/" + gameID + "/league")
                .document(teamName).get().addOnCompleteListener(task -> {
                    DocumentSnapshot document = task.getResult();
                    List<HashMap<String, Object>> leagueList = (List<HashMap<String, Object>>) document.get("game");
                    List<HashMap<String, Object>> leagueinfo =  (List<HashMap<String, Object>>) document.get("info");
                    rankComparator.set(new RankComparator(leagueinfo,leagueList));

                    Collections.sort(temp, rankComparator.get());////승점높은순
                    System.out.println("-----------------------"+teamName);
                    System.out.println("정렬 후 list: ");
                    for(Object o: temp) {
                        System.out.println(o);
                    }
                    Rankinfo.put(holder.getBindingAdapterPosition(),temp);
                    GameTableLeagueItemAdapter leagueItemAdapter = new GameTableLeagueItemAdapter(context, temp, isManager, groupID, gameID, teamName,0);
                    holder.recy_league.setAdapter(leagueItemAdapter);
                    System.out.println("\n");


                    for(int i=0;i<temp.size();i++){
                        HashMap<String, Object> map = (HashMap<String, Object>) temp.get(i);
                        if(map.get("win")!=null){///결과가 존재
                            if ((long) map.get("win")==0&&(long)map.get("lose")==0){///win lose 둘 다 0이면
                                    holder.noresult.setVisibility(View.VISIBLE);///결과없음 보이기
                            }else{
                                holder.noresult.setVisibility(View.GONE); //결과가 한명이라도 존재하면 결과없음 안보이게
                                break;
                            }
                        }
                        else{
                                holder.noresult.setVisibility(View.VISIBLE); //결과없음
                        }
                    }

                });


        holder.recy_league.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        holder.recy_league.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                if(e.getAction() == MotionEvent.ACTION_MOVE){
                    return false;
                }
                if(e.getAction() == MotionEvent.ACTION_UP){

                    List list = new ArrayList();
                    list.addAll(rankComparator.get().detailList);
                    List resultList = new ArrayList();

                    for(int i=0;i<temp.size();i++){
                        String uid = (String) temp.get(i).get("userUID");

                        for(int j = 0; j < list.size(); j++) {
                            HashMap map = (HashMap) list.get(j);
                            if(map.get("userUID").equals(uid)) {
                                resultList.add(list.remove(j));
                            }
                        }
                    }
                    GameTableLeagueItemAdapter adapter = new GameTableLeagueItemAdapter(context, resultList, isManager, groupID, gameID, teamName,1);
                    RankDetailDialog dialog = new RankDetailDialog(view.getContext(), adapter);

                    dialog.show();
                }
                return false;
            }
            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView number, noresult;
        RecyclerView recy_league;
        View tab;
        View rank_view;
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.table_league_number);
            noresult = itemView.findViewById(R.id.no_result);
            recy_league = itemView.findViewById(R.id.recy_table_league);
            rank_view = itemView.findViewById(R.id.rank_view);
            tab = itemView.findViewById(R.id.league_tab);
        }
    }

    public void setList(List<HashMap<String, Object>> list) {
        this.list = list;
    }
    public HashMap<Integer, Object> getRankinfo(){
        return Rankinfo;
    }

}





class RankComparator implements Comparator<HashMap<String, Object>> {

    List<HashMap<String, Object>> leagueList;
    List<String> nameList = new ArrayList<>(); ///이름 저장해서 순서찾아올거임
    List<HashMap<String,Object>> detailList = new ArrayList<>();
    int listSize=0;
    RankComparator(List<HashMap<String, Object>> info, List<HashMap<String, Object>> leagueList) {

        if(leagueList!=null){
            this.leagueList = leagueList;
            listSize = leagueList.size();

            for(int i=0;i<info.size();i++){
                HashMap<String, Object> infomap = (HashMap<String, Object>) info.get(i);
                nameList.add((String) infomap.get("userUID"));
                HashMap map = new HashMap();
                map.put("userUID",infomap.get("userUID"));
                detailList.add(map);
            }
        }
    }

    @Override
    public int compare(HashMap h1, HashMap h2) {
        System.out.println("-----------h1 h2");
        System.out.println(h1);
        System.out.println(h2);
        int d1 = (int) h1.get("winningPoint");
        int d2 = (int) h2.get("winningPoint");
        System.out.println("---compare: "+h1.get("userUID")+", "+h2.get("userUID"));
        System.out.println(nameList+"네임리스트");

        System.out.println("---result: d1-"+d1+", d2- "+d2);

        int p1,p2;

        p1=nameList.indexOf(h1.get("userUID"));
        p2=nameList.indexOf(h2.get("userUID"));

        if (d1 > d2) {
                detailList.get(p1).put("winscore",d1); //그 위치에 정보저장
                detailList.get(p2).put("winscore",d2);
            return -1;
        }
        else if (d1 < d2) {
                detailList.get(p1).put("winscore",d1);
                detailList.get(p2).put("winscore",d2);
            return 1;
        }
        else {
                detailList.get(p1).put("winscore",d1);
                detailList.get(p2).put("winscore",d2);

            if(d1==0){
                return 1;
            }
            if(d2==0){
                return -1;
            }

            int cntWin1 = 0, cntLose1 = 0, cntWin2 = 0, cntLose2 = 0;
            for (int i = 0; i < listSize; i++) {
                HashMap<String, Object> map = (HashMap<String, Object>) leagueList.get(i);

                if(map == null || map.isEmpty()) continue;
                long resultscore1 = 0, resultscore2 = 0;
                if(map.get("resultscore1")!=null) resultscore1 = (long) map.get("resultscore1");
                if(map.get("resultscore2")!=null) resultscore2 = (long) map.get("resultscore2");

                if (map.get("user1UID").equals(h1.get("userUID"))) {
                    cntWin1 += resultscore1;
                    cntLose1 += resultscore2;
                } else if (map.get("user2UID").equals(h1.get("userUID"))) {
                    cntWin1 += resultscore2;
                    cntLose1 += resultscore1;
                }
                if (map.get("user1UID").equals(h2.get("userUID"))) {
                    cntWin2 += resultscore1;
                    cntLose2 += resultscore2;
                } else if (map.get("user2UID").equals(h2.get("userUID"))) {
                    cntWin2 += resultscore2;
                    cntLose2 += resultscore1;
                }
            }

            System.out.println("유저1 "+h1.get("userUID"));
            System.out.println("유저2 "+h2.get("userUID"));
            System.out.println("유저1승세트 : "+cntWin1 + "   유저1패세트 : "+cntLose1 +"   유저2승 : "+cntWin2+"   유저2패 : "+cntLose2);



            double result1, result2;
            if(cntLose1==0)
                return -1;
            if(cntLose2==0)
                return 1;
            result1 = (double) cntWin1 / cntLose1;
            result2 = (double) cntWin2 / cntLose2;

            p1=nameList.indexOf(h1.get("userUID"));
            p2=nameList.indexOf(h2.get("userUID"));


            if (result1 > result2){
                     detailList.get(p1).put("setRate",result1);
                     detailList.get(p2).put("setRate",result2);
                return -1;
            }
            else if (result1 < result2){
                    detailList.get(p1).put("setRate",result1);
                    detailList.get(p2).put("setRate",result2);
                return 1;
            }
            else {


                System.out.println("정말...?");
                cntWin1 = 0;
                cntLose1 = 0;
                cntWin2 = 0;
                cntLose2 = 0;
                    detailList.get(p1).put("setRate",result1);
                    detailList.get(p2).put("setRate",result2);


                for (int i = 0; i < leagueList.size(); i++) {
                    HashMap<String, Object> map = (HashMap<String, Object>) leagueList.get(i);
                    if(map == null || map.isEmpty()) continue;

                    long score1 = 0, score2 = 0, score3 = 0, score4 = 0, score5 = 0;
                    long score6 = 0, score7 = 0, score8 = 0, score9 = 0, score10 = 0;

                    if(map.get("score1") != null) score1 = (long) map.get("score1");
                    if(map.get("score2") != null) score2 = (long) map.get("score2");
                    if(map.get("score3") != null) score3 = (long) map.get("score3");
                    if(map.get("score4") != null) score4 = (long) map.get("score4");
                    if(map.get("score5") != null) score5 = (long) map.get("score5");
                    if(map.get("score6") != null) score6 = (long) map.get("score6");
                    if(map.get("score7") != null) score7 = (long) map.get("score7");
                    if(map.get("score8") != null) score8 = (long) map.get("score8");
                    if(map.get("score8") != null) score9 = (long) map.get("score9");
                    if(map.get("score10") != null) score10 = (long) map.get("score10");



                    if (map.get("user1UID").equals(h1.get("userUID"))) {
                        cntWin1 += score1 + score3 + score5 + score7 + score9;
                        cntLose1 += score2 + score4 + score6 + score8 + score10;

                    } else if (map.get("user2UID").equals(h1.get("userUID"))) {
                        cntWin1 += score2 + score4 + score6 + score8 + score10;
                        cntLose1 += score1 + score3 + score5 + score7 + score9;
                    }

                    if (map.get("user1UID").equals(h2.get("userUID"))) {
                        cntWin2 += score1 + score3 + score5 + score7 + score9;
                        cntLose2 += score2 + score4 + score6 + score8 + score10;

                    } else if (map.get("user2UID").equals(h2.get("userUID"))) {
                        cntWin2 += score2 + score4 + score6 + score8 + score10;
                        cntLose2 += score1 + score3 + score5 + score7 + score9;
                    }

                }

                if(cntLose1==0){
                    return -1;
                }
                if(cntLose2==0){
                    return 1;
                }

                System.out.println("유저1 "+h1.get("userUID"));
                System.out.println("유저2 "+h2.get("userUID"));
                System.out.println("유저1승점 : "+cntWin1 + "   유저1패 : "+cntLose1 +"   유저2승 : "+cntWin2+"   유저2패 : "+cntLose2);


                p1=nameList.indexOf(h1.get("userUID"));
                p2=nameList.indexOf(h2.get("userUID"));


                double r1, r2;
                r1=(double) cntWin1 / cntLose1;
                r2=(double) cntWin2 / cntLose2;
                System.out.println("유저1 "+ r1+"   유저2 "+r2);

                if (r1 > r2){
                       detailList.get(p1).put("scoreRate",r1);
                       detailList.get(p2).put("scoreRate",r2);
                    return -1;
                }
                else{
                        detailList.get(p1).put("scoreRate",r1);
                        detailList.get(p2).put("scoreRate",r2);
                    return 1;

                }


            }
        }

    }
}

