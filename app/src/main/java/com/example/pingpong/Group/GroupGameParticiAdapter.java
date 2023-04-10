package com.example.pingpong.Group;

import android.annotation.SuppressLint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.example.pingpong.ProfileDialog;
import com.example.pingpong.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class GroupGameParticiAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List UIDList;
    boolean isManager;
    View itemview;
    String groupID, gameID, managerUID;
    boolean isStringList, accept = false;
    RequestManager requestManager;
    public GroupGameParticiAdapter(List list, boolean manage, String groupID, String gameID, boolean stringList, RequestManager requestManager) {
        this.UIDList = list;
        this.isManager = manage;
        this.groupID = groupID;
        this.gameID = gameID;
        this.isStringList = stringList;
        this.requestManager = requestManager;
    }

    public void setManager(String manager) {
        this.managerUID = manager;
    }

    public void setAccept() {
        this.accept = true;
    }

    ;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        itemview = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_partici, parent, false);
        return new GroupGameParticiAdapter.AHoler(itemview);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String userUID;
        boolean pay = false;
        HashMap info = new HashMap();

        if (isStringList) {
            userUID = (String) UIDList.get(position);
        } else {
            HashMap<String, Object> user = (HashMap<String, Object>) UIDList.get(position);
            userUID = (String) user.get("userUID");
            pay = (boolean) user.get("pay");
            if (isManager) {
                ((AHoler) holder).checkBox.setVisibility(View.VISIBLE);
                ((AHoler) holder).checkBox.setChecked(pay);
                ((AHoler) holder).checkBox.setOnClickListener(view -> {
                    boolean isChecked = ((AHoler) holder).checkBox.isChecked();
                    user.put("pay", isChecked);
                    UIDList.remove(position);
                    UIDList.add(position, user);

                    HashMap<String, Object> map = new HashMap<>();
                    map.put("participants", UIDList);

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("Groups/" + groupID + "/game").document(gameID).update(map);
                });
            }
        }

        //UID로 사용자의 사진, 이름, 이메일 가져오기
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        boolean finalPay = pay;
        db.collection("Users").document(userUID).get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot document = task.getResult();

                    assert document != null;
                    String name = document.getString("name");
                    String email = document.getString("email");
                    String degree = document.getString("degree");
                    if(document.getLong("win") != null) {
                        long win = document.getLong("win");
                        long lose = document.getLong("lose");
                        String percent;
                        if(win == 0 && lose == 0) percent = "0.0";
                        else {
                            double winlose = (double) win / (lose + win);
                            percent = String.valueOf(winlose * 100);
                        }

                        if(percent.length()>5)
                            percent = percent.substring(0,6);


                        info.put("winlose",win+"승 "+lose+"패");
                        info.put("percent",percent+"%");
                    }
                    String team = document.getString("team");
                    String gender = document.getString("gender");
                    Uri uri = Uri.parse(document.getString("image"));


                    info.put("name", name);
                    info.put("degree", degree);
                    info.put("image", uri);
                    info.put("team", team);
                    info.put("gender", gender);

                    ((AHoler) holder).name.setText(name);
                    ((AHoler) holder).email.setText(email);
                    ((AHoler) holder).degree.setText(degree);
                    requestManager.load(uri).into(((AHoler) holder).image);
                });


        if (managerUID != null) {    //관리자에 별표 표시하기
            if (userUID.equals(managerUID)) ((AHoler) holder).manager.setVisibility(View.VISIBLE);
        }

        if (accept) {    //수락버튼 활성화
            ((AHoler) holder).acceptBtn.setVisibility(View.VISIBLE);
            ((AHoler) holder).acceptBtn.setOnClickListener(v -> {
                //디비에 저장
                FirebaseFirestore db1 = FirebaseFirestore.getInstance();
                DocumentReference reference = db1.collection("Groups").document(groupID);
                reference.update("people", FieldValue.arrayUnion(userUID));
                reference.update("waiting", FieldValue.arrayRemove(userUID));
                reference.update("member", FieldValue.increment(1));

                reference = db1.collection("Users").document(userUID);
                reference.update("groupList", FieldValue.arrayUnion(groupID));
                reference.update("waitingList", FieldValue.arrayRemove(groupID));

                //리사이클러뷰 다시
                GroupMemberFragment.notiallrecy(userUID);

                db1.collection("Users").document(userUID).get()
                        .addOnCompleteListener(task -> {
                            DocumentSnapshot document = task.getResult();
                            String token = document.getString("token");
                            new Thread(() -> {
                                JSONObject json = makeFCMJson(token, GroupMainActivity.groupName+"그룹 가입 신청이 수락되었습니다.");
                                new FCMMessage().sendJsonToFCM(json);
                            }).start();
                        });
            });
        }


        ((AHoler) holder).image.setOnClickListener(view -> {
            ProfileDialog dialog = new ProfileDialog(view.getContext(), info);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));  //배경 투명

            dialog.show();
        });


    }

    @Override
    public int getItemCount() {
        if (UIDList == null) {
            return 0;
        }
        return UIDList.size();
    }

    public class AHoler extends RecyclerView.ViewHolder {
        TextView name, email, degree;
        ImageView image;
        CheckBox checkBox;
        FrameLayout manager;
        Button acceptBtn;

        public AHoler(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.partici_name);
            email = itemView.findViewById(R.id.partici_email);
            degree = itemView.findViewById(R.id.partici_degree);
            image = itemView.findViewById(R.id.partici_image);
            checkBox = itemView.findViewById(R.id.partici_pay);
            manager = itemView.findViewById(R.id.partici_manager);
            acceptBtn = itemView.findViewById(R.id.partici_accept_btn);

        }
    }

    private JSONObject makeFCMJson(String token, final String message) {
        // FMC 메시지 생성 start
        JSONObject root = new JSONObject();
        try {

            JSONObject notification = new JSONObject();
            notification.put("body", message);
            notification.put("title", R.string.app_name);
            notification.put("type", "2");//0이 게임, 1이면 글,댓글
            notification.put("groupID", groupID);

            root.put("data", notification);
            root.put("to", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return root;
    }


    public void setList(List list) {
        this.UIDList = list;
    }



}
