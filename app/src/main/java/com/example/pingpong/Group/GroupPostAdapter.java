package com.example.pingpong.Group;

import static android.view.View.GONE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.example.pingpong.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kr.co.prnd.readmore.ReadMoreTextView;

public class GroupPostAdapter extends RecyclerView.Adapter<GroupPostAdapter.ItemViewHolder> {
    List<HashMap<String, Object>> groupPostList;
    View view;
    Activity activity;
    private OnItemClickListener mListener;
    HashMap<String, Object> info;
    RequestManager requestManager;
    private String groupID, userUID, manager;

    public GroupPostAdapter(List<HashMap<String,Object>> list, Activity activity, String groupID, String userUID, String manager, RequestManager requestManager) {
        this.groupPostList = list;
        this.activity = activity;
        this.groupID = groupID;
        this.userUID = userUID;
        this.manager = manager;
        this.info = new HashMap<>();
        this.requestManager = requestManager;
    }

    @NonNull
    @Override
    public GroupPostAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_post, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupPostAdapter.ItemViewHolder holder, int position) {

        String postID = (String) groupPostList.get(position).get("postID");
        String text = (String) groupPostList.get(position).get("content");
        String writerUID = (String) groupPostList.get(position).get("writerUID");
        Timestamp time = (Timestamp) groupPostList.get(position).get("time");

        HashMap<String, Object> map = new HashMap<>();
        map.put("postID", postID);

        holder.text.setText(text);
        map.put("text", text);

        Date dtime = time.toDate();
        @SuppressLint("SimpleDateFormat") DateFormat f = new SimpleDateFormat("yyyy-MM-dd  HH:mm");
        String stime = f.format(dtime);
        holder.time.setText(stime);
        map.put("timestamp", time);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        boolean isManager = userUID.equals(manager);

        (holder).manager.setVisibility(View.GONE);
        holder.btn_more.setVisibility(View.VISIBLE);
        System.out.println(writerUID+" **  "+userUID);
        if (!writerUID.equals(userUID) && !isManager) {
            holder.btn_more.setVisibility(GONE);
        }
        map.put("writerUID", writerUID);
        db.collection("Users").document(writerUID).get().addOnCompleteListener(task1 -> {
            DocumentSnapshot document1 = task1.getResult();
            String writerName = (String) document1.getData().get("name");
            holder.name.setText(writerName);
            map.put("name", writerName);
            Uri uri = Uri.parse(document1.getString("image"));
            requestManager.load(uri).into(holder.profile);

        });
        if (manager != null) {    //관리자에 별표 표시하기
            if (writerUID.equals(manager)) {
                (holder).manager.setVisibility(View.VISIBLE);
            }
        }


//
//        db.collection("Groups").document(groupID).collection("post").document(postID).get().addOnCompleteListener(task -> {
//            DocumentSnapshot document = task.getResult();
//            if (document != null) {
//                String text = (String) document.getData().get("content");
//
//                holder.text.setText(text);
//                map.put("text", text);
//
//                Timestamp time = (Timestamp) document.getData().get("time");
//                Date dtime = time.toDate();
//                @SuppressLint("SimpleDateFormat") DateFormat f = new SimpleDateFormat("yyyy-MM-dd  HH:mm");
//                String stime = f.format(dtime);
//                holder.time.setText(stime);
//                map.put("timestamp", time);
//
//                String writerUID = document.getString("writerUID");
//                if (!writerUID.equals(userUID) && !isManager) {
//                    holder.btn_more.setVisibility(GONE);
//                }
//                map.put("writerUID", writerUID);
//                db.collection("Users").document(writerUID).get().addOnCompleteListener(task1 -> {
//                    DocumentSnapshot document1 = task1.getResult();
//                    String writerName = (String) document1.getData().get("name");
//                    holder.name.setText(writerName);
//                    map.put("name", writerName);
//                    Uri uri = Uri.parse(document1.getString("image"));
//                    requestManager.load(uri).into(holder.profile);
//
//                });
//                if (manager != null) {    //관리자에 별표 표시하기
//                    if (writerUID.equals(manager)) {
//                        (holder).manager.setVisibility(View.VISIBLE);
//                    }
//                }
//            }
//
//        });


        info.put(postID, map);

        //////댓글쓰기 클릭
        holder.commentLayout.setOnClickListener(view -> {
            String writer = (String) map.remove("writerUID");
            Intent intent = new Intent(view.getContext(), WritingCommentActivity.class);

            intent.putExtra("postInfo", map);//
            intent.putExtra("groupID", groupID);
            intent.putExtra("postID", postID);
            intent.putExtra("userUID", userUID);
            intent.putExtra("isNotice", false);
            intent.putExtra("isManager", isManager);
            intent.putExtra("writerUID", writer);

            //키보드 올리기
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            view.getContext().startActivity(intent);
        });


    }

    @Override
    public int getItemCount() {
        return groupPostList.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profile;
        TextView name;
        TextView time;
        ImageView btn_more;
        ReadMoreTextView text;
        ViewGroup commentLayout;
        FrameLayout manager;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            profile = itemView.findViewById(R.id.grouppost_profile);
            name = itemView.findViewById(R.id.grouppost_name);
            time = itemView.findViewById(R.id.grouppost_time);
            btn_more = itemView.findViewById(R.id.grouppost_btn_more);
            text = itemView.findViewById(R.id.grouppost_text);
            commentLayout = itemView.findViewById(R.id.btn_post_comment);
            manager = itemView.findViewById(R.id.grouppost_manager);
            btn_more.setOnClickListener(view -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    mListener.onItemClick(view, pos);
                }
            });


        }
    }

    public void setList(List<HashMap<String,Object>> list) {
        this.groupPostList = list;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int pos);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }


}
