package com.example.pingpong.Group;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.example.pingpong.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.ItemViewHolder> {
    List<HashMap<String, Object>> noticeList;
    View view;
    private final String groupID, manager, userUID;
    RequestManager requestManager;

    NoticeAdapter(List<HashMap<String, Object>> list, String groupID, String manager, String userUID, RequestManager requestManager) {
        noticeList = list;
        this.groupID = groupID;
        this.manager = manager;
        this.userUID = userUID;
        this.requestManager = requestManager;
    }

    @NonNull
    @Override
    public NoticeAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notice, parent, false);
        return new ItemViewHolder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull NoticeAdapter.ItemViewHolder holder, int position) {
        HashMap<String, Object> notice = (HashMap<String, Object>) noticeList.get(position);
        String writer = (String) notice.get("writerUID");
        String noticeID = (String) notice.get("noticeID");
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Users").document(writer).get().addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();
            if (document != null) {
                String name = document.getString("name");
                holder.name.setText(name);
            }


        });

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference reference = storage.getReference("profile_img/profile_" + writer + ".jpg");
        reference.getDownloadUrl().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null) {
                    requestManager.load(task.getResult()).into(((ItemViewHolder) holder).profile);
                }
            }
        });

        Timestamp time = (Timestamp) notice.get("time");
        Date date = time.toDate();
        @SuppressLint("SimpleDateFormat") DateFormat format = new SimpleDateFormat("yyyy년 MM월 dd일");
        String strdate = format.format(date);
        String contents = (String) notice.get("contents");

        holder.time.setText(strdate);
        holder.text.setText(contents);

        if (manager.equals(userUID))
            holder.del.setVisibility(View.VISIBLE);

        holder.del.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
            builder.setMessage("삭제하시겠습니까?");

            builder.setPositiveButton("확인", (dialogInterface, i) -> {
                noticeList.remove(notice);
                notifyDataSetChanged();

                db.collection("Groups").document(groupID).collection("notice").document(noticeID).delete();


                Toast.makeText(view.getContext(), "삭제 성공", Toast.LENGTH_SHORT).show();
            });

            builder.setNegativeButton("취소", (dialogInterface, i) -> {
            });
            builder.show();
        });
    }

    @Override
    public int getItemCount() {
        return noticeList.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        TextView name;
        TextView time;
        CircleImageView profile;
        ImageView del;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.item_notice_text);
            name = itemView.findViewById(R.id.item_notice_name);
            time = itemView.findViewById(R.id.item_notice_time);
            profile = itemView.findViewById(R.id.item_notice_profile);
            del = itemView.findViewById(R.id.btn_notice_del);
        }
    }
}
