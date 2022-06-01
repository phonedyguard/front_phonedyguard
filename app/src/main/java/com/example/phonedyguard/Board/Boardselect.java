package com.example.phonedyguard.Board;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.phonedyguard.MainDisplay;
import com.example.phonedyguard.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Boardselect extends AppCompatActivity {

    String token = ((MainDisplay)MainDisplay.context_main).call_token;
    long num = ((BoardActivity)BoardActivity.context_main).num; //게시판 위치 얻어옴

    private final String BASEURL = "http://3.36.109.233/"; //url
    private TextView title_et, content_et, id_et;
    private String id;
    private String check;
    private selectInterface selectInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.board_select);

        Button backbt = (Button) findViewById(R.id.back_button);
        Button modbt = (Button) findViewById(R.id.mod_button);
        Button delbt = (Button) findViewById(R.id.del_button);


        title_et = findViewById(R.id.title_et);
        content_et = findViewById(R.id.content_et);
        id_et = findViewById(R.id.id_et);

        if(id_et.getText() == "W"){//(로그인 일치 구분 값)
            delbt.setVisibility(View.VISIBLE);
            modbt.setVisibility(View.VISIBLE);}
        else{
            delbt.setVisibility(View.INVISIBLE);
            modbt.setVisibility(View.INVISIBLE);}

        backbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), BoardActivity.class);
                startActivity(intent);
            }
        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASEURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        selectInterface = retrofit.create(selectInterface.class);
        Call <getBoard> call = selectInterface.getData(token,num);

        call.enqueue(new Callback<getBoard>()  {
            @Override
            public void onResponse(Call<getBoard> call, Response<getBoard> response) {
                getBoard result = response.body();
                title_et.setText(result.getTitle());
                content_et.setText(result.getContent());
               // id_et.setText(result.getId());
                 id_et.setText(result.getCheck());
            }

            @Override
            public void onFailure(Call<getBoard> call, Throwable t) {

            }
        });
    }
}