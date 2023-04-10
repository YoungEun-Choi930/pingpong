package com.example.pingpong.Group;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pingpong.R;
import com.google.firebase.Timestamp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class GroupGameAdapter extends RecyclerView.Adapter <GroupGameAdapter.ItemViewHolder> {
    View view;
    List<HashMap<String, Object>> gamelist;
    private GroupGameAdapter.OnItemClickListener mListener = null;


    public GroupGameAdapter(List<HashMap<String, Object>> list) {
        this.gamelist = list;
    }

    public void setOnItemClickListener(GroupGameAdapter.OnItemClickListener listener){
        this.mListener = listener;
    }

    public interface OnItemClickListener{ //리스너인터페이스
        void onItemClick(View v, int pos);
    }

    @NonNull
    @Override
    public GroupGameAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_game, parent, false);
        return new GroupGameAdapter.ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupGameAdapter.ItemViewHolder holder, int position) {
        HashMap<String, Object> game = (HashMap<String, Object>) gamelist.get(position);
        String name = (String) game.get("name");
        Timestamp date = (Timestamp) game.get("date");
        if (date != null) {
            Date d = date.toDate();
            @SuppressLint("SimpleDateFormat") DateFormat format = new SimpleDateFormat("yyyy년 MM월 dd일");
            String strdate = format.format(d);

            holder.gamename.setText(name);
            holder.gamedate.setText(strdate);
        }


    }

    @Override
    public int getItemCount() {
        return gamelist.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView gamename;
        TextView gamedate;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            gamename = itemView.findViewById(R.id.group_game_name);
            gamedate = itemView.findViewById(R.id.group_game_date);

            itemView.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if(pos != RecyclerView.NO_POSITION)
                    mListener.onItemClick(v,pos);
            });
        }
    }

    public void setList(List<HashMap<String, Object>> list) {
        this.gamelist = list;
    }

}
