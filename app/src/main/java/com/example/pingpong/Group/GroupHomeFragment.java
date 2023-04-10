package com.example.pingpong.Group;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.pingpong.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GroupHomeFragment extends Fragment {
    private String groupID, userUID, manager;
    ViewPager2 viewPager2;
    RecyclerView recy_post;
    TabLayout tabLayout;
    ViewPagerAdapter viewPagerAdapter;
    GroupPostAdapter postAdapter;
    List<HashMap<String, Object>> noticeList;
    List<HashMap<String, Object>> groupPostList;
    ImageView groupImageView;
    TextView more;
    View view;
    RequestManager requestManager;

    public GroupHomeFragment(String groupID, String userUID, String manager) {
        this.userUID = userUID;
        this.groupID = groupID;
        this.noticeList = new ArrayList();
        this.groupPostList = new ArrayList();
        this.manager = manager;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("새로고침");
        requestManager = Glide.with(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_group_home, container, false);
        setHasOptionsMenu(true);

        groupImageView = view.findViewById(R.id.group_home_image);

        this.groupPostList = new ArrayList();
        this.noticeList = new ArrayList();

        SwipeRefreshLayout mSwipeRefreshLayout = view.findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            onResume();
            final Handler handler = new Handler();
            handler.postDelayed(() -> mSwipeRefreshLayout.setRefreshing(false), 500);
        });


//////////////////////////공지뷰페이저///////////////////
        viewPagerAdapter = new ViewPagerAdapter(noticeList, manager, requestManager);
        viewPager2 = view.findViewById(R.id.vp_grouphome_notice);
        viewPager2.setAdapter(viewPagerAdapter);
        viewPager2.setClipToPadding(false);
        tabLayout = view.findViewById(R.id.tl);
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {

        }).attach();

        more = view.findViewById(R.id.group_home_more_notice);


///////////////////////공지클릭////////////////////
        viewPagerAdapter.setOnItemClickListener((v, pos) -> {
            if (v.getId() == R.id.linear_notice) {
                String noticeID = (String) ((HashMap<String, Object>) noticeList.get(pos)).get("noticeID");
                HashMap<String, Object> info = (HashMap<String, Object>) viewPagerAdapter.map.get(noticeID);
                assert info != null;
                boolean isGame = (boolean) info.get("isGame");
                if (isGame) {
                    String writerUID = (String) info.get("writerUID");
                    Intent signintent = new Intent(getContext(), GroupSignGameActivity.class);
                    signintent.putExtra("groupID", groupID);
                    signintent.putExtra("gameID", noticeID);
                    signintent.putExtra("managerUID", manager);
                    signintent.putExtra("userUID", userUID);
                    signintent.putExtra("writerUID", writerUID);

                    startActivity(signintent);

                } else {
                    String writerUID = (String) info.get("writerUID");
                    Intent intent = new Intent(getContext(), WritingCommentActivity.class);
                    if (writerUID.equals(userUID))
                        intent.putExtra("isWriter", true);
                    if (manager.equals(userUID))
                        intent.putExtra("isManager", true);
                    intent.putExtra("postInfo", info);//
                    intent.putExtra("groupID", groupID);
                    intent.putExtra("postID", noticeID);
                    intent.putExtra("userUID", userUID);
                    intent.putExtra("managerUID", manager);
                    intent.putExtra("isNotice", true);
                    intent.putExtra("writerUID", writerUID);
                    startActivity(intent);

                }
            }
        });


        ///////////////////포스트리사이클러뷰/////////////////////////////
        recy_post = view.findViewById(R.id.recy_grouphome_post);
        postAdapter = new GroupPostAdapter(groupPostList, getActivity(), groupID, userUID, manager, requestManager);
        recy_post.setAdapter(postAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recy_post.setLayoutManager(layoutManager);
        //////////////////////DB에서 포스트 가져오기////////////////


        /////////공지모아보기 클릭///////////////////////
        more.setOnClickListener(view1 -> {
            Intent intent = new Intent(getContext(), NoticeActivity.class);
            intent.putExtra("noticeList", (Serializable) noticeList);
            intent.putExtra("groupID", groupID);
            intent.putExtra("manager", manager);
            intent.putExtra("userUID", userUID);
            startActivity(intent);
        });


        ////포스트의 더보기버튼 클릭했을때
        postAdapter.setOnItemClickListener((v, pos) -> {
            String postID = (String) groupPostList.get(pos).get("postID");
            HashMap<String, Object> info = (HashMap<String, Object>) postAdapter.info.get(postID);
            assert info != null;
            String[] more;
            String writer = (String) info.get("writerUID");

            boolean isManager = manager.equals(userUID);
            boolean isWriter = writer.equals(userUID);
            if (isManager) {
                if (isWriter)
                    more = new String[]{"게시글 수정", "게시글 삭제", "공지 등록"};
                else
                    more = new String[]{"게시글 삭제", "공지 등록"};
            } else {
                more = new String[]{"게시글 수정", "게시글 삭제"};
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

            builder.setItems(more, (dialogInterface, i) -> {
                if (isManager && !isWriter)
                    i++;
                switch (i) {
                    case 0: ////수정하는경우
                        Intent intent = new Intent(getContext(), WritingPost.class);
                        intent.putExtra("text", (String) info.get("text"));
                        intent.putExtra("groupID", groupID);
                        intent.putExtra("postID", postID);
                        intent.putExtra("userUID", userUID);
                        intent.putExtra("modify", true);
                        intent.putExtra("manager", manager);
                        view.getContext().startActivity(intent.addFlags(FLAG_ACTIVITY_NEW_TASK));
                        updatePost();
                        updateNotice();
                        break;
                    case 1: ///삭제하는경우
                        new Post().deletePost(groupID, postID, writer);
                        Toast.makeText(getContext(), "게시글 삭제 완료", Toast.LENGTH_SHORT).show();
                 
                        updatePost();
                        updateNotice();

                        break;
                    case 2:// 공지 등록
                        FirebaseFirestore db1 = FirebaseFirestore.getInstance();
                        Map<String, Object> post = new HashMap<>();

                        post.put("contents", (String) info.get("text"));
                        post.put("writerUID", writer);
                        post.put("time", (Timestamp) info.get("timestamp"));
                        post.put("isGame", false);
                        post.put("noticeTime", Timestamp.now());

                        db1.collection("Groups").document(groupID).collection("notice").document(postID)
                                .set(post)
                                .addOnSuccessListener(unused -> {
                                    updateNotice();
                                    Toast.makeText(getContext(), "공지로 등록되었습니다.", Toast.LENGTH_SHORT).show();

                                        String groupname = Objects.requireNonNull(Objects.requireNonNull(((GroupMainActivity) getActivity()).getSupportActionBar()).getTitle()).toString();
                                        @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        String timestamp = df.format(((Timestamp) info.get("timestamp")).toDate());
                                        new Thread(() -> {
                                            JSONObject json = makeFCMJson(groupname + "그룹에 새로운 공지가 등록되었습니다.", postID, (String) info.get("name"), (String) info.get("text"), timestamp);
                                            new FCMMessage().sendJsonToFCM(json);
                                        }).start();

                                })
                                .addOnFailureListener(thr -> {
                                });
                        db1.collection("Groups").document(groupID).collection("post").document(postID)
                                .set(post);

                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });

        return view;

    }

    @SuppressLint("NotifyDataSetChanged")
    public void updatePost() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        groupPostList = new ArrayList();
        db.collection("Groups").document(groupID).collection("post").orderBy("time").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot query = task.getResult(); //

                assert query != null;
                for (DocumentSnapshot document : query.getDocuments()) {
                    String postID = document.getId();
                    HashMap<String,Object> map = new HashMap<>();
                    map.put("postID", postID);
                    map.put("writerUID", document.get("writerUID"));
                    map.put("time",document.get("time"));
                    map.put("content",document.get("contents"));
                    System.out.println(postID + "포스트이름");

                    groupPostList.add(0, map);
                }
                postAdapter.setList(groupPostList);
                postAdapter.notifyDataSetChanged();
            }
        });
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    public void updateNotice() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        noticeList = new ArrayList();
        db.collection("Groups").document(groupID).collection("notice").orderBy("noticeTime").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot query = task.getResult();
                assert query != null;
                for (DocumentSnapshot document : query.getDocuments()) {
                    String writer = document.getString("writerUID");
                    String contents = document.getString("contents");
                    Timestamp time = (Timestamp) document.getData().get("time");
                    boolean isGame = document.getBoolean("isGame");
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("noticeID", document.getId());
                    map.put("writerUID", writer);
                    map.put("contents", contents);
                    map.put("time", time);
                    map.put("isGame", isGame);
                    noticeList.add(0, map);
                }
                viewPagerAdapter.setList(noticeList);
                viewPagerAdapter.notifyDataSetChanged();
                more.setText(Integer.toString(noticeList.size()));
            }
        });
    }

    public void updateGroupImage() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Groups").document(groupID).get().addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();
            assert document != null;
            String image = (String) document.get("image");
            requestManager.load(image).into(groupImageView);
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        updateGroupImage();
        updatePost();
        updateNotice();

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.grouphome, menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_write_post) {
            Intent intent = new Intent(getContext(), WritingPost.class);
            intent.putExtra("groupID", groupID);
            intent.putExtra("userUID", userUID);
            intent.putExtra("manager", manager);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private JSONObject makeFCMJson(final String message, String noticeID, String name, String text, String timestamp) {
        // FMC 메시지 생성 start
        // FMC 메시지 생성 start
        JSONObject root = new JSONObject();
        try {

            JSONObject notification = new JSONObject();
            notification.put("body", message);
            notification.put("title", getString(R.string.app_name));
            notification.put("type", "1");//0이 게임, 1이면 글,댓글
            notification.put("groupID", groupID);
            notification.put("postID", noticeID);
            notification.put("writerUID", userUID);

            notification.put("name", name);
            notification.put("text", text);

            notification.put("timestamp", timestamp);

            root.put("data", notification);
            root.put("to", "/topics/" + groupID);
            System.out.println("알림보내기");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return root;
    }



}