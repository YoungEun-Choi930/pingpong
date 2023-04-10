package com.example.pingpong.Main;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.example.pingpong.R;

import java.util.HashMap;
import java.util.List;

public class MainGroupAdapter extends RecyclerView.Adapter <MainGroupAdapter.ItemViewHolder> {
    View view;
    List<String> groupStrList;
    HashMap<String, HashMap<String, Object>> groupInfoList;
    private MainGroupAdapter.OnItemClickListener mListener = null;
    RequestManager requestManager;

    public MainGroupAdapter(List<String> list, HashMap<String, HashMap<String, Object>> map, RequestManager requestManager) {
        this.groupStrList = list;
        this.groupInfoList = map;
        this.requestManager = requestManager;
    }

    public void setOnItemClickListener(MainGroupAdapter.OnItemClickListener listener){
        this.mListener = listener;
    }

    public interface OnItemClickListener{ //리스너인터페이스
        void onItemClick(View v, int pos);
    }

    @NonNull
    @Override
    public MainGroupAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_list, parent, false);
        return new ItemViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        String groupID = (String) groupStrList.get(position);
        HashMap<String, Object> info = (HashMap<String, Object>) groupInfoList.get(groupID);

        if(info != null) {
            String groupName = (String) info.get("groupName");
            long member = (long) info.get("member");
            Uri image = (Uri) info.get("image");
            holder.groupName.setText(groupName);
            holder.groupCount.setText("멤버 " + member);
            requestManager.load(image).into(holder.image);
        }

    }

    @Override
    public int getItemCount() {
        return groupStrList.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView groupName;
        TextView groupCount;
        ImageView image;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            groupName = itemView.findViewById(R.id.main_group_name);
            groupCount = itemView.findViewById(R.id.main_group_count);
            image = itemView.findViewById(R.id.main_group_image);

            itemView.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if(pos != RecyclerView.NO_POSITION)
                    mListener.onItemClick(v,pos);
            });
        }
    }

    public void setList(List<String> list, HashMap<String, HashMap<String, Object>> map) {
        this.groupStrList = list;
        this.groupInfoList = map;
    }

}
