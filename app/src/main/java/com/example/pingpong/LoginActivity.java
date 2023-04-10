package com.example.pingpong;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pingpong.Main.MainActivity;
import com.example.pingpong.Main.MainGroupFragment;
import com.example.pingpong.SearchSetting.MainProfileChangeActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private final String TAG = "mainTag";
    private FirebaseAuth mAuth;
    private final int RC_SIGN_IN = 123;
    public static String userUID;
    SignInButton signInButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signInButton = findViewById(R.id.btn_login);

        // [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // [END config_signin]

        // [START initialize_auth]
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        signInButton.setOnClickListener(v -> signIn());
    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
        //로그인이 되어있으면 홈화면으로 넘어가기 : splash에서 하고 넘어왔으니까 여기선 안함.
        if (currentUser != null) {

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            //DB에 사용자로 등록되어있는지 확인
            db.collection("Users").document(currentUser.getUid()).get()
                    .addOnCompleteListener(task2 -> {
                        if (Objects.requireNonNull(task2.getResult()).exists()) {//정보가 이미 들어있다. == 사용자 등록완료 -> 정보입력 필요없고 홈화면으로
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                        } else {  //정보 없음 -> 로그인띄우기
                            System.out.println("너 걸리니????");
                            signOut();

                            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(getString(R.string.default_web_client_id))
                                    .requestEmail()
                                    .build();
                            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
                            // [END config_signin]

                            // [START initialize_auth]
                            // Initialize Firebase Auth
                            mAuth = FirebaseAuth.getInstance();
                            // [END initialize_auth]

                            signInButton.setOnClickListener(v -> signIn());
                        }
                    });






//            Intent intent = new Intent(getApplication(), MainActivity.class);
//            userUID = currentUser.getUid();
//            startActivity(intent);
//            finish();
        }
    }
    // [END on_start_check_user]

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {//인증 성공
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(Objects.requireNonNull(account));
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(getApplicationContext(), "Google sign in Failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        //showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {  //로그인이 성공했으면
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        assert user != null;
                        userUID = user.getUid();

                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        // fcm 토큰 받아오기
                        FirebaseMessaging.getInstance().getToken()
                                .addOnCompleteListener(task1 -> {
                                    if (!task1.isSuccessful()) {
                                        Log.w(TAG, "Fetching FCM registration token failed", task1.getException());
                                        return;
                                    }

                                    // Get new FCM registration token
                                    String token = task1.getResult();

                                    // db에 token 저장
                                    db.collection("Users").document(user.getUid()).update("token", token);

                                });

                        //DB에 사용자로 등록되어있는지 확인
                        db.collection("Users").document(user.getUid()).get()
                                .addOnCompleteListener(task2 -> {
                                    if (Objects.requireNonNull(task2.getResult()).exists()) {//정보가 이미 들어있다. == 사용자 등록완료 -> 정보입력 필요없고 홈화면으로
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                    } else {  //정보 없음 -> 정보입력화면으로
                                        Intent intent = new Intent(getApplicationContext(), MainProfileChangeActivity.class);

                                        intent.putExtra("userName", user.getDisplayName());
                                        intent.putExtra("userEmail", user.getEmail());
                                        intent.putExtra("userUID", user.getUid());
                                        intent.putExtra("userImage", user.getPhotoUrl());

                                        startActivity(intent);
                                    }
                                });


                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        // Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        Toast.makeText(getApplicationContext(), "Authentication Failed", Toast.LENGTH_LONG).show();

                        // updateUI(null);
                    }

                    // [START_EXCLUDE]
                    // hideProgressDialog();
                    // [END_EXCLUDE]
                });
    }
    // [END auth_with_google]

    // [START signin]
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signin]

    private void signOut() {
       // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                   public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "Complete", Toast.LENGTH_LONG).show();
                    }
                });
    }
//
//    private void revokeAccess() {
//        // Firebase sign out
//        mAuth.signOut();
//
//        // Google revoke access
//        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
//                task -> Toast.makeText(getApplicationContext(), "Complete", Toast.LENGTH_LONG).show());
//    }


}