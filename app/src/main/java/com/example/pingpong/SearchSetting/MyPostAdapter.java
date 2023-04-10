package com.example.pingpong.SearchSetting;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pingpong.Group.WritingCommentActivity;
import com.example.pingpong.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostAdapter extends RecyclerView.Adapter<MyPostAdapter.ItemViewHolder> {
    List<String> myPostList;
    View view;
    private final String userUID, userName;
    Uri userImage;

    private OnItemClickListener mListener;

    public MyPostAdapter(List<String> list, String userUID, String userName, Uri userImage) {
        this.myPostList = list;
        this.userUID = userUID;
        this.userName = userName;
        this.userImage = userImage;
    }

    @NonNull
    @Override
    public MyPostAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mypost, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyPostAdapter.ItemViewHolder holder, int position) {
        String postID = (String) myPostList.get(position);
        String[] split = postID.split("/");

        // onResume에서만 새로고침되기때문에 onResume에서는 내용 바뀔수도 있으니 무조건 db에서 들고온다!
        HashMap<String, Object> map = new HashMap<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Groups").document(split[2]).get().addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();
            String groupName = (String) document.getData().get("name");
            holder.groupName.setText(groupName);
            map.put("groupName", groupName);
            String manager = (String) document.getData().get("manager");
            map.put("manager", manager);

        });
        map.put("postID", split[4]);
        db.document(postID).get().addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();

            Timestamp time = (Timestamp) document.get("time");
            Date dtime = time.toDate();
            @SuppressLint("SimpleDateFormat") DateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String stime = f.format(dtime);
            holder.time.setText(stime);
            map.put("timestamp", time);
            map.put("time", holder.time.getText().toString());

            String post = (String) document.getData().get("contents");
            holder.post.setText(post);
            map.put("text", post);

            String writer = document.getString("writerUID");
            map.put("writerUID", writer);

            // writer는 무조건 본인일것임.
            holder.name.setText(userName);
            Glide.with(view).load(userImage).into(holder.profile);
            map.put("name", userName);


        });


        holder.itemView.setOnClickListener(view -> {

            String writer = (String) map.get("writerUID");
            Intent intent = new Intent(view.getContext(), WritingCommentActivity.class);

            boolean isManager = map.get("manager").equals(userUID);
            intent.putExtra("postInfo", map);//
            intent.putExtra("groupID", split[2]);
            intent.putExtra("postID", split[4]);
            intent.putExtra("userUID", userUID);
            intent.putExtra("isNotice", false);
            intent.putExtra("isManager", isManager);
            intent.putExtra("writerUID", writer);
            view.getContext().startActivity(intent);


        });

    }

    @Override
    public int getItemCount() {
        if (myPostList == null)
            return 0;
        return myPostList.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView groupName;
        CircleImageView profile;
        TextView name;
        TextView time;
        TextView post;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.mypost_groupname);
            profile = itemView.findViewById(R.id.mypost_profile);
            name = itemView.findViewById(R.id.name);
            time = itemView.findViewById(R.id.time);
            post = itemView.findViewById(R.id.mypost_contents);

            itemView.setOnClickListener(view -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION)
                    mListener.onItemClick(view, pos);
            });

        }
    }

    public void setList(List<String> list) {
        this.myPostList = list;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    public interface OnItemClickListener { //리스너인터페이스
        void onItemClick(View v, int pos);
    }


}
