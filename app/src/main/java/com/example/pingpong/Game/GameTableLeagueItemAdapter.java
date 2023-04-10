package com.example.pingpong.Game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pingpong.R;

import java.util.HashMap;
import java.util.List;

public class GameTableLeagueItemAdapter extends RecyclerView.Adapter<GameTableLeagueItemAdapter.ItemViewHolder> {
    List<HashMap<String, Object>> info;
    View view;
    Context context;
    LayoutInflater inflater;
    boolean isManager;
    String groupID, gameID, teamName;
    int type;

    GameTableLeagueItemAdapter(Context context, List<HashMap<String, Object>> info, boolean isManager, String groupID, String gameID, String teamName, int type) {
        this.teamName = teamName;
        this.info = info;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.isManager = isManager;
        this.groupID = groupID;
        this.gameID = gameID;
        this.type = type;

    }

    @NonNull
    @Override
    public GameTableLeagueItemAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = inflater.inflate(R.layout.item_league_recyitem, parent, false);
        return new ItemViewHolder(view);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull GameTableLeagueItemAdapter.ItemViewHolder holder, int position) {


        HashMap<String, Object> leagueinfo = (HashMap<String, Object>) info.get(position);

        // 이름
        String userUID = (String) leagueinfo.get("userUID");
        String userUID1 = (String) leagueinfo.get("userUID1");
        String userUID2 = (String) leagueinfo.get("userUID2");

        String userName;
        if (userUID == null) {//복식
            if (GameInfoFragment.nameMap.get(userUID1).length() > 4) //이름 길면 자름
                userName = GameInfoFragment.nameMap.get(userUID1).substring(0, 4);
            else
                userName = GameInfoFragment.nameMap.get(userUID1);
            if (GameInfoFragment.nameMap.get(userUID2).length() > 4)
                userName += "," + GameInfoFragment.nameMap.get(userUID2).substring(0, 4);
            else
                userName += "," + GameInfoFragment.nameMap.get(userUID2);
        } else {
            if (GameInfoFragment.nameMap.get(userUID).length() > 6)
                userName = GameInfoFragment.nameMap.get(userUID).substring(0, 6);
            else
                userName = GameInfoFragment.nameMap.get(userUID);
        }

        holder.name.setText(userName);
        if (type == 0) { ////승, 패, 순위나오는 뷰

            // 점수
            long win = (long) leagueinfo.getOrDefault("win", 0L);
            long lose = (long) leagueinfo.getOrDefault("lose", 0L);
            if (leagueinfo.get("win") != null) {
                if (win == 0 && (long) lose == 0) {
                    holder.win.setText("");
                    holder.lose.setText("");
                    holder.rank.setText("");
                } else {
                    holder.win.setText(Long.toString(win));
                    holder.lose.setText(Long.toString(lose));
                    holder.rank.setText(Integer.toString(position + 1));
                }

            }

        } else { ////클릭해서 승점, 세트득실률, 점수득실률 나오는 뷰
            int winscore = (int) leagueinfo.getOrDefault("winscore", 0);
            double setRate = (double) leagueinfo.getOrDefault("setRate", 0D);
            double scoreRate = (double) leagueinfo.getOrDefault("scoreRate", 0D);

            if (leagueinfo.get("winscore") != null) {
                if (winscore == 0) {
                    holder.win.setText("");
                    holder.lose.setText("");
                    holder.rank.setText("");
                } else {
                    holder.win.setText(Integer.toString(winscore));
                    if (Double.toString(setRate).length() > 6)
                        holder.lose.setText(Double.toString(setRate).substring(0, 6));
                    else holder.lose.setText(Double.toString(setRate));
                    if (Double.toString(scoreRate).length() > 6)
                        holder.rank.setText(Double.toString(scoreRate).substring(0, 6));
                    else holder.rank.setText(Double.toString(scoreRate));
                }

            }

        }


    }

    @Override
    public int getItemCount() {
        return info.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView name, win, lose, rank;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.recy_league_name);
            win = itemView.findViewById(R.id.recy_league_win);
            lose = itemView.findViewById(R.id.recy_league_lose);
            rank = itemView.findViewById(R.id.recy_league_rank);

        }
    }


}
