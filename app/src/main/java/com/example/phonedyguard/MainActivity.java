package com.example.phonedyguard;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.phonedyguard.Board.BoardActivity;
import com.example.phonedyguard.map.Navigation;
import com.example.phonedyguard.sign_in.LoginActivity;
import com.example.phonedyguard.sign_up.RegisterActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.security.MessageDigest;

public class MainActivity extends AppCompatActivity {

    //해쉬키 얻는 함수
    private void getAppKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                Log.e("Hash key", something);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e("name not found", e.toString());
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first);
        getAppKeyHash();

        Button signbt = (Button) findViewById(R.id.loginButton);
        Button signupbt = (Button) findViewById(R.id.signButton);

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        else Log.d(TAG, "Fetching FCM registration token success");

                        // Get new FCM registration token
                        String token = task.getResult();

                        // 내부 저장소에 안드로이드 기기 고유 token 저장
                        SharedPreferences sharedPreferences_fire = getSharedPreferences("tokenDB_fire", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences_fire.edit();
                        editor.putString("token_fire", token);
                        editor.commit();

                        // Log and toast
                        String msg = "FCM registration Token: " + token;
                        Log.d(TAG, msg);
                    }
                });

        signbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        signupbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });

    }
}