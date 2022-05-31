package com.example.phonedyguard.sign_in;

import com.google.gson.annotations.SerializedName;

// 데이터 보내기 (로그인 정보)
public class Token_rtf {

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    public Token_rtf(String email , String password) {
        this.email = email;
        this.password = password;
    }

}