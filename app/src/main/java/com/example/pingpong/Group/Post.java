package com.example.pingpong.Group;

import androidx.annotation.NonNull;

import com.example.pingpong.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

/*
WritingCommentActicity, GroupHomeFragment, NoticeAdapter 에서 동시에 사용하는 메소드 deletePost를 정의하기 위함.
 */

public class Post {
    private static final String userUID = LoginActivity.userUID;

    public void deletePost(String groupID, String postID, String writerUID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String postpath = "/Groups/" + groupID + "/post/" + postID;

        /////공지인 경우 공지리스트에서도 삭제
        db.collection("Groups").document(groupID).collection("notice").document(postID).delete();

        ///////////포스트도큐먼트에 포함된 코멘트콜렉션 삭제
        db.document(postpath).collection("comment").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                db.collection("Groups").document(groupID).collection("post")
                        .document(postID).collection("comment").document(documentSnapshot.getId()).delete();
            }
        });


        DocumentReference reference = db.collection("Users").document(writerUID);
        reference.update("postList", FieldValue.arrayRemove(postpath));


        ///////포스트콜렉션에서 해당 도큐먼트 삭제
        db.document(postpath).delete();


        //////유저 코멘트들 제거
        db.collection("Users").whereArrayContains("groupList", groupID).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String uid = document.getId();

                        List<String> comment = (List) document.get("commentList");

                        if (comment != null && comment.size() != 0) {
                            List<String> updateComment = (List) document.get("commentList");
                            if (updateComment != null) {
                                updateComment.removeIf(str -> str.contains(postpath));
                            }

                            // 다시 저장
                            DocumentReference userDocument = db.collection("Users").document(uid);
                            userDocument.update("commentList", updateComment);
                        }
                    }
                });

    }
}
