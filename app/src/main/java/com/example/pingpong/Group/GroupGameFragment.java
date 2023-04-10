package com.example.pingpong.Group;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pingpong.Game.GameHomeActivity;
import com.example.pingpong.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupGameFragment extends Fragment {
    String groupID, manager, userUID;
    RecyclerView signRecy, proRecy, endRecy;
    GroupGameAdapter signAdapt, proAdapt, endAdapt;
    List signGameList, proGameList, endGameList;


    public GroupGameFragment(String groupID, String manager, String userUID) {
        this.groupID = groupID;
        this.userUID = userUID;
        this.signGameList = new ArrayList();
        this.proGameList = new ArrayList();
        this.endGameList = new ArrayList();
        this.manager = manager;


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //리사이클러뷰 어댑터 설정
        View view = inflater.inflate(R.layout.fragment_group_game, container, false);
        setHasOptionsMenu(true);


        //신청중인 게임
        signAdapt = new GroupGameAdapter(signGameList);
        signRecy = view.findViewById(R.id.recy_gamelist_sign);
        signRecy.setAdapter(signAdapt);

        //진행중인 게임
        proAdapt = new GroupGameAdapter(proGameList);
        proRecy = view.findViewById(R.id.recy_gamelist_progress);
        proRecy.setAdapter(proAdapt);

        //종료된 게임
        endAdapt = new GroupGameAdapter(endGameList);
        endRecy = view.findViewById(R.id.recy_gamelist_end);
        endRecy.setAdapter(endAdapt);


        //신청중 게임 클릭시 신청 화면으로 이동
        signAdapt.setOnItemClickListener(new GroupGameAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                String gameID = (String) ((HashMap) signGameList.get(pos)).get("id");
                System.out.println(gameID+"왜잘렸니?");

                Intent intent = new Intent(view.getContext(), GroupSignGameActivity.class);
                intent.putExtra("groupID", groupID);
                intent.putExtra("gameID", gameID);
                intent.putExtra("managerUID", manager);
                intent.putExtra("userUID", userUID);
                startActivity(intent);
            }

        });

        //진행중게임
        proAdapt.setOnItemClickListener(new GroupGameAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                String gameID = (String) ((HashMap) proGameList.get(pos)).get("id");
                Long type = (Long) ((HashMap) proGameList.get(pos)).get("gameType");

                Intent intent = new Intent(view.getContext(), GameHomeActivity.class);
                intent.putExtra("groupID", groupID);
                intent.putExtra("gameID", gameID);
                intent.putExtra("managerUID", manager);
                intent.putExtra("userUID", userUID);
                intent.putExtra("gameType", type);

                startActivity(intent);
            }

        });

        //진행중게임
        endAdapt.setOnItemClickListener(new GroupGameAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                String gameID = (String) ((HashMap) endGameList.get(pos)).get("id");
                Long type = (Long) ((HashMap) endGameList.get(pos)).get("gameType");

                Intent intent = new Intent(view.getContext(), GameHomeActivity.class);
                intent.putExtra("groupID", groupID);
                intent.putExtra("gameID", gameID);
                intent.putExtra("managerUID", manager);
                intent.putExtra("userUID", userUID);
                intent.putExtra("gameType", type);

                startActivity(intent);
            }

        });

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume() {
        super.onResume();
        //리스트 가져오기
        signGameList = new ArrayList();
        proGameList = new ArrayList();
        endGameList = new ArrayList();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Groups").document(groupID).collection("game").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot query = task.getResult(); //게임 문서 목록들

                        assert query != null;
                        for (DocumentSnapshot document : query.getDocuments()) {
                            String id = document.getId();
                            long state = (Long) document.getData().get("state");
                            String name = (String) document.getData().get("gamename");
                            Timestamp date = (Timestamp) document.getData().get("startdate");


                            HashMap<String, Object> map = new HashMap<>();
                            map.put("id", id);
                            map.put("name", name);
                            map.put("date", date);
                            if (document.getData().get("gameType") != null) {
                                long gameType = (Long) document.getData().get("gameType");
                                map.put("gameType", gameType);
                            }

                            switch ((int) state) {
                                case 0:
                                    signGameList.add(map);
                                    break;
                                case 1:
                                    proGameList.add(map);
                                    break;
                                case -1:
                                    endGameList.add(map);
                            }
                        }
                        signAdapt.setList(signGameList);
                        signAdapt.notifyDataSetChanged();
                        proAdapt.setList(proGameList);
                        proAdapt.notifyDataSetChanged();
                        endAdapt.setList(endGameList);
                        endAdapt.notifyDataSetChanged();

                    }
                });

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.creatgame, menu);
        MenuItem m = menu.findItem(R.id.menu_make_game);
        if (manager.equals(userUID))
            m.setVisible(true);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_make_game) {
            Intent gameIntent = new Intent(getContext(), MakeNewJoinActivity.class);
            gameIntent.putExtra("groupID", groupID);
            gameIntent.putExtra("userUID", userUID);
            startActivity(gameIntent);
        }
        return super.onOptionsItemSelected(item);
    }


}