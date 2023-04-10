package com.example.pingpong.SearchSetting;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.example.pingpong.Game.GameHomeActivity;
import com.example.pingpong.LoginActivity;
import com.example.pingpong.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ItemViewHolder> implements Filterable {
    List<HashMap> list, filteredList;
    View view;
    Uri userImage;
    RequestManager requestManager;
    String userName;
    long r1,r2;

    HistoryAdapter(List<HashMap> list, Uri me, String meName, RequestManager requestManager) {
        calUserWin(list);
        this.list = list;
        this.filteredList = list;
        this.userImage = me;
        this.userName = meName;
        if(userName.length()>5) userName = userName.substring(0,5);
        this.requestManager = requestManager;
    }

    @NonNull
    @Override
    public HistoryAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.ItemViewHolder holder, int position) {
        HashMap<String, Object> info = (HashMap<String, Object>) filteredList.get(position);
        holder.groupName.setText((String) info.get("gameName"));

        Timestamp date = (Timestamp) info.get("gameDate");
        Date d = date.toDate();
        @SuppressLint("SimpleDateFormat") DateFormat format = new SimpleDateFormat("yy/MM/dd  HH:mm");
        String strdate = format.format(d);
        if(info.get("gamescheduleName")!=null)  {
            String gameName = (String) info.get("gamescheduleName");
            //gameName = gameName.split("-")[0];
            holder.gameRound.setText(gameName);
        }


        String user1UID = (String) info.get("user1UID");
        String user2UID = (String) info.get("user2UID");
        String user3UID = (String) info.get("user3UID");
        String user4UID = (String) info.get("user4UID");

        System.out.println("adapter + "+ user1UID+","+user2UID+","+user3UID+","+user4UID);
        System.out.println(info.get("path"));

        String youUID = "", me2UID = "", you2UID = "";

        String userUID = LoginActivity.userUID;

        if (user1UID.equals(userUID)) { //유저1이 본인
            youUID = user2UID;
            me2UID = user3UID;
            you2UID = user4UID;
        }
        else if (user2UID.equals(userUID)) { //유저2가 본인
            youUID = user1UID;
            me2UID = user4UID;
            you2UID = user3UID;
        }
        else if (user3UID!= null && user3UID.equals(userUID)) { //유저3이 본인
            youUID = user2UID;
            me2UID = user1UID;
            you2UID = user4UID;
        }
        else if (user4UID != null && user4UID.equals(userUID)) { //유저4가 본인
            youUID = user1UID;
            me2UID = user2UID;
            you2UID = user3UID;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        System.out.println(youUID+"youUID");
        db.document("Users/" + youUID).get().addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();
            String name = document.getString("name");
            if(name.length()>5) name = name.substring(0,5);
            holder.youN.setText(name);
            requestManager.load(document.getData().get("image")).into(holder.you);
        });

        if (me2UID == null) {
            holder.me2.setVisibility(View.GONE);
            holder.meN2.setVisibility(View.GONE);
            holder.you2.setVisibility(View.GONE);
            holder.youN2.setVisibility(View.GONE);
        } else {
            holder.me2.setVisibility(View.VISIBLE);
            holder.meN2.setVisibility(View.VISIBLE);
            holder.you2.setVisibility(View.VISIBLE);
            holder.youN2.setVisibility(View.VISIBLE);
            db.document("Users/" + me2UID).get().addOnCompleteListener(task -> {
                DocumentSnapshot document = task.getResult();
                String name = document.getString("name");
                if(name.length()>5) name = name.substring(0,5);
                holder.meN2.setText(name);
                requestManager.load(document.getData().get("image")).into(holder.me2);
            });

            db.document("Users/" + you2UID).get().addOnCompleteListener(task -> {
                DocumentSnapshot document = task.getResult();
                String name = document.getString("name");
                if(name.length()>5) name = name.substring(0,5);
                holder.youN2.setText(name);
                requestManager.load(document.getData().get("image")).into(holder.you2);
            });
        }

        if(user1UID.equals(userUID) || (user3UID!= null && user3UID.equals(userUID))){
            holder.resultscore1.setText(Long.toString((long) info.get("resultscore1")));
            holder.resultscore2.setText(Long.toString((long) info.get("resultscore2")));
            if (info.get("score1") != null)
                holder.score1.setText(Long.toString((long) info.get("score1")));
            else holder.score1.setText("0");
            if (info.get("score2") != null)
                holder.score2.setText(Long.toString((long) info.get("score2")));
            else holder.score2.setText("0");
            if (info.get("score3") != null)
                holder.score3.setText(Long.toString((long) info.get("score3")));
            else holder.score3.setText("0");
            if (info.get("score4") != null)
                holder.score4.setText(Long.toString((long) info.get("score4")));
            else holder.score4.setText("0");
            if (info.get("score5") != null)
                holder.score5.setText(Long.toString((long) info.get("score5")));
            else holder.score5.setText("0");
            if (info.get("score6") != null)
                holder.score6.setText(Long.toString((long) info.get("score6")));
            else holder.score6.setText("0");
            if (info.get("score7") != null | info.get("score8") != null) {
                holder.score7.setVisibility(View.VISIBLE);
                holder.score8.setVisibility(View.VISIBLE);
                if (info.get("score7") != null)
                    holder.score7.setText(Long.toString((long) info.get("score7")));
                else holder.score7.setText("0");
                if (info.get("score8") != null)
                    holder.score8.setText(Long.toString((long) info.get("score8")));
                else holder.score8.setText("0");
            }
            if (info.get("score9") != null | info.get("score10") != null) {
                holder.score9.setVisibility(View.VISIBLE);
                holder.score10.setVisibility(View.VISIBLE);
                if (info.get("score9") != null)
                    holder.score9.setText(Long.toString((long) info.get("score9")));
                else holder.score9.setText("0");
                if (info.get("score10") != null)
                    holder.score10.setText(Long.toString((long) info.get("score10")));
                else holder.score10.setText("0");
            }

        }else { ////유저2 유저4가 본인이면 좌우바꿔야하니까
            holder.resultscore2.setText(Long.toString((long) info.get("resultscore1")));
            holder.resultscore1.setText(Long.toString((long) info.get("resultscore2")));
            if (info.get("score1") != null)
                holder.score2.setText(Long.toString((long) info.get("score1")));
            else holder.score2.setText("0");
            if (info.get("score2") != null)
                holder.score1.setText(Long.toString((long) info.get("score2")));
            else holder.score1.setText("0");
            if (info.get("score3") != null)
                holder.score4.setText(Long.toString((long) info.get("score3")));
            else holder.score4.setText("0");
            if (info.get("score4") != null)
                holder.score3.setText(Long.toString((long) info.get("score4")));
            else holder.score3.setText("0");
            if (info.get("score5") != null)
                holder.score6.setText(Long.toString((long) info.get("score5")));
            else holder.score6.setText("0");
            if (info.get("score6") != null)
                holder.score5.setText(Long.toString((long) info.get("score6")));
            else holder.score5.setText("0");
            if (info.get("score7") != null | info.get("score8") != null) {
                holder.score7.setVisibility(View.VISIBLE);
                holder.score8.setVisibility(View.VISIBLE);
                if (info.get("score7") != null)
                    holder.score8.setText(Long.toString((long) info.get("score7")));
                else holder.score8.setText("0");
                if (info.get("score8") != null)
                    holder.score7.setText(Long.toString((long) info.get("score8")));
                else holder.score7.setText("0");
            }
            if (info.get("score9") != null | info.get("score10") != null) {
                holder.score9.setVisibility(View.VISIBLE);
                holder.score10.setVisibility(View.VISIBLE);
                if (info.get("score9") != null)
                    holder.score10.setText(Long.toString((long) info.get("score9")));
                else holder.score10.setText("0");
                if (info.get("score10") != null)
                    holder.score9.setText(Long.toString((long) info.get("score10")));
                else holder.score9.setText("0");
            }

        }
        r1 = Long.parseLong(holder.resultscore1.getText().toString());
        r2 = Long.parseLong(holder.resultscore2.getText().toString());
        if(r1 > r2) {
            holder.layout.setBackgroundColor(Color.parseColor("#EEF9FF"));
        }
        else if(r1 < r2) {
            holder.layout.setBackgroundColor(Color.parseColor("#F6F6F6"));
        }

        holder.btnGameInfo.setOnClickListener(view -> {
            String path = (String) info.get("path");

            String gameID = path.split("/")[3];
            String groupID = path.split("/")[1];

            FirebaseFirestore db1 = FirebaseFirestore.getInstance();
            db1.document("Groups/"+groupID).get().addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    String manager = document.getString("manager");

                    Intent intent = new Intent(view.getContext(), GameHomeActivity.class);
                    intent.putExtra("gameID", gameID);
                    intent.putExtra("groupID", groupID);
                    intent.putExtra("managerUID", manager);
                    intent.putExtra("gameType", (long) info.get("gameType"));

                    view.getContext().startActivity(intent);
                }
            });

            //Toast.makeText(view.getContext(), path,Toast.LENGTH_SHORT).show();
        });

    }

    public void calUserWin(List<HashMap> list) {
        String userUID = LoginActivity.userUID;
        for(HashMap info : list) {
            String user1UID = (String) info.get("user1UID");
            String user3UID = (String) info.get("user3UID");

            long r1, r2;
            if(user1UID.equals(userUID) || (user3UID!= null && user3UID.equals(userUID))){
                r1 = (long) info.get("resultscore1");
                r2 = (long) info.get("resultscore2");
            }
            else {
                r2 = (long) info.get("resultscore1");
                r1 = (long) info.get("resultscore2");
            }
            if(r1 > r2) {
                info.put("resultWin",true);
            }
            else if(r1 < r2) {
                info.put("resultWin",false);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        if(filteredList == null) return 0;
        return filteredList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                List<HashMap> list = filteredList;
                List<HashMap> result = new ArrayList();
                String search = charSequence.toString().trim();
                System.out.println(search+"서치스트링");

                //단식 복식
                String team = search.substring(0,2);
                search = search.substring(2);
                if(team.equals("단식")) {
                    for (HashMap<String, Object> info : list) {
                        System.out.println("단식필터");
                        System.out.println(info.get("user3UID")+"<-------user3UID");
                        if (info.get("user3UID")==null) result.add(info);
                    }
                }else if(team.equals("복식")){
                    for (HashMap<String, Object> info : list) {
                        if (info.get("user3UID")!=null) result.add(info);
                    }
                }else {
                    result = filteredList;
                }

                list = result;
                result = new ArrayList<>();

                //이긴게임 진게임
                if(search.contains("이긴 게임")){
                    search = search.split("이긴 게임")[1];
                    for (HashMap<String, Object> info : list) {
                        if((boolean) info.get("resultWin")) result.add(info);
                    }
                }else if(search.contains("진 게임")){
                    search = search.split("진 게임")[1];
                    for (HashMap<String, Object> info : list) {
                        if(!(boolean) info.get("resultWin")) result.add(info);
                    }
                }else {
                    search = search.substring(3);
                    result = list;
                }

                list = result;
                result = new ArrayList<>();

                //그룹 스피너
                if (!search.contains("전체")){

                    for (HashMap<String, Object> info : list) {
                        String path = (String) info.get("path");
                        String groupName = path.split("/")[1]; //그룹이름
                        if (groupName.equals(search)) {
                            result.add(info);
                        }
                    }
                }else {
                    result = list;
                }
                list = result;

                FilterResults results = new FilterResults();
                results.values = list;
                return results;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredList = (List<HashMap>) filterResults.values;
                notifyDataSetChanged();

            }
        };
    }


    public class ItemViewHolder extends RecyclerView.ViewHolder {
        CircleImageView me, you, me2, you2;
        TextView groupName, gameRound, meN, meN2, youN, youN2;
        Button btnGameInfo;
        TextView score1, score2, score3, score4, score5, score6, score7, score8, score9, score10, resultscore1, resultscore2;
        View layout;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            me = itemView.findViewById(R.id.me);
            me2 = itemView.findViewById(R.id.me2);
            you = itemView.findViewById(R.id.you);
            you2 = itemView.findViewById(R.id.you2);
            meN = itemView.findViewById(R.id.me_name);
            meN2 = itemView.findViewById(R.id.me_name2);
            youN = itemView.findViewById(R.id.you_name);
            youN2 = itemView.findViewById(R.id.you_name2);
            layout = itemView.findViewById(R.id.linearLayout);
            requestManager.load(userImage).into(me);
            meN.setText(userName);

            groupName = itemView.findViewById(R.id.groupName);
            gameRound = itemView.findViewById(R.id.gameRound);
            btnGameInfo = itemView.findViewById(R.id.btn_gameinfo);

            score1 = itemView.findViewById(R.id.game_score_1);
            score2 = itemView.findViewById(R.id.game_score_2);
            score3 = itemView.findViewById(R.id.game_score_3);
            score4 = itemView.findViewById(R.id.game_score_4);
            score5 = itemView.findViewById(R.id.game_score_5);
            score6 = itemView.findViewById(R.id.game_score_6);
            score7 = itemView.findViewById(R.id.game_score_7);
            score8 = itemView.findViewById(R.id.game_score_8);
            score9 = itemView.findViewById(R.id.game_score_9);
            score10 = itemView.findViewById(R.id.game_score_10);
            resultscore1 = itemView.findViewById(R.id.game_score_result1);
            resultscore2 = itemView.findViewById(R.id.game_score_result2);


        }
    }
}
