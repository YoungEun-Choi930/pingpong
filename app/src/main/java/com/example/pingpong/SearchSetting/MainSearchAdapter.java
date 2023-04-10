package com.example.pingpong.SearchSetting;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.pingpong.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainSearchAdapter extends RecyclerView.Adapter <MainSearchAdapter.ItemViewHolder> implements Filterable {
    List<HashMap<String, Object>> groupList;
    List<HashMap<String, Object>> filterList;
    View view;
    TextView text;
    private MainSearchAdapter.OnItemClickListener mListener = null;
    RequestManager requestManager;

    public MainSearchAdapter(List<HashMap<String, Object>> list, TextView text, RequestManager requestManager) {
        this.groupList = list;
        this.filterList = list;
        this.text = text;
        this.requestManager = requestManager;
    }

    public void setOnItemClickListener(MainSearchAdapter.OnItemClickListener listener){
        this.mListener = listener;
    }

    public interface OnItemClickListener{ //리스너인터페이스
        void onItemClick(View v, int pos);
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView name, comment, member;
        ImageView image;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.search_item_name);
            comment = itemView.findViewById(R.id.search_item_comment);
            member = itemView.findViewById(R.id.search_item_member);
            image = itemView.findViewById(R.id.search_item_image);

            itemView.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if(pos != RecyclerView.NO_POSITION)
                    mListener.onItemClick(v,pos);
            });

        }
    }

    public void setList(List<HashMap<String, Object>> list) {
        groupList = list;
        filterList = list;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search, parent, false);
        return new MainSearchAdapter.ItemViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        HashMap<String, Object> info = (HashMap<String, Object>) filterList.get(position);
        String name = (String) info.get("groupName");
        String comment = (String) info.get("comment");
        long member = (long) info.get("member");
        Uri image = (Uri) info.get("Uri");

        holder.name.setText(name);
        holder.comment.setText(comment);
        holder.member.setText("멤버 "+member+"명");
        requestManager.load(image).into(holder.image);
    }

    @Override
    public int getItemCount() {
        return filterList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                List<HashMap<String, Object>> result = new ArrayList();

                if(charSequence == null || charSequence.length() == 0) {
                    result = groupList;
                    filterList = groupList;
                } else {
                    String search = charSequence.toString().trim();
                    for(HashMap<String, Object> info: groupList) {
                        String groupName = (String) info.get("groupName");
                        assert groupName != null;
                        if(groupName.contains(search) || groupName.toLowerCase().contains(search.toLowerCase())) {
                            result.add(info);

                        }

                    }
                }

                FilterResults results = new FilterResults();
                results.values = result;
                return results;
            }

            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filterList = (List<HashMap<String, Object>>) filterResults.values;
                notifyDataSetChanged();

                int size = filterList.size();
                if(size != groupList.size())
                    text.setText(size+"개의 검색결과가 있습니다.");
                else
                    text.setText("이런 그룹은 어때요");
            }
        };
    }
}
