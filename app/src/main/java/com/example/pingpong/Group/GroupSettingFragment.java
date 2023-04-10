package com.example.pingpong.Group;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.pingpong.Main.MakeNewBuisnessActivity;
import com.example.pingpong.Main.MakeNewGroupActivity;
import com.example.pingpong.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class GroupSettingFragment extends Fragment implements View.OnClickListener {

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch groupNotice;
    TextView groupLeave, groupInfo, groupDel, groupInfoChange, buisnessInfoChange;
    LinearLayout managerBtn;
    String userUID, groupID;
    HashMap<String, Object> info;

    public static SharedPreferences checked; //스위치 값을 저장
    SharedPreferences.Editor editor; //스위치 값 수정

    private boolean result1 = true, result2 = true, result3 = true, result4 = true,
            result5 = true, result6 = true, result7 = true;

    public GroupSettingFragment(String groupID, String userUID) {
        this.groupID = groupID;
        this.userUID = userUID;
        info = new HashMap<>();

        getInfo();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_group_setting, container, false);

        groupNotice = view.findViewById(R.id.switch_groupNotice);
        groupNotice.setOnClickListener(this);
        groupLeave = view.findViewById(R.id.btn_group_leave);
        groupLeave.setOnClickListener(this);
        groupInfo = view.findViewById(R.id.btn_group_info);
        groupInfo.setOnClickListener(this);


        managerBtn = view.findViewById(R.id.group_setting_manager);
        if (userUID.equals(info.get("manager"))) {
            groupDel = view.findViewById(R.id.btn_group_del);
            groupDel.setOnClickListener(this);
            groupInfoChange = view.findViewById(R.id.btn_change_group_info);
            groupInfoChange.setOnClickListener(this);
            buisnessInfoChange = view.findViewById(R.id.btn_change_buisness_info);
            buisnessInfoChange.setOnClickListener(this);

            groupLeave.setVisibility(View.GONE);
            managerBtn.setVisibility(View.VISIBLE);
        }
        checked = requireActivity().getSharedPreferences(groupID, Activity.MODE_PRIVATE);
        editor = checked.edit();
        boolean switch_state = checked.getBoolean("switch_state", true);
        System.out.println(switch_state + "스위치스테이트");
        groupNotice.setChecked(switch_state);
        groupNotice.setOnCheckedChangeListener((compoundButton, isCheked) -> {
            // 안드로이드 10 이상
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                if (isCheked) {
                    FirebaseMessaging.getInstance().subscribeToTopic(groupID);

                    editor.putBoolean("switch_state", true);
                    editor.commit();
                } else {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(groupID);
                    editor.putBoolean("switch_state", false);
                    editor.commit();
                }
            }
            // 안드로이드 10 미만
            else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);
                if (isCheked) {
                    NotificationManager manager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        NotificationChannel channel = manager.getNotificationChannel("ChannerID");
                        channel.setAllowBubbles(false);
                    }
                    FirebaseMessaging.getInstance().subscribeToTopic(groupID);
                    editor.putBoolean("switch_state", true);

                } else {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(groupID);
                    editor.putBoolean("switch_state", false);
                    editor.commit();
                }
            }
        });


        return view;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btn_group_leave:
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(view.getContext());
                alt_bld.setMessage("그룹을 탈퇴하시겠습니까?").setCancelable(false)
                        .setPositiveButton("네", (dialogInterface, i) -> {
                            //db
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            DocumentReference groupDocument = db.document("Groups/" + groupID);
                            DocumentReference userDocument = db.document("Users/" + userUID);
                            groupDocument.update("people", FieldValue.arrayRemove(userUID));
                            groupDocument.update("member", FieldValue.increment(-1));
                            userDocument.update("groupList", FieldValue.arrayRemove(groupID));
                            Toast myToast = Toast.makeText(getContext(),
                                    "탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT);
                            myToast.show();

                            //화면 끝
                            requireActivity().finish();
                        }).setNegativeButton("아니오", (dialogInterface, i) -> dialogInterface.cancel());
                AlertDialog alert = alt_bld.create();
                alert.setTitle("그룹 탈퇴");
                alert.show();

                break;
            case R.id.btn_group_info:
                Intent intent = new Intent(getContext(), GroupInfoActivity.class);
                intent.putExtra("userUID", userUID);
                intent.putExtra("state", true);
                intent.putExtra("groupInfo", info);
                startActivity(intent);
                break;


            //매니저
            case R.id.btn_change_group_info:
                Intent changeGroupIntent = new Intent(getContext(), MakeNewGroupActivity.class);
                changeGroupIntent.putExtra("ischange", true);
                changeGroupIntent.putExtra("groupInfo", info);

                startActivity(changeGroupIntent);
                break;
            case R.id.btn_change_buisness_info:
                Intent changeBuisnessIntent = new Intent(getContext(), MakeNewBuisnessActivity.class);
                changeBuisnessIntent.putExtra("ischange", true);
                changeBuisnessIntent.putExtra("groupInfo", info);

                startActivity(changeBuisnessIntent);
                break;
            case R.id.btn_group_del:
                AlertDialog.Builder alt_del = new AlertDialog.Builder(view.getContext());
                alt_del.setMessage("그룹을 삭제하시겠습니까? \n그룹의 모든 데이터가 삭제되며 이 작업은 복구할 수 없습니다.").setCancelable(false)
                        .setPositiveButton("네", (dialogInterface, i) -> {
                            //db
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            DocumentReference groupDocument = db.document("Groups/" + groupID);
                            groupDocument.delete();


                            new Thread(() -> {
                                System.out.println("-------------------- thread start -------------------");
                                while (result1 || result2 || result3 || result4 || result5 || result6 || result7) {

                                }

                                //끝나면 모두 다 false가 되면
                                // toast message
                                makeToastMessage();
                                //화면 끝
                                requireActivity().finish();
                                System.out.println("-------------------- thread finish -------------------");

                            }).start();


                            /////////// post collection 삭제
                            groupDocument.collection("post").get().addOnCompleteListener(task -> {
                                QuerySnapshot queryDocumentSnapshots = task.getResult();
                                if (task.isSuccessful() && queryDocumentSnapshots.size() != 0) {
                                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                        String postID = documentSnapshot.getId();
                                        groupDocument.collection("post").document(postID).collection("comment").get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                                            for (QueryDocumentSnapshot documentSnapshot1 : queryDocumentSnapshots1) {
                                                groupDocument.collection("post").document(postID)
                                                        .collection("comment").document(documentSnapshot1.getId()).delete();
                                            }
                                            result1 = false;
                                            System.out.println("-------------------- result1 -------------------");

                                        });
                                        groupDocument.collection("post").document(postID).delete();
                                    }
                                } else result1 = false;
                            });

                            /////////// notice collection 삭제
                            groupDocument.collection("notice").get().addOnCompleteListener(task -> {
                                QuerySnapshot queryDocumentSnapshots = task.getResult();
                                if (task.isSuccessful() && queryDocumentSnapshots.size() != 0) {
                                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                        groupDocument.collection("notice").document(documentSnapshot.getId()).delete();
                                    }
                                }
                                result2 = false;
                                System.out.println("-------------------- result2 -------------------");

                            });


                            /////////// game collection 삭제
                            groupDocument.collection("game").get().addOnCompleteListener(task -> {
                                QuerySnapshot queryDocumentSnapshots = task.getResult();
                                if (task.isSuccessful() && queryDocumentSnapshots.size() != 0) {

                                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                        String gameID = documentSnapshot.getId();
                                        groupDocument.collection("game").document(gameID).collection("league").get().addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                QuerySnapshot queryDocumentSnapshots1 = task1.getResult();
                                                for (QueryDocumentSnapshot documentSnapshot1 : queryDocumentSnapshots1) {
                                                    groupDocument.collection("game").document(gameID)
                                                            .collection("league").document(documentSnapshot1.getId()).delete();
                                                }
                                            }
                                            System.out.println("-------------------- result3 -------------------");
                                            result3 = false;

                                        });
                                        groupDocument.collection("game").document(gameID).collection("schedule").get().addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                QuerySnapshot queryDocumentSnapshots1 = task1.getResult();
                                                for (QueryDocumentSnapshot documentSnapshot1 : queryDocumentSnapshots1) {
                                                    groupDocument.collection("game").document(gameID)
                                                            .collection("schedule").document(documentSnapshot1.getId()).delete();
                                                }
                                            }
                                            System.out.println("-------------------- result4 -------------------");
                                            result4 = false;
                                        });
                                        groupDocument.collection("game").document(gameID).delete();
                                    }

                                } else {
                                    result3 = false;
                                    result4 = false;
                                    System.out.println("-------------------- result34 -------------------");

                                }
                            });

                            //모든 사용자의 groupList에서 그룹 지우기
                            db.collection("Users").whereArrayContains("groupList", groupID).get().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    QuerySnapshot queryDocumentSnapshots = task.getResult();
                                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                                        String uid = document.getId();

                                        List<String> post = (List<String>) document.get("postList");
                                        if (post != null) {
                                            System.out.println(post + "포스트아이디");
                                            post.removeIf(str -> str.contains(groupID));
                                        }

                                        List<String> comment = (List<String>) document.get("commentList");
                                        if (comment != null) {
                                            comment.removeIf(str -> str.contains(groupID));
                                        }

                                        // 다시 저장
                                        DocumentReference userDocument = db.collection("Users").document(uid);
                                        userDocument.update("groupList", FieldValue.arrayRemove(groupID));
                                        userDocument.update("postList", post);
                                        userDocument.update("commentList", comment);

                                    }
                                }
                                result5 = false;
                                System.out.println("-------------------- result5 -------------------");


                            });

                            //모든 사용자의 waitingList에서 그룹 지우기
                            db.collection("Users").whereArrayContains("waitingList", groupID).get().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    QuerySnapshot queryDocumentSnapshots = task.getResult();
                                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                                        String uid = document.getId();
                                        DocumentReference userDocument = db.collection("Users").document(uid);

                                        userDocument.update("groupList", FieldValue.arrayRemove(groupID));
                                    }

                                }
                                result6 = false;
                                System.out.println("-------------------- result6 -------------------");
                            });


                            StorageReference mStorageRef;
                            mStorageRef = FirebaseStorage.getInstance().getReference();
                            mStorageRef.child("group_img").child(groupID).listAll().addOnCompleteListener(task -> {
                                List<StorageReference> list = Objects.requireNonNull(task.getResult()).getItems();
                                for (StorageReference ref : list) {
                                    ref.delete();
                                }

                                result7 = false;
                                System.out.println("-------------------- result7 -------------------");


                            });

                        }).setNegativeButton("아니오", (dialogInterface, i) -> dialogInterface.cancel());
                AlertDialog alertd = alt_del.create();
                alertd.setTitle("그룹 삭제");
                alertd.show();
                break;


        }
    }


    private void getInfo() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Groups").document(groupID).get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot document = task.getResult();

                    assert document != null;
                    info.put("groupID", document.getId());
                    info.put("groupName", document.getString("name"));
                    info.put("comment", document.getString("comment"));
                    info.put("member", document.getLong("member"));
                    info.put("manager", document.getString("manager"));
                    info.put("type", document.getLong("type"));
                    info.put("openingDate", document.getTimestamp("openingDate"));
                    info.put("buisnessName", document.getString("buisnessName"));
                    info.put("buisnessOwner", document.getString("buisnessOwner"));
                    info.put("buisnessAddress", document.getString("buisnessAddress"));
                    info.put("buisnessPhonenum", document.getString("buisnessPhonenum"));
                    info.put("buisnessNumber", document.getString("buisnessNumber"));
                    info.put("latitude", document.getDouble("latitude"));
                    info.put("longitude", document.getDouble("longitude"));

                    FragmentActivity activity = getActivity();
                    if (activity != null) { /////그룹이름 수정되고나서 툴바이름 변경
                        ((GroupMainActivity) activity).setActionBarTitle((String) info.get("groupName"));
                    }

                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference reference = storage.getReference("group_img/" + groupID + "/main.jpg");
                    reference.getDownloadUrl().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            info.put("Uri", task1.getResult());

                        }
                    });
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        getInfo();

    }

    private void makeToastMessage() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            System.out.println("-------------------- toast me -------------------");

            Toast myToast = Toast.makeText(getContext(), "삭제가 완료되었습니다.", Toast.LENGTH_SHORT);
            myToast.show();
        });
    }


}