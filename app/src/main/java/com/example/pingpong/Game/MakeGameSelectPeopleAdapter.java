package com.example.pingpong.Game;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.pingpong.Group.GroupPostAdapter;
import com.example.pingpong.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MakeGameSelectPeopleAdapter extends RecyclerView.Adapter<MakeGameSelectPeopleAdapter.ItemViewHoleder> {
    private View view;
    public List<HashMap<String, Object>> particiList;
    private int viewType = 0;   // 0: select 기본으로, 1: pay 이면 select, 2: 모두 select true
    private boolean isTeam;
    private List<HashMap<String, Object>> selectedList = new ArrayList();
    private List<HashMap<String, Object>> selectedsaveList = new ArrayList();
    private RequestManager requestManager;
    private GroupPostAdapter.OnItemClickListener mListener;

    public MakeGameSelectPeopleAdapter(List<HashMap<String, Object>> list, RequestManager requestManager, boolean isTeam) {
        this.isTeam = isTeam;
        this.particiList = list;
        int size = particiList.size();
        this.requestManager = requestManager;
        for(int i = 0; i < size; i++) {
            HashMap<String, Object> user = (HashMap<String, Object>) particiList.get(i);
            String userUID = (String) user.get("userUID");

            //UID로 사용자의 사진, 이름, 이메일 가져오기
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            int finalI = i;
            db.collection("Users").document(userUID).get().addOnCompleteListener(task -> {
                DocumentSnapshot document = task.getResult();

                if (document != null) {
                    String name = document.getString("name");
                    String email = document.getString("email");
                    String degree = document.getString("degree");
                    Uri uri = Uri.parse(document.getString("image"));

                    user.put("degree", degree);
                    user.put("name", name);
                    user.put("email", email);
                    user.put("image", uri);

                    particiList.set(finalI, user);

                }

            });
        }
    }

    @NonNull
    @Override
    public ItemViewHoleder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_select_people, parent, false);
        return new MakeGameSelectPeopleAdapter.ItemViewHoleder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHoleder holder, int position) {
        HashMap<String, Object> user = (HashMap<String, Object>) particiList.get(position);
        String userUID = (String) user.get("userUID");
        boolean pay = (boolean) user.getOrDefault("pay", false);

        holder.pay.setChecked(pay);
        if (viewType == 1) {
            holder.select.setChecked(pay);
        } else if (viewType == 2) {
            holder.select.setChecked(true);
        }


        if (user.get("name") == null) {
            //UID로 사용자의 사진, 이름, 이메일 가져오기
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Users").document(userUID).get().addOnCompleteListener(task -> {
                DocumentSnapshot document = task.getResult();

                if (document != null) {
                    String name = document.getString("name");
                    String email = document.getString("email");
                    String degree = document.getString("degree");
                    Uri uri = Uri.parse(document.getString("image"));

                    holder.name.setText(name);
                    holder.email.setText(email);
                    holder.degree.setText(degree);
                    requestManager.load(uri).into(holder.image);

                }

            });

        } else {
            holder.name.setText((String) user.get("name"));
            holder.email.setText((String) user.get("email"));
            holder.degree.setText((String) user.get("degree"));
            requestManager.load((Uri) user.get("image")).into(holder.image);
        }

        if(!isTeam) {
            holder.select.setOnClickListener(view -> {
                if (holder.select.isChecked()) {
                    HashMap<String, Object> info = new HashMap<>();
                    info.put("userUID", userUID);
                    info.put("degree", Integer.parseInt(((String) user.get("degree")).substring(0, 1)));
                    selectedList.add(info);

                    info = new HashMap<>();
                    info.put("userUID", userUID);
                    info.put("pay", pay);
                    selectedsaveList.add(info);
                } else {
                    HashMap<String, Object> info = new HashMap<>();
                    info.put("userUID", userUID);
                    info.put("degree", Integer.parseInt(((String) user.get("degree")).substring(0, 1)));
                    selectedList.remove(info);

                    info = new HashMap<>();
                    info.put("userUID", userUID);
                    info.put("pay", pay);
                    selectedsaveList.remove(info);
                }
            });
        }



    }

    @Override
    public int getItemCount() {
        return particiList.size();
    }

    public class ItemViewHoleder extends RecyclerView.ViewHolder {
        TextView name, email, degree;
        ImageView image;
        CheckBox pay, select;

        public ItemViewHoleder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.partici_name);
            email = itemView.findViewById(R.id.partici_email);
            degree = itemView.findViewById(R.id.partici_degree);
            image = itemView.findViewById(R.id.partici_image);
            pay = itemView.findViewById(R.id.partici_pay);
            select = itemView.findViewById(R.id.partici_select);

            if(isTeam) {
                pay.setVisibility(View.GONE);
                select.setVisibility(View.GONE);
                itemView.setOnClickListener(view -> {
                    int pos = getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        mListener.onItemClick(view, pos);
                    }
                });
            }
            else {
                pay.setVisibility(View.VISIBLE);
                select.setVisibility(View.VISIBLE);
            }

        }

    }

    public void setViewType(int type) {
        this.viewType = type;
        this.selectedList = new ArrayList();
        this.selectedsaveList = new ArrayList<>();
    }

    public void setSelectedList(int type) { //1: pay, 2: all
        this.selectedList = new ArrayList();
        this.selectedsaveList = new ArrayList<>();

        for(HashMap<String, Object> user : particiList) {
            String userUID = (String) user.get("userUID");
            boolean pay = (boolean) user.getOrDefault("pay", false);
            if(type == 1 && pay == false) continue;

            HashMap<String, Object> info = new HashMap<>();
            info.put("userUID", userUID);
            if(user.get("degree").equals("미정")) info.put("degree",9);
            else if(user.get("degree").equals("선수")) info.put("degree",0);
            else {
                info.put("degree", Integer.parseInt(((String) user.get("degree")).substring(0, 1)));
            }
            selectedList.add(info);

            info = new HashMap<>();
            info.put("userUID", userUID);
            info.put("pay", pay);
            selectedsaveList.add(info);
        }
    }

    public List<HashMap<String, Object>> getSelectedList() {
        return selectedList;
    }

    public List<HashMap<String, Object>> getSelectedsaveList() {
        return selectedsaveList;
    }

    public void setOnItemClickListener(GroupPostAdapter.OnItemClickListener listener) {
        this.mListener = listener;
    }

    public void setList(List<HashMap<String, Object>> list) {
        this.particiList = new ArrayList<>();
        particiList.addAll(list);
        this.notifyDataSetChanged();
    }

}
