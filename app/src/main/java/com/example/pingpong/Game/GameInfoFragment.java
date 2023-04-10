package com.example.pingpong.Game;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.pingpong.Group.GroupGameParticiAdapter;
import com.example.pingpong.Group.WritingPost;
import com.example.pingpong.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class GameInfoFragment extends Fragment {
    String groupID, gameID, gameName, gameDate, gameDeadline, gameComment, gameWinner = "", gameWinner2 = "";
    boolean ismanager;
    List<HashMap<String, Object>> particiList = new ArrayList();
    GroupGameParticiAdapter winnerAdapter, particiAdapter;

    TextView name, startdate, deadline, comment, winnerEmpty;
    RecyclerView winner, partici;
    ImageView help;
    RequestManager requestManager;
    public static HashMap<String, String> nameMap = new HashMap<>();


    public GameInfoFragment(String groupID, String gameID, boolean ismanager) {
        this.groupID = groupID;
        this.gameID = gameID;
        this.ismanager = ismanager;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestManager = Glide.with(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_info, container, false);
        setHasOptionsMenu(true);
        name = view.findViewById(R.id.game_name);
        startdate = view.findViewById(R.id.game_date);
        deadline = view.findViewById(R.id.game_deadline);
        comment = view.findViewById(R.id.game_comment);
        winner = view.findViewById(R.id.game_winner_recy);
        winnerEmpty = view.findViewById(R.id.game_winner_text);
        partici = view.findViewById(R.id.game_partici_recy);
        help = view.findViewById(R.id.game_partici_help);

        if(ismanager) {
            help.setVisibility(View.VISIBLE);
            help.setOnClickListener(v -> {
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(v.getContext());
                alt_bld.setMessage("관리자는 참여자의 게임 참가비 지불 여부를 저장할 수 있습니다.").setCancelable(false)
                        .setNegativeButton("확인", (dialogInterface, i) -> dialogInterface.cancel());
                AlertDialog alert = alt_bld.create();
                alert.setTitle("도움말");
                alert.show();
            });
        }


        if(gameName == null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Groups").document(groupID).collection("game").document(gameID).get()
                    .addOnCompleteListener(task -> {
                        DocumentSnapshot document = task.getResult();

                        gameName = (String) document.getData().get("gamename");
                        gameComment = (String) document.getData().get("comment");
                        particiList = (List<HashMap<String, Object>>) document.getData().get("participants");

                        Timestamp date = (Timestamp) document.getData().get("startdate");
                        Date d = date.toDate();
                        @SuppressLint("SimpleDateFormat") DateFormat format = new SimpleDateFormat("yy/MM/dd  HH:mm");
                        String strdate = format.format(d);
                        gameDate = "게임 시작일: " + strdate;

                        date = (Timestamp) document.getData().get("deadline");
                        d = date.toDate();
                        strdate = format.format(d);
                        gameDeadline = "신청 마감일: " + strdate;


                        name.setText(gameName);
                        startdate.setText(gameDate);
                        deadline.setText(gameDeadline);
                        comment.setText(gameComment);

                        if(particiList == null) particiList = new ArrayList();
                        particiAdapter = new GroupGameParticiAdapter(particiList, ismanager, groupID, gameID, false, requestManager);
                        partici.setAdapter(particiAdapter);

                        for(HashMap<String, Object> user: particiList) {
                            String uid = (String) user.get("userUID");
                            db.document("Users/"+uid).get().addOnCompleteListener(task1 -> {
                                String name = task1.getResult().getString("name");
                                nameMap.put((String) uid, name);
                            });
                        }

                    });
        } else {
            name.setText(gameName);
            startdate.setText(gameDate);
            deadline.setText(gameDeadline);
            comment.setText(gameComment);
            particiAdapter = new GroupGameParticiAdapter(particiList, ismanager, groupID, gameID, false, requestManager);
            partici.setAdapter(particiAdapter);
        }

        //처음 세팅을 위너가 없다고 해줬었으니까.
        winnerEmpty.setVisibility(View.VISIBLE);
        winner.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getWinner();

    }



    private void getWinner(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Groups").document(groupID).collection("game").document(gameID).get().addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();
            String gWinner = "", gWinner2 = "";
            if(document.getData().get("winnerUID")!=null) gWinner = (String) document.getData().get("winnerUID");
            if(document.getData().get("winnerUID2")!=null) gWinner2 = (String) document.getData().get("winnerUID2");


            if(!gWinner.equals(gameWinner)){    //변했으니까 바꿔줘야함
                gameWinner = gWinner;

                if(!gWinner2.equals(gameWinner2)) {
                    gameWinner2 = gWinner2;
                }

                if (gameWinner.equals("")) {
                    winnerEmpty.setVisibility(View.VISIBLE);
                    winner.setVisibility(View.GONE);
                } else {
                    winnerEmpty.setVisibility(View.GONE);
                    winner.setVisibility(View.VISIBLE);

                    List<String> list = new ArrayList();
                    list.add(gameWinner);
                    if(!gameWinner2.equals(""))
                        list.add(gameWinner2);
                    winnerAdapter = new GroupGameParticiAdapter(list, false, groupID, gameID, true, requestManager);
                    winner.setAdapter(winnerAdapter);

                }

            }

        });
    }
}