package com.example.pingpong.SearchSetting;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

public class MyCommentAdapter extends RecyclerView.Adapter<MyCommentAdapter.ItemViewHolder> {
    List<String> myCommentList;
    View view;
    private final String userUID, userName;

    public MyCommentAdapter(List<String> list, String userUID, String userName) {
        this.myCommentList = list;
        this.userUID = userUID;
        this.userName = userName;
    }

    @NonNull
    @Override
    public MyCommentAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mycomment, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyCommentAdapter.ItemViewHolder holder, int position) {
        String commentID = myCommentList.get(position);
        String[] split = commentID.split("/");
        String postID = split[0] + "/" + split[1] + "/" + split[2] + "/" + split[3] + "/" + split[4];
        /////2은 그룹아이디 4는 포스트아이디 6는 코멘트아이디. 왜냐? 0이 비어서!!

        // onResume에서만 새로고침되기때문에 onResume에서는 내용 바뀔수도 있으니 무조건 db에서 들고온다!
        HashMap<String, Object> map = new HashMap<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        map.put("postID", split[4]);
        // 그룹 이름
        db.collection("Groups").document(split[2]).get().addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();
            if (document != null && document.getData() != null) {
                String groupName = (String) document.getData().get("name");
                holder.groupName.setText(groupName);
                map.put("groupName", groupName);
                String manager = (String) document.getData().get("manager");
                map.put("manager", manager);
            }

        });
        // 글 정보
        System.out.println(postID+">>>>>>>.포스트아이디");
        db.document(postID).get().addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();
            if (document != null && document.getData() != null) {
                System.out.println(document.getId()+">>>>>>>.도큐먼트있음");

                String post = (String) document.getData().get("contents");
                holder.post.setText(post);
                map.put("text", post);

                Timestamp time = (Timestamp) document.getData().get("time");
                if (time != null) {
                    Date dtime = time.toDate();
                    @SuppressLint("SimpleDateFormat") DateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    String stime = f.format(dtime);

                    map.put("timestamp", time);
                    map.put("time", stime);
                }

                String writer = (String) document.getData().get("writerUID");
                System.out.println(writer+">>>>>>>.롸이터");


                map.put("writerUID", writer);
                db.collection("Users").document(writer).get().addOnCompleteListener(task1 -> {
                    DocumentSnapshot documentSnapshot = task1.getResult();
                    if (documentSnapshot != null && documentSnapshot.getData() != null) {
                        String name = (String) documentSnapshot.getData().get("name");
                        map.put("name", name);
                    }
                });
            }
        });
        // 댓글 정보
        db.document(commentID).get().addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();
            if (document != null && document.getData() != null) {

                String contents = (String) document.getData().get("content");
                holder.comment.setText(contents);

                Timestamp time = (Timestamp) document.getData().get("time");
                if (time != null) {
                    Date dtime = time.toDate();
                    @SuppressLint("SimpleDateFormat") DateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    String stime = f.format(dtime);
                    holder.time.setText(stime);
                }

                Long type = (Long) document.getData().get("type");
                if (type == null) type = 0L;
                if (type == 0)
                    holder.type.setText("댓글");
                else
                    holder.type.setText("답글");

            }
        });

        // 댓글 작성자는 무조건 자기자신
        holder.name.setText(userName);

        holder.itemView.setOnClickListener(view -> {
            String writer = (String) map.get("writerUID");
            Intent intent = new Intent(view.getContext(), WritingCommentActivity.class);

            String manager = (String) map.get("manager");
            boolean isManager = false;
            if (manager != null) {
                isManager = manager.equals(userUID);
            }
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
        return myCommentList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView groupName;
        TextView name;
        TextView type;
        TextView time;
        TextView post;
        TextView comment;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.mycomment_groupname);
            name = itemView.findViewById(R.id.mycomment_name);
            type = itemView.findViewById(R.id.mycomment_type);
            time = itemView.findViewById(R.id.mycomment_time);
            post = itemView.findViewById(R.id.mycomment_post);
            comment = itemView.findViewById(R.id.mycomment_comment);
        }
    }

    public void setList(List<String> list) {
        this.myCommentList = list;
    }

}
