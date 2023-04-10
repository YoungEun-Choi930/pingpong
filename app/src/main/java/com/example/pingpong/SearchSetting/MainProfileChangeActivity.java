package com.example.pingpong.SearchSetting;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.pingpong.LoginActivity;
import com.example.pingpong.Main.MainActivity;
import com.example.pingpong.Main.MainGroupFragment;
import com.example.pingpong.MyFirebaseMessagingService;
import com.example.pingpong.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainProfileChangeActivity extends AppCompatActivity {
    int REQUEST_IMAGE_CODE = 1001;
    int REQUEST_EXTERNAL_STORAGE_PERMISSION = 1002;
    int REQUEST_CROP_PICTURE = 1003;
    ImageView changeImage, ivUser;
    EditText name, team;
    Spinner degree;
    String userDegree, userGender;
    RadioGroup gender;
    String userUID, userEmail;
    byte[] image;
    Uri userImage;
    private MainProfileChangeActivity.ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_info);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.changeinfo_toolbar);
        setSupportActionBar(myToolbar);//툴바달기


        //권한 요청
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_EXTERNAL_STORAGE_PERMISSION);
            }
        }

        //화면
        ivUser = findViewById(R.id.changeinfo_profile_image);
        name = findViewById(R.id.changeinfo_name);
        team = findViewById(R.id.changeinfo_team);
        degree = findViewById(R.id.changeinfo_degree);
        String[] degreeList = {"선수","1부", "2부", "3부", "4부", "5부", "6부", "7부", "8부", "9부", "미정"};
        ArrayAdapter spinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, degreeList);
        degree.setAdapter(spinnerAdapter);
        degree.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) view).setText(degreeList[position]);
                userDegree = degreeList[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        gender = findViewById(R.id.changeinfo_gender);
        changeImage = findViewById(R.id.btn_changeImage);
        changeImage.setOnClickListener(view -> {
            Intent in = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(in, REQUEST_CROP_PICTURE);
        });



        Intent intent = getIntent();
        String userName = intent.getStringExtra("userName");
        userUID = LoginActivity.userUID;
        userImage = intent.getParcelableExtra("userImage");

        if(intent.hasExtra("userEmail")) {  //init info
            myToolbar.setTitle("기본 정보 입력");
            userEmail = intent.getStringExtra("userEmail");
        }
        else {      //change info
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);  //툴바에 뒤로가기

            userDegree = intent.getStringExtra("userDegree");
            userGender = intent.getStringExtra("userGender");
            String userTeam = intent.getStringExtra("userTeam");

            team.setText(userTeam);

            //부수
            int d;
            if(userDegree.equals("미정")) d = 9;
            else if(userDegree.equals("선수")) d=0;
            else d = Integer.parseInt(userDegree.substring(0,1))-1;
            degree.setSelection(d);

            //성별

            if(userGender.equals("0")) {
                RadioButton man = findViewById(R.id.changeinfo_gender0);
                man.setChecked(true);
            }
            else if(userGender.equals("1")) {
                RadioButton womman = findViewById(R.id.changeinfo_gender1);
                womman.setChecked(true);
            }

        }


        //공통
        Glide.with(this).load(userImage).into(ivUser);
        name.setText(userName);


        //로딩창
        progress = new ProgressDialog(this);
        progress.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));  //배경 투명

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_IMAGE_CODE){
            //크롭된 이미지 가져와서 이미지뷰에 보여주기
            if (resultCode == RESULT_OK) {
                assert data != null;
                if (data.hasExtra("data")) { //데이터를 가지고 있는지 확인
                    final Bundle extras = data.getExtras();

                    if (extras != null) {
                        Bitmap photo = extras.getParcelable("data"); //크롭한 이미지 가져오기
                        ivUser.setImageBitmap(photo); //이미지뷰에 넣기

                        ByteArrayOutputStream stream = new ByteArrayOutputStream(); //db에 저장할 용도
                        photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        image = stream.toByteArray();
                    }
                }
            }
        }
        else if(requestCode == REQUEST_CROP_PICTURE) {
            if (resultCode == RESULT_OK) {
                // 이미지를 가져온 이후의 리사이즈할 이미지 크기를 결정
                // 이후에 이미지 크롭 어플리케이션을 호출

                assert data != null;
                Uri img = data.getData();

                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(img, "image/*");

                intent.putExtra("outputX", 100); //크롭한 이미지 x축 크기
                intent.putExtra("outputY", 100); //크롭한 이미지 y축 크기
                intent.putExtra("aspectX", 1); //크롭 박스의 x축 비율
                intent.putExtra("aspectY", 1); //크롭 박스의 y축 비율
                intent.putExtra("scale", true);
                intent.putExtra("return-data", true);
                startActivityForResult(intent, REQUEST_IMAGE_CODE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.changeinfo, menu); //툴바에 메뉴 설정
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){
            case R.id.menu_changeinfo_yes://확인버튼
                //이름
                String resultName = name.getText().toString();
                String resultTeam = team.getText().toString();
                //성별
                int genderId = gender.getCheckedRadioButtonId();
                RadioButton resultGenderbtn = (RadioButton)findViewById(genderId);

                //모두 입력되었는지 확인
                if(resultName.equals("") | userDegree.equals("") | resultGenderbtn == null){
                    Toast.makeText(this, "정보를 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
                else {
                    progress.show();
                    //성별 가공. initinfo_gender0 에서 마지막 숫자만 빼기. 0이면 남자, 1이면 여자.
                    String resultGender = resultGenderbtn.getResources().getResourceName(genderId);
                    resultGender = resultGender.substring(resultGender.length()-1);

                    //DB에 저장
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    if(userEmail != null) { //init info
                        Map<String, Object> user = new HashMap<>();
                        user.put("email", userEmail);
                        user.put("name", resultName);
                        user.put("degree", userDegree);
                        user.put("gender", resultGender);
                        user.put("team",resultTeam);

                        if(image == null)
                            user.put("image", userImage.toString());

                        // uid문서에 map정보 입력.
                        db.collection("Users").document(userUID).set(user);

                        //토큰 저장
                        MyFirebaseMessagingService messagingService = new MyFirebaseMessagingService();
                        messagingService.sendTokenToServer("");
                    }
                    else {      //change info
                        Map<String, Object> user = new HashMap<>();
                        user.put("name", resultName);
                        user.put("degree", userDegree);
                        user.put("gender", resultGender);
                        user.put("team",resultTeam);


                        db.collection("Users").document(userUID).update(user);
                    }

                    //MainProfileFragment info change
                    MainProfileFragment.userName = resultName;
                    MainProfileFragment.userDegree = userDegree;
                    MainProfileFragment.userGender = resultGender;
                    MainProfileFragment.userTeam = resultTeam;

                    //공통 - storage에 사진저장 -> uri를 firestore에 저장 -> 완료후 화면 이동
                    if(image != null) {
                        StorageReference mStorageRef;
                        mStorageRef = FirebaseStorage.getInstance().getReference();

                        //firebase storage에 업로드
                        String filename = "profile_" + userUID + ".jpg";
                        StorageReference riversRef = mStorageRef.child("profile_img/" + filename);
                        riversRef.putBytes(image).addOnSuccessListener(taskSnapshot -> riversRef.getDownloadUrl()
                                .addOnSuccessListener(uri ->
                                        db.collection("Users").document(userUID).update("image", uri.toString())
                                .addOnSuccessListener(unused -> {
                                    //MainProfileFragment info change
                                    MainProfileFragment.userImage = uri;

                                    if(userEmail != null) { //init이면 intent
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                    }

                                    progress.dismiss();
                                    //화면이동
                                    finish();
                                })));
                    }
                    else {
                        MainProfileFragment.userImage = userImage;
                        // 로딩창 끄기
                        progress.dismiss();

                        if(userEmail != null) { //init이면 intent
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                        }
                        //화면이동
                        finish();
                    }

                }

                break;
            case android.R.id.home://뒤로가기 버튼
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


    }

    static class ProgressDialog extends Dialog {
        public ProgressDialog(Context context)
        {
            super(context);
            requestWindowFeature(Window.FEATURE_NO_TITLE);// 다이얼 로그 제목을 안보이게...
            setContentView(R.layout.dialog_progress);
            setCanceledOnTouchOutside(false);   //주변터치방지
            setCancelable(false);   // 뒤로가기 방지
        }
    }
}