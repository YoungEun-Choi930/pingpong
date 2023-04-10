package com.example.pingpong.Main;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.pingpong.Group.GroupMainActivity;
import com.example.pingpong.LoginActivity;
import com.example.pingpong.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainGroupFragment extends Fragment {
    RecyclerView recyclerView;
    private MainGroupAdapter adapter;
    public static List<String> groupStrList = new ArrayList<>();
    public static HashMap<String, HashMap<String, Object>> groupInfoList = new HashMap<>();
    LinearLayout newGroup;
    RequestManager requestManager;
    SwipeRefreshLayout mSwipe;
    SwipeRefreshLayout.OnRefreshListener listener;

    public MainGroupFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestManager = Glide.with(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //리사이클러뷰 어댑터 설정
        View view = inflater.inflate(R.layout.fragment_main_group, container, false);
        adapter = new MainGroupAdapter(groupStrList, groupInfoList, requestManager);
        recyclerView = view.findViewById(R.id.recy_main_grouplist);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this.getContext(), 2));


        //새로고침
        mSwipe = view.findViewById(R.id.main_group_layout);
        listener = () -> {
            setList();

            final Handler handler = new Handler();
            handler.postDelayed(() -> {
                mSwipe.setRefreshing(false);
                notifyAdapter();
            }, 1000);    //이 시간으로 정확하게 되는건지는 모르겠음 잘
        };

        mSwipe.setOnRefreshListener(listener);


        //그룹 클릭시 그룹 홈화면으로 이동
        adapter.setOnItemClickListener((v, pos) -> {

            String groupID = groupStrList.get(pos);
            HashMap<String, Object> groupInfo = groupInfoList.get(groupID);

            System.out.println("------------------------------ 그룹 클릭 ------------------------------");
            System.out.println("groupID: " + groupID);
            System.out.println("groupInfo: " + groupInfo);

            Intent intent = new Intent(view.getContext(), GroupMainActivity.class);

            intent.putExtra("groupID", groupID);
            intent.putExtra("groupInfo", groupInfo);

            startActivity(intent);
        });

        newGroup = view.findViewById(R.id.main_group_make_btn);
        newGroup.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MakeNewBuisnessActivity.class);
            startActivity(intent);
        });


        return view;
    }

    public static void setList() {
        //DB에서 내가 가입한 그룹목록 들고오기
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(LoginActivity.userUID)
                .get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    groupStrList = (List<String>) document.getData().get("groupList");
                    if (groupStrList == null) groupStrList = new ArrayList<>();

                    int size = groupStrList.size();
                    for (int index = 0; index < size; index++) {
                        String groupID = groupStrList.get(index);
                        System.out.println(groupID + "그룹아이디");
                        HashMap<String, Object> info = new HashMap<>();
                        db.collection("Groups").document(groupID).get().addOnCompleteListener(task1 -> {
                            DocumentSnapshot document1 = task1.getResult();

                            if(document1.exists()) {

                                info.put("groupName", document1.getString("name"));
                                info.put("managerUID", document1.getString("manager"));
                                info.put("member", document1.getLong("member"));
                                info.put("image", Uri.parse(document1.getString("image")));

                                groupInfoList.put(groupID, info);
                            }

                        });
                    }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void notifyAdapter() {
        adapter.setList(groupStrList, groupInfoList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSwipe.setRefreshing(true);
        listener.onRefresh();
    }
}