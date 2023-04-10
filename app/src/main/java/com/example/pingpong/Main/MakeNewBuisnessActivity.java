package com.example.pingpong.Main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.example.pingpong.Group.GroupMainActivity;
import com.example.pingpong.LoginActivity;
import com.example.pingpong.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.Marker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class MakeNewBuisnessActivity extends AppCompatActivity implements OnMapReadyCallback {
    EditText buisnessName, buisnessOwner, buisnessAddress, buisnessPhonenum, buisnessNumber;
    boolean ischange;
    String groupID;
    MapView naverMap;
    ImageButton searchButton;
    Double x, y;
    Marker marker = new Marker();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_new_buisness);

        Intent intent = getIntent();
        ischange = intent.getBooleanExtra("ischange", false);

        NestedScrollView scroll = findViewById(R.id.scrollView);
        buisnessName = findViewById(R.id.buisness_name);
        buisnessOwner = findViewById(R.id.buisness_owner);
        buisnessAddress = findViewById(R.id.buisness_address);
        buisnessPhonenum = findViewById(R.id.buisness_phonenum);
        buisnessNumber = findViewById(R.id.buisness_number);
        naverMap = findViewById(R.id.naverMap);
        naverMap.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_MOVE: //누르고 움직였을 때
                        scroll.requestDisallowInterceptTouchEvent(true);
                        return false;
                    case MotionEvent.ACTION_UP: //누른걸 땠을 때
                        scroll.requestDisallowInterceptTouchEvent(false);
                        return true;
                    case MotionEvent.ACTION_DOWN: //처음 눌렸을 때
                        scroll.requestDisallowInterceptTouchEvent(true);
                        return false;
                    default:
                        return true;
                }
            }
        });
        searchButton = findViewById(R.id.search_button);

        Toolbar myToolbar = findViewById(R.id.buisness_toolbar);
        setSupportActionBar(myToolbar);//툴바달기
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        if (ischange) {
            getSupportActionBar().setTitle("사업자 정보 수정하기");
            HashMap<String, Object> info = (HashMap<String, Object>) intent.getSerializableExtra("groupInfo");
            groupID = (String) info.get("groupID");
            buisnessName.setText((String) info.get("buisnessName"));
            buisnessOwner.setText((String) info.get("buisnessOwner"));
            buisnessAddress.setText((String) info.get("buisnessAddress"));
            buisnessPhonenum.setText((String) info.get("buisnessPhonenum"));
            buisnessNumber.setText((String) info.get("buisnessNumber"));

            x = (double) info.get("longitude");
            y = (double) info.get("latitude");
            naverMap.getMapAsync(this);
        }


        searchButton.setOnClickListener(view -> {
            AddressTask task = new AddressTask(getApplicationContext());

            String address = buisnessAddress.getText().toString();
            if (address.equals("")) {
                Toast myToast = Toast.makeText(getApplicationContext(), "주소를 입력해 주세요.", Toast.LENGTH_SHORT);
                myToast.show();
                return;
            }

            task.execute(address);

            // 동기로 진행된다. task가 성공하면 값을 return 받는다.
            // 만약 error가 발생하면 callBackValue에 Error : 가 포함된다.

            try {
                String callBackValue = task.get();
                if (callBackValue.contains("Error")) {
                    Toast myToast = Toast.makeText(getApplicationContext(), "정확한 주소를 입력해 주세요.", Toast.LENGTH_SHORT);
                    myToast.show();
                    return;
                }

                System.out.println(callBackValue);

                JSONArray result = new JSONObject(callBackValue).getJSONArray("addresses");

                if (result.length() == 0) {
                    Toast myToast = Toast.makeText(getApplicationContext(), "정확한 주소를 입력해 주세요.", Toast.LENGTH_SHORT);
                    myToast.show();
                    return;
                }
                JSONObject data = (JSONObject) result.get(0);

                x = Double.parseDouble(data.getString("x"));
                y = Double.parseDouble(data.getString("y"));
                System.out.println("x:" + x + " ,  y:" + y + "----------");

                // 지도에 표시
                naverMap.getMapAsync(MakeNewBuisnessActivity.this);


            } catch (Exception e) {
                e.printStackTrace();
                Toast myToast = Toast.makeText(getApplicationContext(), "주소 확인중 에러가 발생하였습니다.", Toast.LENGTH_SHORT);
                myToast.show();
            }

        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.skip, menu); //툴바에 메뉴 설정
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_next://확인버튼

                String resultName = buisnessName.getText().toString();
                String resultOwner = buisnessOwner.getText().toString();
                String resultAddress = buisnessAddress.getText().toString();
                String resultPhonenum = buisnessPhonenum.getText().toString();
                String resultNumber = buisnessNumber.getText().toString();

                if (resultName.equals("") | resultOwner.equals("") | resultAddress.equals("") | resultPhonenum.equals("") | resultNumber.equals("")) {
                    Toast myToast = Toast.makeText(getApplicationContext(),
                            "정보를 모두 입력바랍니다.", Toast.LENGTH_SHORT);
                    myToast.show();
                } else if (x == null || y == null) {
                    Toast myToast = Toast.makeText(getApplicationContext(),
                            "주소를 검색해주시기 바랍니다.", Toast.LENGTH_SHORT);
                    myToast.show();
                } else {
                    try {
                        //사업자등록 확인하기
                        BuisnessAuthTask task = new BuisnessAuthTask(this);

                        task.execute(resultNumber);

                        // 동기로 진행된다. task가 성공하면 값을 return 받는다.
                        // 만약 error가 발생하면 callBackValue에 Error : 가 포함된다.
                        String callBackValue = task.get();
                        if (callBackValue.contains("Error")) {
                            Toast myToast = Toast.makeText(getApplicationContext(), "사업자등록번호 확인중 에러가 발생하였습니다.", Toast.LENGTH_SHORT);
                            myToast.show();
                        }

                        // 받아온 데이터 json으로
                        JSONArray result = new JSONObject(callBackValue).getJSONArray("data");
                        JSONObject data = (JSONObject) result.get(0);
                        String taxType = data.getString("tax_type");

                        if (taxType.contains("국세청에 등록되지 않은 사업자등록번호입니다")) {
                            Toast myToast = Toast.makeText(getApplicationContext(), "국세청에 등록되지 않은 사업자등록번호입니다.", Toast.LENGTH_SHORT);
                            myToast.show();
                        } else {  // 정상
                            // DB에 저장
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("buisnessName", resultName);
                            map.put("buisnessOwner", resultOwner);
                            map.put("buisnessAddress", resultAddress);
                            map.put("buisnessPhonenum", resultPhonenum);
                            map.put("buisnessNumber", resultNumber);
                            map.put("latitude", y);
                            map.put("longitude", x);

                            if (ischange) {  //db에 저장하고 끝
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                db.collection("Groups").document(groupID).update(map);
                                finish();

                            } else {  //init -> makenewGroupActivity로 넘어가기
                                Intent intent = new Intent(this, MakeNewGroupActivity.class);
                                intent.putExtra("buisness", map);


                                startActivity(intent);
                                finish();
                            }
                        }
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                        Log.d("사업자 확인", "ExecutionException");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.d("사업자 확인", "InterruptedException");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }

                break;
            case R.id.menu_skip://건너뛰기
                Intent intent = new Intent(this, MakeNewGroupActivity.class);
                intent.putExtra("buisness", new HashMap<String,Object>());


                startActivity(intent);
                finish();

                break;
            case android.R.id.home://뒤로가기 버튼
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {

        //기존의 마커 없애기
        marker.setMap(null);

        marker.setPosition(new LatLng(y, x));
        marker.setMap(naverMap);

        CameraPosition cameraPosition = new CameraPosition(new LatLng(y, x), 16, 0, 0);
        naverMap.setCameraPosition(cameraPosition);
    }


    @SuppressLint("StaticFieldLeak")
    class BuisnessAuthTask extends AsyncTask<String, Void, String> {


        public Context mContext;

        public BuisnessAuthTask(Context mContext) {
            super();
            this.mContext = mContext;
        }

        @Override
        protected String doInBackground(String... strings) {

            // 전송할 데이터
            String value = (String) strings[0];
            String postParameters = "{\"b_no\":[\"" + value + "\"]}";
            byte[] sendData = postParameters.getBytes(StandardCharsets.UTF_8);
            Log.d("BuisnessAuthTask", postParameters);

            try {
                // HttpURLConnection 클래스를 사용하여 POST 방식으로 데이터를 전송합니다.
                URL url = new URL("https://api.odcloud.kr/api/nts-businessman/v1/status?serviceKey=M52t5AL260wqnm8jFS%2F4k8oKuS0VpEoOTup5sBRoJ6xUG%2BvTi5hqMmB6uq0F6Dvqoepo5gJEhDnWWSSSFiHTgA%3D%3D\n");

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000); //5초안에 응답이 오지 않으면 예외가 발생합니다.
                httpURLConnection.setConnectTimeout(5000); //5초안에 연결이 안되면 예외가 발생합니다.
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setRequestProperty("accept", "application/json");
                httpURLConnection.setRequestProperty("Authorization", "M52t5AL260wqnm8jFS/4k8oKuS0VpEoOTup5sBRoJ6xUG+vTi5hqMmB6uq0F6Dvqoepo5gJEhDnWWSSSFiHTgA==");
                httpURLConnection.setRequestMethod("POST"); //요청 방식 POST
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(sendData); //전송할 데이터 전송
                outputStream.flush();
                outputStream.close();

                // 응답을 읽습니다.
                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d("BuisnessAuthTask", "POST response code - " + responseStatusCode);

                InputStream inputStream;
                if (responseStatusCode == HttpURLConnection.HTTP_OK) {  // 정상적인 응답 데이터
                    inputStream = httpURLConnection.getInputStream();
                } else {        // 에러 발생
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);  // 서버에서 읽어오기 위한 스트림 객체
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);      // 줄단위로 읽어오기 위해 BufferReader로 감싼다.

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {        // 반복문 돌면서읽어오기
                    sb.append(line);
                }
                bufferedReader.close();


                return sb.toString();


            } catch (Exception e) {

                Log.d("AsyncTask", "InsertData: Error ", e);

                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //다이얼로그 띄우는데 이건 나중에 하자
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Log.d("AsyncTask", "POST response - " + s);
        }

    }


    @SuppressLint("StaticFieldLeak")
    class AddressTask extends AsyncTask<String, Void, String> {


        public Context mContext;

        public AddressTask(Context mContext) {
            super();
            this.mContext = mContext;
        }

        @Override
        protected String doInBackground(String... strings) {
            String value = (String) strings[0];

            String postParameters = "{\"query\":\"" + value + "\" , \"coordinate=127.1054328,37.3595963\"}";
            byte[] sendData = postParameters.getBytes(StandardCharsets.UTF_8);


            try {
                URL url = new URL("https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=" + URLEncoder.encode(value, "UTF-8"));

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000); //5초안에 응답이 오지 않으면 예외가 발생합니다.
                httpURLConnection.setConnectTimeout(5000); //5초안에 연결이 안되면 예외가 발생합니다.
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setRequestProperty("accept", "application/json");
                httpURLConnection.setRequestProperty("X-NCP-APIGW-API-KEY-ID", "7lt7qeokii");
                httpURLConnection.setRequestProperty("X-NCP-APIGW-API-KEY", "EV4vnCOFRbR4i2VN7SQJfAc1kN5V1iawJsqX5OZt");
                httpURLConnection.setRequestMethod("GET"); //요청 방식 GET


                // 응답을 읽습니다.
                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d("BuisnessAuthTask", "POST response code - " + responseStatusCode);

                InputStream inputStream;
                if (responseStatusCode == HttpURLConnection.HTTP_OK) {  // 정상적인 응답 데이터
                    inputStream = httpURLConnection.getInputStream();
                } else {        // 에러 발생
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);  // 서버에서 읽어오기 위한 스트림 객체
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);      // 줄단위로 읽어오기 위해 BufferReader로 감싼다.

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {        // 반복문 돌면서읽어오기
                    sb.append(line);
                }
                bufferedReader.close();

                return sb.toString();


            } catch (Exception e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();

            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //다이얼로그 띄우는데 이건 나중에 하자
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Log.d("AsyncTask", "POST response - " + s);
        }

    }

}

