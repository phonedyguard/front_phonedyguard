package com.example.phonedyguard.Board;

import com.google.gson.annotations.SerializedName;

public class getBoard {
    @SerializedName("title") //서버 변수
    private String Title; //클라이언트 변수
    @SerializedName("number")
    private long num;
    @SerializedName("content")
    private String Content;

    public getBoard(String Title, long num, String Content)
    {
        this.Content = Content;
        this.Title = Title;
        this.num = num;

    }
    public String getContent() { return Content; }
    public String getTitle() {
        return Title;
    }
    public long getNum() {
        return num;
    }
}