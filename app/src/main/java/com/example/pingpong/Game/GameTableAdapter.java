package com.example.pingpong.Game;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pingpong.LoginActivity;
import com.example.pingpong.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameTableAdapter extends RecyclerView.Adapter<GameTableAdapter.ItemViewHolder> implements Filterable {
    List<HashMap<String, Object>> scheduleList, filteredList;
    boolean ismanager, isSchedule;
    View view;
    String groupID, gameID;

    public GameTableAdapter(List<HashMap<String, Object>> list, boolean ismanager, String groupID, String gameID, boolean isSchedule) {
        this.scheduleList = list;
        this.filteredList = list;
        this.ismanager = ismanager;
        this.groupID = groupID;
        this.gameID = gameID;
        this.isSchedule = isSchedule;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game_table, parent, false);
        return new GameTableAdapter.ItemViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, @SuppressLint("RecyclerView") int position) {
        HashMap<String, Object> info = (HashMap<String, Object>) filteredList.get(position);
        System.out.println("--------------game table adapter 리사이클러뷰에서 info 하나는");
        System.out.println(info.get("round"));
        System.out.println(info);

        holder.name.setText((String) info.get("gameName"));

        //이름
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String user1UID = (String) info.get("user1UID");
        String user2UID = (String) info.get("user2UID");
        String user3UID = (String) info.get("user3UID");
        String user4UID = (String) info.get("user4UID");

        boolean isTeam = false;

        if (user1UID.equals("none")) {
            info.put("name1", "none");
            holder.partici1.setText("none");
        } else if (user3UID == null) { ///단식
            String name = GameInfoFragment.nameMap.get(user1UID);
            info.put("name1", name);
            holder.partici1.setText(name);
        } else { //복식
            String name1 = GameInfoFragment.nameMap.get(user1UID);
            String name2 = GameInfoFragment.nameMap.get(user3UID);
            String name = name1 + "," + name2;

            info.put("name1", name1 + "," + name2);
            holder.partici1.setText(name);
        }

        if (user2UID.equals("none")) {
            info.put("name2", "none");
            holder.partici2.setText("none");
        } else if (user2UID.equals("부전승")) {
            info.put("name2", "부전승");
            holder.partici2.setText("부전승");
        } else if (user4UID == null) { //단식
            String name = GameInfoFragment.nameMap.get(user2UID);
            info.put("name2", name);
            holder.partici2.setText(name);
        } else { //복식
            isTeam = true;
            String name1 = GameInfoFragment.nameMap.get(user2UID);
            String name2 = GameInfoFragment.nameMap.get(user4UID);
            String name = name1 + "," + name2;
            info.put("name2", name);
            holder.partici2.setText(name);
        }


        //점수
        if (info.get("resultscore1") != null) {
            holder.scoreLayout.setVisibility(View.VISIBLE);
            holder.resultEmpty.setVisibility(View.GONE);

            holder.resultscore1.setText(Long.toString((long) info.get("resultscore1")));
            holder.resultscore2.setText(Long.toString((long) info.get("resultscore2")));
            if (info.get("score1") != null)
                holder.score1.setText(Long.toString((long) info.get("score1")));
            else holder.score1.setText("0");
            if (info.get("score2") != null)
                holder.score2.setText(Long.toString((long) info.get("score2")));
            else holder.score2.setText("0");
            if (info.get("score3") != null)
                holder.score3.setText(Long.toString((long) info.get("score3")));
            else holder.score3.setText("0");
            if (info.get("score4") != null)
                holder.score4.setText(Long.toString((long) info.get("score4")));
            else holder.score4.setText("0");
            if (info.get("score5") != null)
                holder.score5.setText(Long.toString((long) info.get("score5")));
            else holder.score5.setText("0");
            if (info.get("score6") != null)
                holder.score6.setText(Long.toString((long) info.get("score6")));
            else holder.score6.setText("0");
            if (info.get("score7") != null | info.get("score8") != null) {
                holder.score7.setVisibility(View.VISIBLE);
                holder.score8.setVisibility(View.VISIBLE);
                if (info.get("score7") != null)
                    holder.score7.setText(Long.toString((long) info.get("score7")));
                else holder.score7.setText("0");
                if (info.get("score8") != null)
                    holder.score8.setText(Long.toString((long) info.get("score8")));
                else holder.score8.setText("0");
            }
            if (info.get("score9") != null | info.get("score10") != null) {
                holder.score9.setVisibility(View.VISIBLE);
                holder.score10.setVisibility(View.VISIBLE);
                if (info.get("score9") != null)
                    holder.score9.setText(Long.toString((long) info.get("score9")));
                else holder.score9.setText("0");
                if (info.get("score10") != null)
                    holder.score10.setText(Long.toString((long) info.get("score10")));
                else holder.score10.setText("0");
            }
        } else {  //결과없음
            holder.scoreLayout.setVisibility(View.GONE);
            holder.resultEmpty.setVisibility(View.VISIBLE);
            holder.resultscore1.setText("0");
            holder.resultscore2.setText("0");
        }
        boolean isGamer = false;

        if (!ismanager) {
            String userName = GameInfoFragment.nameMap.get(LoginActivity.userUID);

            if (holder.partici1.getText().toString().contains(userName) || holder.partici2.getText().toString().contains(userName)) {
                isGamer = true;

            }
        }
        // 매니저면 결과입력가능
        if ((isGamer || ismanager) && !user2UID.equals("부전승") && !user1UID.equals("none") && !user2UID.equals("none")) {
            //} //maganer
            boolean finalIsTeam = isTeam;
            View.OnClickListener clickListener = v -> {
                if (isSchedule) {
                    GameScoreDialog dialog = new GameScoreDialog(view.getContext(), groupID, gameID, info);
                    dialog.setDiaglogListener(result -> {     //바뀐값 받아옴.

                        // 기존 값 받아오기
                        Long eScore1, eScore2;
                        eScore1 = Long.parseLong(holder.resultscore1.getText().toString());
                        eScore2 = Long.parseLong(holder.resultscore2.getText().toString());
                        Boolean isUser1Won;
                        if (eScore1 == 0 && eScore2 == 0) {
                            isUser1Won = null;
                        } else {
                            if (eScore1 > eScore2)
                                isUser1Won = true;
                            else
                                isUser1Won = false;
                        }

                        //filteredList, scheduleList 모두 정보 바꾸기
                        HashMap<String, Object> temp = new HashMap<>(result);
                        HashMap<String, Object> change = filteredList.get(position);
                        filteredList.set(position, temp);
                        int cindex = scheduleList.indexOf(change);
                        scheduleList.set(cindex, temp);

                        //notify
                        notifyItemChanged(position);

                        //db에 업데이트
                        String round = (String) result.get("round");
                        result.remove("name1");
                        result.remove("name2");
                        result.remove("round");

                        FirebaseFirestore db1 = FirebaseFirestore.getInstance();
                        db1.document("Groups/" + groupID + "/game/" + gameID + "/schedule/" + round).update(result);

                        // result 누가이겼는지 확인
                        String winner1 = "";
                        String winner2 = null;
                        Boolean isUser1Win;

                        if (result.get("resultscore1") == null) {
                            winner1 = "none";
                            isUser1Win = null;
                        } else {
                            long r1 = (long) result.get("resultscore1");
                            long r2 = (long) result.get("resultscore2");

                            if (r1 > r2) {
                                isUser1Win = true;
                                winner1 = (String) result.get("user1UID");
                                if (result.get("user3UID") != null)
                                    winner2 = (String) result.get("user3UID");
                            } else if (r1 < r2) {
                                isUser1Win = false;
                                winner1 = (String) result.get("user2UID");
                                if (result.get("user4UID") != null)
                                    winner2 = (String) result.get("user4UID");
                            } else {
                                isUser1Win = null;
                                winner1 = "none";
                            }

                        }

                        long score1, score2;
                        Boolean isUser1Win1;
                        if (result.get("resultscore1") != null) {
                            score1 = (long) result.get("resultscore1");
                            score2 = (long) result.get("resultscore2");

                            if (score1 > score2)
                                isUser1Win1 = true;
                            else
                                isUser1Win1 = false;

                            if (isUser1Won != isUser1Win1) { //승패바뀜!!!!!!!!!!!11
                                boolean isChange = (isUser1Won != null);    // 첫입력:false
                                if (score1 > score2) {///user1이 이긴 경우
                                    updateUserInfoScore(user1UID, user2UID, isChange, false);
                                    if (finalIsTeam)
                                        updateUserInfoScore(user3UID, user4UID, isChange, false);


                                    System.out.println("user1이김");
                                } else if (score1 < score2) {///user2가 이긴 경우
                                    updateUserInfoScore(user2UID, user1UID, isChange, false);
                                    if (finalIsTeam)
                                        updateUserInfoScore(user4UID, user3UID, isChange, false);
                                }

                                System.out.println("유저1이겼었니?" + isUser1Won + "유저1이지금이겼니?" + isUser1Win);
                                System.out.println("승패저장하러들어옴");
                            }
                        } else {////삭제한 경우
                            if (eScore1 > eScore2) {///user1이 이긴 경우
                                updateUserInfoScore(user1UID, user2UID, true, true);
                                if (finalIsTeam)
                                    updateUserInfoScore(user3UID, user4UID, true, true);
                                System.out.println("user1이김");
                            } else if (eScore1 < eScore2) {///user2가 이긴 경우
                                updateUserInfoScore(user2UID, user1UID, true, true);
                                if (finalIsTeam)
                                    updateUserInfoScore(user4UID, user3UID, true, true);
                            }
                        }


                        // 다음으로 올라가야하는 강인지 확인
                        String[] isfinish = round.split("-");
                        if (!isfinish[1].equals("2")) {   //결승전이 아니면


                            // 다음 document이름(round) 구함
                            int s1 = Integer.parseInt(isfinish[0]) + 1;  //0번째 +1
                            int s2 = Integer.parseInt(isfinish[1]) / 2;  //1번째 /2
                            int s3 = Integer.parseInt(isfinish[2]);  //2번째 +1 /2

                            int sr3 = (s3 + 1) / 2; //다음 라운드 2번째

                            // notify를 어떻게 하지?
                            // id 알고있으니까 스케쥴 리스트 돌아서 찾아서 포지션 가져와서 노티파이하자!
                            // 다음 인덱스 = (현재 인덱스 + (현재라운드[1]/2(==다음라운드[1]) - 현재라운드[2] + 다음라운드[2]))
                            int index = cindex + s2 - s3 + sr3;

                            System.out.println("-----------update하는 index: " + index);
                            HashMap<String, Object> map = scheduleList.get(index);


                            // user win lose update
                            if (isUser1Won != null && isUser1Won != isUser1Win && map.get("resultscore1") != null) {
                                System.out.println("user에 들어있는 win lose 정보 update");

                                long result1 = (long) map.get("resultscore1");
                                long result2 = (long) map.get("resultscore2");
                                String user1uid = (String) map.get("user1UID");
                                String user2uid = (String) map.get("user2UID");
                                String user3uid = (String) map.get("user3UID");
                                String user4uid = (String) map.get("user4UID");

                                if (result1 > result2) {
                                    db.document("Users/" + user1uid).update("win", FieldValue.increment(-1));
                                    db.document("Users/" + user2uid).update("lose", FieldValue.increment(-1));
                                    if (user3uid != null)
                                        db.document("Users/" + user3uid).update("win", FieldValue.increment(-1));
                                    if (user4uid != null)
                                        db.document("Users/" + user4uid).update("lose", FieldValue.increment(-1));
                                } else if (result1 < result2) {
                                    db.document("Users/" + user1uid).update("lose", FieldValue.increment(-1));
                                    db.document("Users/" + user2uid).update("win", FieldValue.increment(-1));
                                    if (user3uid != null)
                                        db.document("Users/" + user3uid).update("lose", FieldValue.increment(-1));
                                    if (user4uid != null)
                                        db.document("Users/" + user4uid).update("win", FieldValue.increment(-1));
                                }
                            }


                            // s3에따라 user1, user2 결정
                            String user = "", user2 = "";
                            if (s3 % 2 == 1) { //홀수면 user1UID로
                                user = "user1UID";
                                user2 = "user3UID";
                            } else {     //짝수면 user2UID로
                                user = "user2UID";
                                user2 = "user4UID";
                            }
                            map.put(user, winner1);
                            map.put(user2, winner2);

                            // 다음 round notify
                            scheduleList.set(index, map);
                            notifyItemChanged(index);

                            // 다음 round db에 저장
                            round = s1 + "-" + s2 + "-" + sr3;
                            System.out.println("---------------update할 round: " + round);


                            // win, lose 결과가 바뀌었다면 뒤의 round들이 연쇄적으로 score값이 null이 되어야 한다.
                            if (isUser1Won != null && isUser1Won != isUser1Win) {
                                System.out.println("win, lose 결과가 바뀌었다면 뒤의 round들이 연쇄적으로 score값이 null이 되어야 한다.");
                                map.put("score1", null);
                                map.put("score2", null);
                                map.put("score3", null);
                                map.put("score4", null);
                                map.put("score5", null);
                                map.put("score6", null);
                                map.put("score7", null);
                                map.put("score8", null);
                                map.put("score9", null);
                                map.put("score10", null);
                                map.put("resultscore1", null);
                                map.put("resultscore2", null);
                                db1.document("Groups/" + groupID + "/game/" + gameID + "/schedule/" + round).update(map);

                                cindex = index;
                                s3 = sr3;
                                while (true) {
                                    System.out.println("while문 들어옴");
                                    // 다음 document이름(round) 구함
                                    s1 = s1 + 1;  //0번째 +1
                                    s2 = s2 / 2;  //1번째 /2
                                    s3 = s3;  //2번째 +1 /2
                                    sr3 = (s3 + 1) / 2; //다음 라운드 2번째
                                    index = cindex + s2 - s3 + sr3;
                                    System.out.println("index: " + index);
                                    System.out.println("round: " + s1 + "-" + s2 + "-" + s3);
                                    if (s2 == 1) break;
                                    map = scheduleList.get(index);

                                    // user win lose update
                                    if (map.get("resultscore1") != null) {
                                        System.out.println("user에 들어있는 win lose 정보 update");

                                        long result1 = (long) map.get("resultscore1");
                                        long result2 = (long) map.get("resultscore2");
                                        String user1uid = (String) map.get("user1UID");
                                        String user2uid = (String) map.get("user2UID");
                                        String user3uid = (String) map.get("user3UID");
                                        String user4uid = (String) map.get("user4UID");

                                        if (result1 > result2) {
                                            System.out.println("r1>r2 user1 win--, user2 lose--");
                                            db.document("Users/" + user1uid).update("win", FieldValue.increment(-1));
                                            db.document("Users/" + user2uid).update("lose", FieldValue.increment(-1));
                                            if (user3uid != null)
                                                db.document("Users/" + user3uid).update("win", FieldValue.increment(-1));
                                            if (user4uid != null)
                                                db.document("Users/" + user4uid).update("lose", FieldValue.increment(-1));
                                        } else if (result1 < result2) {
                                            System.out.println("r1<r2 user1 lose--, user2 win--");
                                            db.document("Users/" + user1uid).update("lose", FieldValue.increment(-1));
                                            db.document("Users/" + user2uid).update("win", FieldValue.increment(-1));
                                            if (user3uid != null)
                                                db.document("Users/" + user3uid).update("lose", FieldValue.increment(-1));
                                            if (user4uid != null)
                                                db.document("Users/" + user4uid).update("win", FieldValue.increment(-1));
                                        }

                                        map.put("score1", null);
                                        map.put("score2", null);
                                        map.put("score3", null);
                                        map.put("score4", null);
                                        map.put("score5", null);
                                        map.put("score6", null);
                                        map.put("score7", null);
                                        map.put("score8", null);
                                        map.put("score9", null);
                                        map.put("score10", null);
                                        map.put("resultscore1", null);
                                        map.put("resultscore2", null);
                                    }

                                    if (s3 % 2 == 1) { //홀수면 user1UID로
                                        if (map.get("user1UID").equals("none")) break;
                                        map.put("user1UID", "none");
                                    } else {     //짝수면 user2UID로
                                        if (map.get("user2UID").equals("none")) break;
                                        map.put("user2UID", "none");
                                    }


                                    scheduleList.set(index, map);
                                    notifyItemChanged(index);
                                    round = s1 + "-" + s2 + "-" + sr3;
                                    db1.document("Groups/" + groupID + "/game/" + gameID + "/schedule/" + round).update(map);


                                    s3 = sr3;
                                    cindex = index;

                                }
                                //우승자 없애기
                                db1.document("Groups/" + groupID + "/game/" + gameID).update("winnerUID", null);

                                //state 진행중으로
                                db1.document("Groups/" + groupID + "/game/" + gameID).update("state", 1);


                            } else {  // userUID만 바꾼다
                                db1.document("Groups/" + groupID + "/game/" + gameID + "/schedule/" + round).update(user, winner1);
                                db1.document("Groups/" + groupID + "/game/" + gameID + "/schedule/" + round).update(user2, winner2);
                            }

                        } else {  //결승전이면
                            if (winner1.equals("none")) {
                                winner1 = null;
                                db1.document("Groups/" + groupID + "/game/" + gameID).update("state", 1);
                            } else {
                                db1.document("Groups/" + groupID + "/game/" + gameID).update("state", -1);
                            }

                            db1.document("Groups/" + groupID + "/game/" + gameID).update("winnerUID", winner1);
                            db1.document("Groups/" + groupID + "/game/" + gameID).update("winnerUID2", winner2);


                        }
                    });
                    dialog.show();
                } else { /////리그인 경우

                    GameScoreDialog dialog = new GameScoreDialog(view.getContext(), groupID, gameID, info);
                    dialog.setDiaglogListener(result -> {     //바뀐값 받아옴.

                        Long eScore1, eScore2;
                        eScore1 = Long.parseLong(holder.resultscore1.getText().toString());
                        eScore2 = Long.parseLong(holder.resultscore2.getText().toString());
                        Boolean isUser1Won;
                        System.out.println(eScore1 + "기존스코어1   " + eScore2 + "기존스코어2");
                        if (eScore1 == 0 && eScore2 == 0) {
                            isUser1Won = null;
                        } else {
                            System.out.println("기록없지않으면");
                            if (eScore1 > eScore2)
                                isUser1Won = true;
                            else
                                isUser1Won = false;
                        }
                        //notify
                        HashMap<String, Object> temp = new HashMap<>(result);

                        //filteredList, scheduleList 모두 정보 바꾸기
                        HashMap<String, Object> change = filteredList.get(position);
                        filteredList.set(position, temp);
                        int cindex = scheduleList.indexOf(change);
                        scheduleList.set(cindex, temp);

                        notifyItemChanged(position);

                        //db에 업데이트
                        String gameName = (String) result.get("gameName");
                        String[] teamName = gameName.split("-");
                        result.remove("name1");
                        result.remove("name2");

                        long score1, score2;
                        Boolean isUser1Win;
                        if (result.get("resultscore1") != null) {
                            score1 = (long) result.get("resultscore1");
                            score2 = (long) result.get("resultscore2");

                            if (score1 > score2)
                                isUser1Win = true;
                            else
                                isUser1Win = false;
                            System.out.println("유저1현재이김:" + isUser1Win + " 유저1과거이김:" + isUser1Won);
                            if (isUser1Won != isUser1Win) { //승패바뀜!!!!!!!!!!!11
                                boolean isChange = (isUser1Won != null);    // 첫입력:false
                                if (score1 > score2) {///user1이 이긴 경우
                                    updateGameInfoScore(user1UID, user2UID, teamName[0], isChange, false);
                                    updateUserInfoScore(user1UID, user2UID, isChange, false);
                                    if (finalIsTeam) {
                                        System.out.println("복식입니다.1");
                                        updateGameInfoScore(user3UID, user4UID, teamName[0], isChange, false);
                                        updateUserInfoScore(user3UID, user4UID, isChange, false);
                                    }

                                    System.out.println("user1이김");
                                } else if (score1 < score2) {///user2가 이긴 경우
                                    updateGameInfoScore(user2UID, user1UID, teamName[0], isChange, false);
                                    updateUserInfoScore(user2UID, user1UID, isChange, false);
                                    if (finalIsTeam) {
                                        System.out.println("복식입니다.2");
                                        updateGameInfoScore(user4UID, user3UID, teamName[0], isChange, false);
                                        updateUserInfoScore(user4UID, user3UID, isChange, false);
                                    }
                                }

                                System.out.println("유저1이겼었니?" + isUser1Won + "유저1이지금이겼니?" + isUser1Win);
                                System.out.println("승패저장하러들어옴");

                            }

                        } else {////삭제한 경우

                            if (eScore1 > eScore2) {///user1이 이긴 경우
                                updateGameInfoScore(user1UID, user2UID, teamName[0], true, true);
                                updateUserInfoScore(user1UID, user2UID, true, true);
                                if (finalIsTeam) {
                                    updateGameInfoScore(user3UID, user4UID, teamName[0], true, true);
                                    updateUserInfoScore(user3UID, user4UID, true, true);
                                }
                                System.out.println("user1이김");
                            } else if (eScore1 < eScore2) {///user2가 이긴 경우
                                updateGameInfoScore(user2UID, user1UID, teamName[0], true, true);
                                updateUserInfoScore(user2UID, user1UID, true, true);
                                if (finalIsTeam) {
                                    updateGameInfoScore(user4UID, user3UID, teamName[0], true, true);
                                    updateUserInfoScore(user4UID, user3UID, true, true);
                                }
                            }

                        }

                        db.document("Groups/" + groupID + "/game/" + gameID + "/league/" + teamName[0]).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                DocumentSnapshot document = task.getResult();
                                List list = (List) document.get("game");
                                list.set((Integer.parseInt(teamName[1]) - 1), result);
                                db.document("Groups/" + groupID + "/game/" + gameID + "/league/" + teamName[0]).update("game", list);
                            }
                        });


                    });
                    dialog.show();
                }
            };
            holder.resultEmpty.setOnClickListener(clickListener);
            holder.scoreLayout.setOnClickListener(clickListener);
        }

    }

    public void setList(List<HashMap<String, Object>> list) {
        this.filteredList = list;
    }

    @Override
    public int getItemViewType(int position) {  //리사이클러 재사용문제
        return position;
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                List<HashMap<String, Object>> result = new ArrayList();
                String search = charSequence.toString().trim();

                if (charSequence.length() == 0 || search.equals("전체보기")) {
                    result = scheduleList;
                    filteredList = scheduleList;
                } else {

                    for (HashMap<String, Object> info : scheduleList) {
                        String gameName = (String) info.get("gameName");
                        gameName = gameName.split("-")[0];
                        if (gameName.equals(search)) {
                            result.add(info);

                        }

                    }
                }

                FilterResults results = new FilterResults();
                results.values = result;
                return results;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredList = (List<HashMap<String, Object>>) filterResults.values;
                notifyDataSetChanged();

            }
        };
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView name, partici1, partici2, score1, score2, score3, score4, score5, score6, score7, score8, score9, score10, resultscore1, resultscore2;
        LinearLayout scoreLayout, resultEmpty;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.game_table_name);
            partici1 = itemView.findViewById(R.id.game_partici_name1);
            partici2 = itemView.findViewById(R.id.game_partici_name2);
            score1 = itemView.findViewById(R.id.game_score_1);
            score2 = itemView.findViewById(R.id.game_score_2);
            score3 = itemView.findViewById(R.id.game_score_3);
            score4 = itemView.findViewById(R.id.game_score_4);
            score5 = itemView.findViewById(R.id.game_score_5);
            score6 = itemView.findViewById(R.id.game_score_6);
            score7 = itemView.findViewById(R.id.game_score_7);
            score8 = itemView.findViewById(R.id.game_score_8);
            score9 = itemView.findViewById(R.id.game_score_9);
            score10 = itemView.findViewById(R.id.game_score_10);
            resultscore1 = itemView.findViewById(R.id.game_score_result);
            resultscore2 = itemView.findViewById(R.id.game_score_result2);
            resultEmpty = itemView.findViewById(R.id.game_score_none);
            scoreLayout = itemView.findViewById(R.id.game_score_layout);

        }
    }

    private void updateUserInfoScore(String winuser, String loseuser, boolean isChanged, boolean isDeleted) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.document("Users/" + winuser).get().addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();

            if (!isChanged) {
                if (document.getLong("win") == null) {///첫 입력
                    System.out.println("이긴사람첫입력");

                    db.document("Users/" + winuser).update("win", 1);
                    db.document("Users/" + winuser).update("lose", 0);
                } else { //일반 입력
                    System.out.println("이긴사람 일반입력 win기존값+1");
                    db.document("Users/" + winuser).update("win", FieldValue.increment(1));
                }
            } else {/// 승패 바뀐 경우
                long winscore = 0, losescore = 0;
                if (document.getLong("win") != null) {
                    winscore = document.getLong("win");
                    losescore = document.getLong("lose");
                }
                System.out.println("승패바뀜");
                if (isDeleted) {///삭제한경우
                    System.out.println("이긴사람 승패삭제");
                    if (winscore != 0) {
                        System.out.println(winscore + "<-------winscore");
                        db.document("Users/" + winuser).update("win", FieldValue.increment(-1));
                    }

                } else {//일반
                    System.out.println("이긴사람 윈++ 루즈--");
                    db.document("Users/" + winuser).update("win", FieldValue.increment(1));
                    if (losescore != 0) {
                        db.document("Users/" + winuser).update("lose", FieldValue.increment(-1));
                    }
                }
            }
        });

        db.document("Users/" + loseuser).get().addOnCompleteListener(task -> {
            DocumentSnapshot document = task.getResult();

            if (!isChanged) {
                if (document.get("lose") == null) {///첫 입력
                    System.out.println("lose첫입력");

                    db.document("Users/" + loseuser).update("win", 0);
                    db.document("Users/" + loseuser).update("lose", 1);
                } else { //일반 입력
                    db.document("Users/" + loseuser).update("lose", FieldValue.increment(1));
                }
            } else {///승패 바뀜
                long winscore = 0, losescore = 0;
                if (document.getLong("win") != null) {
                    winscore = document.getLong("win");
                    losescore = document.getLong("lose");
                }

                if (isDeleted) {///삭제한경우
                    System.out.println("승패삭제33");
                    if (losescore != 0) {
                        db.document("Users/" + loseuser).update("lose", FieldValue.increment(-1));
                    }
                } else {
                    System.out.println("진사람 윈-- 루즈++");
                    db.document("Users/" + loseuser).update("lose", FieldValue.increment(1));
                    if (winscore != 0) {
                        db.document("Users/" + loseuser).update("win", FieldValue.increment(-1));
                    }
                }
            }

        });


    }

    private void updateGameInfoScore(String winuser, String loseuser, String teamName, boolean isChanged, boolean isDeleted) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        System.out.println("이긴사람: " + winuser + ", 진사람: " + loseuser + "----updateUserScore");
        db.document("Groups/" + groupID + "/game/" + gameID + "/league/" + teamName).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                List<HashMap<String, Object>> list = (List<HashMap<String, Object>>) documentSnapshot.get("info");
                if (list == null) return;
                for (int i = 0; i < list.size(); i++) {
                    HashMap<String, Object> infomap = list.get(i);

                    boolean result;
                    if (infomap.get("userUID") != null)
                        result = infomap.get("userUID").equals(winuser);
                    else result = infomap.get("userUID1").equals(winuser);

                    if (result) { ////이긴사람 win ++
                        //         System.out.println("----이긴사람");

                        if (!isChanged) {
                            if (infomap.get("win") == null) {///첫 입력
                                //           System.out.println("이긴사람첫입력");
                                infomap.put("win", 1);
                                infomap.put("lose", 0);
                            } else { //일반 입력
                                //          System.out.println("이긴사람 일반입력 win기존값+1");
                                long winscore = (long) infomap.getOrDefault("win", 0L);
                                infomap.replace("win", winscore + 1);
                            }
                        } else {/// 승패 바뀐 경우

                            long winscore = (long) infomap.getOrDefault("win", 0L);
                            long losescore = (long) infomap.getOrDefault("lose", 0L);
                            //    System.out.println("승패바뀜");
                            if (isDeleted) {///삭제한경우
                                //           System.out.println("이긴사람 승패삭제");
                                if (losescore != 0) {
                                    infomap.replace("lose", losescore - 1);
                                }
                                if (winscore != 0) {
                                    infomap.replace("win", winscore - 1);
                                }

                            } else {//일반
                                //      System.out.println("이긴사람 윈++ 루즈--");
                                infomap.replace("win", winscore + 1);
                                if (losescore != 0) {
                                    infomap.replace("lose", losescore - 1);
                                }
                            }


                        }
                        list.set(i, infomap);
                        db.document("Groups/" + groupID + "/game/" + gameID + "/league/" + teamName).update("info", list);
                    }
                    if (infomap.get("userUID") != null)
                        result = infomap.get("userUID").equals(loseuser);
                    else result = infomap.get("userUID1").equals(loseuser);
                    if (result) { ////진사람 lose ++
                        //      System.out.println("----진사람");
                        if (!isChanged) {
                            if (infomap.get("lose") == null) {///첫 입력
                                //            System.out.println("lose첫입력");
                                infomap.put("win", 0);
                                infomap.put("lose", 1);
                            } else { //일반 입력
                                long losescore = (long) infomap.getOrDefault("lose", 0L);
                                infomap.replace("lose", losescore + 1);

                            }
                        } else {///승패 바뀜
                            long winscore = (long) infomap.getOrDefault("win", 0L);
                            long losescore = (long) infomap.getOrDefault("lose", 0L);

                            if (isDeleted) {///삭제한경우
                                //         System.out.println("승패삭제33");

                                if (losescore != 0) {
                                    infomap.replace("lose", losescore - 1);
                                }
                                if (winscore != 0) {
                                    infomap.replace("win", winscore - 1);
                                }
                            } else {
                                //       System.out.println("진사람 윈-- 루즈++");
                                infomap.replace("lose", losescore + 1);
                                if (winscore != 0) {
                                    infomap.replace("win", winscore - 1);
                                }
                            }


                        }
                        list.set(i, infomap);
                        db.document("Groups/" + groupID + "/game/" + gameID + "/league/" + teamName).update("info", list);
                    }
                }
            }
        });


    }


}
