package com.example.phonedyguard.map;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.phonedyguard.Board.BoardActivity;
import com.example.phonedyguard.Board.PostBoard;
import com.example.phonedyguard.Board.listInterface;
import com.example.phonedyguard.Board.registInterface;
import com.example.phonedyguard.MainDisplay;
import com.example.phonedyguard.R;
import com.example.phonedyguard.User.UserInfo;
import com.example.phonedyguard.sign_up.RegisterActivity;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Navigation extends AppCompatActivity
        implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback {
    private GpsTracker gpsTracker;
    private GoogleMap mMap;
    private final String BASEURL = "http://3.36.109.233/"; //url
    private map_restful MapRestful;
    boolean webRoad = false;  //???????????? ?????? ?????? ??? ???????????? ??????

    String token = ((MainDisplay) MainDisplay.context_main).call_token;

    Timer timer;

    //?????? ??????
    double start_latitude;
    double start_longitude;

    //???????????? ??????
    double start_lat = 35.1101192644269;
    double start_lon = 128.9606150522155;
    double end_lat = 35.11127299455579;
    double end_lon = 128.96250151431215;
    private WebView mWebView;
    List<routes> result = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gpsTracker = new GpsTracker(Navigation.this);
        gpsTracker.getLocation();
        start_latitude = gpsTracker.getLatitude();
        start_longitude = gpsTracker.getLongitude();

        setContentView(R.layout.navigation);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map1);
        mapFragment.getMapAsync(this);
        Button button = findViewById(R.id.Nav);


        //html ?????? ??????
        mWebView = (WebView) findViewById(R.id.webView);//xml ???????????? ??????
        mWebView.getSettings().setJavaScriptEnabled(true);//?????????????????? ??????
        //tmap ?????? JavascriptInterface ??? ????????? ?????????.
        mWebView.addJavascriptInterface(new WebViewJavascriptBridge(), "Android_tmap");

        mWebView.loadUrl("file:///android_asset/getroutes.html");


        //------------------------run
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASEURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        MapRestful = retrofit.create(map_restful.class);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Call<safe_routes> saferoute_call = MapRestful.get_saferoutes(token);

                saferoute_call.enqueue(new Callback<safe_routes>() {
                    @Override
                    public void onResponse(Call<safe_routes> call, Response<safe_routes> response) {
                        if (!response.isSuccessful()) {
                            Log.d("@@@: ", String.valueOf(response.code()));
                            return;
                        }

                        safe_routes safeRoutes = response.body(); //post??? ??? ?????????

                         start_lat = safeRoutes.getStart_lat();
                         start_lon = safeRoutes.getStart_lon();
                         end_lat = safeRoutes.getEnd_lat();
                         end_lon = safeRoutes.getEnd_lng();

                        Log.d("@@@", ".?????? startx " +safeRoutes.getStart_lat()+ "sy " + safeRoutes.getEnd_lng()
                            + "e_x " + safeRoutes.getEnd_lat() + "e_y " + safeRoutes.getEnd_lng());
                        mWebView.loadUrl("javascript:initTmap(" + start_lat + ", " + start_lon + ", " + end_lat + ", " + end_lon + ")");
                        mapThread thread = new mapThread();
                        thread.start();
                    }

                    @Override
                    public void onFailure(Call<safe_routes> call, Throwable t) {
                        Log.d("msg", t.getMessage()); //?????? ?????? ?????????
                    }
                });
            }
        });


    }

    class WebViewJavascriptBridge {
        @JavascriptInterface
        public void setTest(final double testx, final double testy) {
            Log.d("test", "jstest" + testx + ", " + testy);
        }

        @JavascriptInterface
        public void getLatLng(final double[] lat, final double[] lng) {
            for (int i = 0; i < lat.length; i++) {
                result.add(new routes(lat[i], lng[i]));
                webRoad = true;
            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;

        LatLng latLng = new LatLng(start_latitude, start_longitude);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(15));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        } else {
            checkLocationPermissionWithRationale();
        }
        Start_Period();
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);


    }

    void darwPath(LatLng startLatLng, LatLng endLatLng) {
        PolylineOptions options = new PolylineOptions().add(startLatLng).add(endLatLng).width(5).color(Color.RED).geodesic(true);
        mMap.addPolyline(options);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Stop_Period();
        Log.d("Click", "onDestroy");
    }

    public void Start_Period() {
        timer = new Timer();
        timer.schedule(addTask, 0, 10000); //// 0?????? ?????????, Interval????????? ????????????
    }

    public void Stop_Period() {
        //Timer ?????? ??????
        if (timer != null) timer.cancel();
    }


    private Handler handler;
    TimerTask addTask = new TimerTask() {
        @Override
        public void run() {
            //??????????????? ????????? ?????? ??????
            createPost();
        }
    };

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private void checkLocationPermissionWithRationale() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("????????????")
                        .setMessage("??? ?????? ???????????? ???????????? ??????????????? ????????? ???????????????. ???????????? ????????? ???????????? ?????????.")
                        .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(Navigation.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        }).create().show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        gpsTracker = new GpsTracker(Navigation.this);

        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();

        Toast.makeText(Navigation.this, "???????????? \n?????? " + latitude + "\n?????? " + longitude, Toast.LENGTH_LONG).show();

        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG);
    }


    //?????? ?????? ??????
    private void createPost() {
        gpsTracker.getLocation();

        latlng_result latlngResult = new latlng_result(gpsTracker.getLatitude(), gpsTracker.getLongitude());

        Call<latlng_result> call = MapRestful.createPost(token, latlngResult);

        Log.d("@@@", " ??????????????????  : " + gpsTracker.getLatitude() + " , " + gpsTracker.getLongitude());
        call.enqueue(new Callback<latlng_result>() {
            @Override
            public void onResponse(Call<latlng_result> call, Response<latlng_result> response) {
                if (!response.isSuccessful()) {
                    Log.d("@@@: ", String.valueOf(response.code()));
                    Log.d("@@@", "?????? lat : " + Double.toString(latlngResult.getLat()) + " lng" + Double.toString(latlngResult.getLng()));
                    return;
                }

                latlng_result latlngResponse = response.body(); //post??? ??? ?????????

                latlngResponse.getLat();
                latlngResponse.getLng();

                Log.d("@@@", " ?????? lattsese : " + Double.toString(latlngResult.getLat()) + " lng" + Double.toString(latlngResult.getLng()));
            }

            @Override
            public void onFailure(Call<latlng_result> call, Throwable t) {
                Log.d("msg", t.getMessage()); //?????? ?????? ?????????
            }
        });
    }

    public class mapThread extends Thread {

        public mapThread( ){ }
        @Override
        public void run() {

            (Navigation.this).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        if (webRoad) {
                            for (int i = 0; i < result.size() - 1; i++) {
                                Log.d("?????????", "" + result.get(i).getLat() + ", " + result.get(i).getLng());
                                darwPath(new LatLng(result.get(i).getLat(), result.get(i).getLng()),
                                        new LatLng(result.get(i + 1).getLat(), result.get(i + 1).getLng()));
                            }
                            webRoad = false;
                            break;
                        }
                    }// while end

                }
            });


        }//run end

    }//Thread end



}// Nav..class end

