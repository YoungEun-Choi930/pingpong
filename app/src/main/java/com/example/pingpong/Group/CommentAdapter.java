package com.example.pingpong.Group;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.pingpong.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ItemViewHolder> {
   List<HashMap<String, Object>> commentList;
   HashMap<String, List> replyList;
   public View view;
   String userUID, groupID, postID;

    private OnItemLongClickListener mLongListener = null;
    private OnItemClickListener mListener;
    RequestManager requestManager;
    public int selectedPosition = -1;
    private boolean isReply, isManager;
   public CommentAdapter(List<HashMap<String, Object>> list, HashMap<String, List> replyList, String userUID, String groupID, String postID, RequestManager requestManager, boolean isReply, boolean isManager){
       this.commentList=list;
       this.replyList = replyList;
       this.userUID = userUID;
       this.groupID = groupID;
       this.postID = postID;
       this.requestManager = requestManager;
       this.isReply = isReply;
       this.isManager = isManager;
   }
    @NonNull
    @Override
    public CommentAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new ItemViewHolder(view);
    }

    @SuppressLint({"NotifyDataSetChanged", "ResourceType"})
    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.ItemViewHolder holder, int position) {
        HashMap<String, Object> comment = (HashMap<String, Object>) commentList.get(position);

        if(isReply) holder.btn_reply.setVisibility(View.GONE);
        else holder.btn_reply.setVisibility(View.VISIBLE);

        String writerUID = (String) comment.get("writerUID");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(writerUID).get().addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();
            String writerName = (String) document.getData().get("name");
            holder.name.setText(writerName);
            Uri uri = Uri.parse(document.getString("image"));
            requestManager.load(uri).into(((ItemViewHolder)holder).profile);

        });

        Timestamp time = (Timestamp) comment.get("time");
        if(time != null) {
            Date dtime = time.toDate();
            @SuppressLint("SimpleDateFormat") DateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String stime = f.format(dtime);
            holder.time.setText(stime);
        }


        holder.comment.setText((String)comment.get("contents"));


        List<HashMap<String, Object>> reply = replyList.get(comment.get("group"));

        CommentAdapter adapter = new CommentAdapter(reply, new HashMap(), userUID, groupID, postID, requestManager, true, isManager);
        if(reply != null) {
            adapter.setOnItemClickListener((v, pos) -> {
                ///키보드 올리기
                InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY);
                HashMap<String ,Object> comment1 = (HashMap<String, Object>)reply.get(pos);
                WritingCommentActivity.writeCommentGroup = (String) comment1.get("group");
                WritingCommentActivity.writeCommentUser = (String) comment1.get("writerUID");

            });
            adapter.setOnItemLongClickListener((v, pos) -> {

                HashMap<String, Object> comment12 = (HashMap<String, Object>)reply.get(pos);
                Timestamp time1 = (Timestamp) comment12.get("time");
                Date dtime1 = time1.toDate();
                @SuppressLint("SimpleDateFormat") DateFormat f1 = new SimpleDateFormat("yyMMddhhmmss");
                String stime1 = f1.format(dtime1);
                String writerUID1 = (String) comment12.get("writerUID");
                String commentID = writerUID1.substring(0,8)+ stime1;

                if(writerUID1.equals(userUID)||isManager){
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle("댓글 삭제");
                    builder.setMessage("댓글을 삭제하시겠습니까?");
                    builder.setPositiveButton("삭제", (dialog, which) -> {
                        db.collection("Groups/"+groupID+"/post/"+postID+"/comment/")
                                .document(commentID).delete().addOnSuccessListener(unused -> {
                                    reply.remove(pos);
                                    adapter.notifyDataSetChanged();
                                });
                        DocumentReference reference = db.collection("Users").document(writerUID1);
                        String commentpath = "/Groups/"+groupID+"/post/"+postID+"/comment/"+commentID;
                        reference.update("commentList", FieldValue.arrayRemove(commentpath));

                    });
                    builder.setNegativeButton("취소",null);
                    builder.show();
                }
                else
                    Toast.makeText(view.getContext(), "본인의 댓글이 아닙니다.", Toast.LENGTH_SHORT).show();

            });


        }
        holder.recy_reply.setAdapter(adapter);


        if(selectedPosition == position){
            holder.commentLayout.setBackgroundColor(Color.parseColor("#E0ECF8"));
        }else {
            holder.commentLayout.setBackgroundColor(Color.parseColor("#F6FAFD"));
        }

    }

    public void setBackColor(int pos) {

    }


    @Override
    public int getItemCount() {
       if(commentList==null){
           return 0;
       }
        return commentList.size();
    }
    public class ItemViewHolder extends RecyclerView.ViewHolder {
       TextView name;
       TextView comment;
       TextView time;
       CircleImageView profile;
       RecyclerView recy_reply;
       ImageView btn_reply;
       View commentLayout;
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);


            name = itemView.findViewById(R.id.item_comment_name);
            comment = itemView.findViewById(R.id.item_comment_comment);
            time = itemView.findViewById(R.id.item_comment_time);
            profile = itemView.findViewById(R.id.item_comment_image);
            recy_reply = itemView.findViewById(R.id.recy_comment_reply);
            recy_reply.setLayoutManager(new LinearLayoutManager(recy_reply.getContext()));
            btn_reply = itemView.findViewById(R.id.btn_comment_reply);
            commentLayout = itemView.findViewById(R.id.commentLayout);
            itemView.setOnLongClickListener(view -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION)
                {
                    mLongListener.onItemLongClick(view, pos);
                }
                return true;
            });
            btn_reply.setOnClickListener(view -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION)
                {
                    mListener.onItemClick(commentLayout, pos);
                }
            });

        }

    }
    public void setList(List<HashMap<String, Object>> list, HashMap<String,List> reply) {
        this.commentList = list;
        this.replyList = reply;
    }



    public interface OnItemClickListener
    {
       void onItemClick(View v, int pos);
    }
    public void setOnItemClickListener(OnItemClickListener listener)
    {
        this.mListener = listener;
    }

    public interface OnItemLongClickListener
    {
        void onItemLongClick(View v, int pos);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener)
    {
        this.mLongListener = listener;
    }
}
