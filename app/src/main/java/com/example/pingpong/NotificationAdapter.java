package com.example.pingpong;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.example.pingpong.Game.GameHomeActivity;
import com.example.pingpong.Group.GroupHomeFragment;
import com.example.pingpong.Group.GroupMainActivity;
import com.example.pingpong.Group.GroupSignGameActivity;
import com.example.pingpong.Group.WritingCommentActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ItemViewHolder> {
    View view;
    List<HashMap<String, Object>> notificationList;
    RequestManager requestManager;
    TextView notxt;

    public NotificationAdapter(List<HashMap<String, Object>> list, RequestManager requestManager, TextView notxt) {
        this.notificationList = list;
        this.requestManager = requestManager;
        this.notxt = notxt;
    }


    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationAdapter.ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        HashMap<String, Object> info = notificationList.get(position);

        String body = (String) info.get("body");
        String groupID = (String) info.get("groupID");
        String time = (String) info.get("time");

        holder.noticeContent.setText(body);
        holder.time.setText(time);


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.document("Groups/" + groupID).get().addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();
            if(document.exists() && task.isSuccessful()) {
                String name = document.getString("name");
                Uri uri = Uri.parse(document.getString("image"));
                holder.noticeName.setText(name);
                requestManager.load(uri).into(holder.image);
            }
            else {
                holder.noticeName.setText("삭제된 그룹");
                requestManager.load(R.drawable.tournament).into(holder.image);

            }
        });

    }

    public void setList(List<HashMap<String, Object>> list) {
        this.notificationList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView noticeName, noticeContent, time;

        ImageView image, del;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            noticeName = itemView.findViewById(R.id.notice_group_name);
            noticeContent = itemView.findViewById(R.id.notice_content);
            time = itemView.findViewById(R.id.notice_group_time);
            image = itemView.findViewById(R.id.noticeImage);
            del = itemView.findViewById(R.id.btn_notification_del);

            del.setOnClickListener(view -> {
                SQLiteDB sqLiteDB = new SQLiteDB(del.getContext());
                SQLiteDatabase db = sqLiteDB.getReadableDatabase();
                String query = "DELETE FROM Message WHERE time = '" + time.getText() + "' AND body = '" + noticeContent.getText() + "';";
                db.execSQL(query);
                int pos = getBindingAdapterPosition();
                notificationList.remove(pos);
                notifyItemRemoved(pos);

                if (notificationList.size() == 0) notxt.setVisibility(View.VISIBLE);
                else notxt.setVisibility(View.GONE);
            });

            itemView.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                HashMap<String, Object> info = notificationList.get(pos);
                String type = (String) info.get("type");

                GroupMainActivity.groupName = noticeName.getText().toString();

                assert type != null;
                switch (type) {
                    case "1": {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("name", info.get("name"));
                        map.put("text", info.get("text"));

                        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date = null;
                        try {
                            date = formatter.parse((String) Objects.requireNonNull(info.get("timestamp")));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        assert date != null;
                        com.google.firebase.Timestamp stamp = new com.google.firebase.Timestamp(date);

                        map.put("timestamp", stamp);


                        Intent intent = new Intent(view.getContext(), WritingCommentActivity.class);
                        intent.putExtra("groupID", (String) info.get("groupID"));
                        intent.putExtra("postID", (String) info.get("postID"));
                        intent.putExtra("writerUID", (String) info.get("writerUID"));
                        intent.putExtra("postInfo", map);
                        if(info.get("gameID").equals("true"))
                            intent.putExtra("isNotice", true);

                        if (((String) Objects.requireNonNull(info.get("body"))).contains("공지")) {
                            intent.putExtra("isNotice", true);
                        }
                        view.getContext().startActivity(intent);
                        break;
                    }
                    case "0": {
                        Intent intent = new Intent(view.getContext(), GroupSignGameActivity.class);
                        intent.putExtra("gameID", (String) info.get("gameID"));
                        intent.putExtra("groupID", (String) info.get("groupID"));
                        intent.putExtra("managerUID", (String) info.get("managerUID"));

                        view.getContext().startActivity(intent);
                        break;
                    }
                    case "2":

                        // 그룹 신청, 수락
                        break;
                    case "3":
                        Intent intent = new Intent(view.getContext(), GameHomeActivity.class);
                        intent.putExtra("gameID", (String) info.get("gameID"));
                        intent.putExtra("groupID", (String) info.get("groupID"));
                        intent.putExtra("managerUID", (String) info.get("managerUID"));
                        String gametype =  (String) info.get("postID");
                        intent.putExtra("gameType", Long.parseLong(gametype));

                        view.getContext().startActivity(intent);
                        break;
                }
            });
        }
    }
}
