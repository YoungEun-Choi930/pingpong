package com.example.pingpong.SearchSetting;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.pingpong.Group.GroupInfoActivity;
import com.example.pingpong.LoginActivity;
import com.example.pingpong.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainSearchFragment extends Fragment {
    SearchView searchView;
    SearchView.SearchAutoComplete autoComplete;
    RecyclerView recy;
    TextView textView;
    List<HashMap<String, Object>> groupList;
    MainSearchAdapter adapter;
    ArrayAdapter<String> autoAdapter;
    ArrayList<String> autoList = new ArrayList<>();
    private final String userUID = LoginActivity.userUID;
    public static List<String> userWaitingList;
    RequestManager requestManager;

    public MainSearchFragment() {
        groupList = new ArrayList();
        setgroupList(false);

        //DB에서 내가 신청중 그룹목록 들고오기
        //신청보내면 보낸곳에서 list update 하니까 첨에 한번만 하면 됨
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(userUID)
                .get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    userWaitingList = (List<String>) document.getData().get("waitingList");
                    if (userWaitingList == null) userWaitingList = new ArrayList();

                });

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestManager = Glide.with(this);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_search, container, false);

        textView = view.findViewById(R.id.search_text);
        textView.setText("이런 그룹은 어때요");
        searchView = view.findViewById(R.id.search_editText);
        autoComplete = searchView.findViewById(R.id.search_src_text);
        autoAdapter = new ArrayAdapter<>(getContext(), android.R.layout.select_dialog_item, autoList);
        autoComplete.setThreshold(1);
        autoComplete.setAdapter(autoAdapter);

        autoComplete.setOnItemClickListener((adapterView, view1, i, l) -> {
            String str = (String) adapterView.getItemAtPosition(i);
            autoComplete.setText(str);
            autoComplete.setSelection(autoComplete.length());
        });

        searchView.setOnClickListener(view12 -> searchView.setIconified(false));
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {    //입력받은 문자열을 처리
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {  //입력란의 문자열이 바뀔 때
                return true;
            }
        });


        recy = view.findViewById(R.id.search_recy);
        adapter = new MainSearchAdapter(groupList, textView, requestManager);
        recy.setAdapter(adapter);

        adapter.setOnItemClickListener((v, pos) -> {
            HashMap<String, Object> groupInfo = (HashMap) adapter.filterList.get(pos);
            String groupID = (String) groupInfo.get("groupID");

            Intent intent = new Intent(view.getContext(), GroupInfoActivity.class);
            intent.putExtra("groupID", groupID);
            intent.putExtra("userUID", userUID);
            intent.putExtra("groupInfo", groupInfo);
            intent.putExtra("state", false);

            startActivity(intent);

        });

        //새로고침
        SwipeRefreshLayout mSwipe = view.findViewById(R.id.search_layout);
        mSwipe.setOnRefreshListener(() -> {
            setgroupList(true);

            final Handler handler = new Handler();
            handler.postDelayed(() -> mSwipe.setRefreshing(false), 1000);    //이 시간으로 정확하게 되는건지는 모르겠음 잘
        });


        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setgroupList(boolean notify) {
        groupList = new ArrayList();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Groups").orderBy("member", Query.Direction.DESCENDING).get()
                .addOnCompleteListener(task -> {
                    QuerySnapshot query = task.getResult();
                    assert query != null;
                    for (DocumentSnapshot document : query.getDocuments()) {

                        HashMap<String, Object> map = new HashMap<>();
                        map.put("groupID", document.getId());
                        map.put("groupName", document.getString("name"));
                        map.put("comment", document.getString("comment"));
                        map.put("member", document.getLong("member"));
                        map.put("manager", document.getString("manager"));
                        map.put("type", document.getLong("type"));
                        map.put("openingDate", document.getTimestamp("openingDate"));
                        map.put("Uri", Uri.parse(document.getString("image")));
                        map.put("buisnessName", document.getString("buisnessName"));
                        map.put("buisnessOwner", document.getString("buisnessOwner"));
                        map.put("buisnessAddress", document.getString("buisnessAddress"));
                        map.put("buisnessPhonenum", document.getString("buisnessPhonenum"));
                        map.put("buisnessNumber", document.getString("buisnessNumber"));
                        map.put("latitude", document.getDouble("latitude"));
                        map.put("longitude", document.getDouble("longitude"));

                        groupList.add(map);


                    }
                    if (notify) {
                        adapter.setList(groupList);
                        adapter.notifyDataSetChanged();
                        System.out.println(groupList.size()+"11size");
                        System.out.println("111111111111111111notify");
                    }

                    //그룹이 삭제된 경우도 있으니까 해줘야하는데 왜 안되는것인가
                    //groupList = newlist;

                });

    }

}