package com.ventura.bracketslib.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.ventura.bracketslib.R;
import com.ventura.bracketslib.fragment.BracketsColomnFragment;
import com.ventura.bracketslib.model.MatchData;
import com.ventura.bracketslib.viewholder.BracketsCellViewHolder;

import java.util.ArrayList;

/**
 * Created by Emil on 21/10/17.
 */

public class BracketsCellAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private BracketsColomnFragment fragment;
    private Context context;
    private ArrayList<MatchData> list;
    private boolean handler;
    private int bracketColor;
    private int textColor;

    public BracketsCellAdapter(BracketsColomnFragment bracketsColomnFragment, Context context,
                               ArrayList<MatchData> list) {

        this.fragment = bracketsColomnFragment;
        this.context = context;
        this.list = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.layout_cell_brackets, parent, false);
        return new BracketsCellViewHolder(view, bracketColor, textColor);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        BracketsCellViewHolder viewHolder = null;
        if (holder instanceof BracketsCellViewHolder){
            viewHolder = (BracketsCellViewHolder) holder;
            setFields(viewHolder, position);
        }
    }

    private void setFields(final BracketsCellViewHolder viewHolder, final int position) {
        handler = new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                viewHolder.setAnimation(list.get(position).getHeight());
            }
        }, 100);

        String name1 = list.get(position).getCompetitorOne().getName();
        String name2 = list.get(position).getCompetitorTwo().getName();
        String score1 = list.get(position).getCompetitorOne().getScore();
        String score2 = list.get(position).getCompetitorTwo().getScore();
        viewHolder.getTeamOneName().setText(name1);
        viewHolder.getTeamTwoName().setText(name2);
        viewHolder.getTeamOneScore().setText(score1);
        viewHolder.getTeamTwoScore().setText(score2);

        // 우승자 배경 색 다르게
        if(!score1.equals("결과없음")){
            int s1 = Integer.parseInt(list.get(position).getCompetitorOne().getScore());
            int s2 = Integer.parseInt(list.get(position).getCompetitorTwo().getScore());

            if(s1 > s2) {
                System.out.print("s1>s2");
                viewHolder.getTeamOneLayout().setBackgroundColor(Color.parseColor("#C5C5C5"));
                viewHolder.getTeamTwoLayout().setBackgroundColor(bracketColor);
            } else if(s2 > s1) {
                System.out.print("s1<s2");
                viewHolder.getTeamOneLayout().setBackgroundColor(bracketColor);
                viewHolder.getTeamTwoLayout().setBackgroundColor(Color.parseColor("#C5C5C5"));
            }
        }
        if(name2.equals("부전승")) {
            System.out.print("부전승");
            viewHolder.getTeamOneLayout().setBackgroundColor(Color.parseColor("#C5C5C5"));
            viewHolder.getTeamTwoLayout().setBackgroundColor(bracketColor);
        }
        System.out.println("-------------v");
        System.out.print(name1+" : "+ name2+" ---- ");
        System.out.println(score1+" : "+score2+"\n");
    }

    @Override
    public int getItemCount() {
        return this.list.size();
    }

    public void setList(ArrayList<MatchData> colomnList) {
        this.list = colomnList;
        notifyDataSetChanged();
    }

    public void setBracketColor(int bracketColor) {
        this.bracketColor = bracketColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }
}
