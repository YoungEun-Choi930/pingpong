package com.example.pingpong.Group;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.example.pingpong.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewPagerAdapter extends RecyclerView.Adapter <ViewPagerAdapter.ItemViewHolder> {
    List<HashMap<String, Object>> noticeList;
    View view;

    private OnItemClickListener mListener = null;
    HashMap<String, Object> map;
    RequestManager requestManager;
    private final String managerUID;
    public ViewPagerAdapter(List<HashMap<String, Object>> list, String manager, RequestManager requestManager){
        this.noticeList=list;
        managerUID=manager;
        map = new HashMap<>();
        this.requestManager = requestManager;
    }
    @NonNull
    @Override
    public ViewPagerAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grouphome_notice, parent, false);
        view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewPagerAdapter.ItemViewHolder holder, int position) {
        HashMap<String, Object> notice = (HashMap<String, Object>) noticeList.get(position);
        HashMap<String, Object> info = new HashMap<>();

        String writer = (String) notice.get("writerUID");
        info.put("writerUID",writer);
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        db.collection("Users").document(writer).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                String name = (String)document.getData().get("name");
                holder.name.setText(name);
                Uri uri = Uri.parse(document.getString("image"));
                requestManager.load(uri).into(holder.profileImage);

                info.put("name",name);
            }
        });


        if(managerUID != null) {    //관리자에 별표 표시하기
            if(writer.equals(managerUID)){
                (holder).manager.setVisibility(View.VISIBLE);
            }else (holder).manager.setVisibility(View.GONE);
        }
        Timestamp time = (Timestamp) notice.get("time");
        Date date = time.toDate();
        @SuppressLint("SimpleDateFormat") DateFormat format = new SimpleDateFormat("yyyy-MM-dd  HH:mm");
        String strdate = format.format(date);
        String contents = (String) notice.get("contents");
        info.put("text",contents);
        info.put("timestamp",time);

        boolean isGame = (boolean) notice.get("isGame");
        info.put("isGame",isGame);

        holder.time.setText(strdate);
        holder.contents.setText(contents);

        String noticeID = (String) notice.get("noticeID");
        map.put(noticeID,info);
    }

    @Override
    public int getItemCount() {
        return noticeList.size();
    }



    public class ItemViewHolder extends RecyclerView.ViewHolder {
       CircleImageView profileImage;
       TextView name;
       TextView time;
        FrameLayout manager;
       TextView contents;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.grouphome_profile);
            name = itemView.findViewById(R.id.grouphome_name);
            time = itemView.findViewById(R.id.grouphome_time);
            contents = itemView.findViewById(R.id.grouphome_contents);
            manager = itemView.findViewById(R.id.grouphome_manager);
            itemView.setOnClickListener(view -> {
                int pos = getBindingAdapterPosition();
                if(pos != RecyclerView.NO_POSITION)
                    mListener.onItemClick(view,pos);
            });

        }
    }
    public void setList(List<HashMap<String, Object>> list){
        noticeList = list;
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.mListener = listener;
    }

    public interface OnItemClickListener{ //리스너인터페이스
        void onItemClick(View v, int pos);
    }

}
