package com.raj.grs.accidentalertsystem;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import static android.app.NotificationManager.IMPORTANCE_HIGH;

public class StatusScreen extends AppCompatActivity {

    RelativeLayout statusBar;
    AnimationDrawable animator;
    ImageView red,attention;
    TextView offlinealert;
    FloatingActionButton locate;
    Switch offline;
    String longitude="0",latitude="0";
    String GyroX="0",GyroY;
    float GyroXflaot,GyroYfloat;
    NotificationManager notificationManager;

    boolean termination=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_screen);
        setTitle("Accident Alert System");
       // startService(new Intent(this,MyService.class));


        locate = (FloatingActionButton)findViewById(R.id.locate);
        offline = (Switch)findViewById(R.id.switch1);
        offlinealert = (TextView)findViewById(R.id.textView) ; offlinealert.setVisibility(View.GONE);
        red = (ImageView)findViewById(R.id.imageView4);     red.setVisibility(View.GONE);
        attention = (ImageView)findViewById(R.id.imageView5); attention.setVisibility(View.GONE);
        offline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!offline.isChecked())
                {
                    offlinealert.setVisibility(View.GONE);
                    red.setVisibility(View.GONE);
                    attention.setVisibility(View.GONE);
                   // termination = false;
                }
                if(offline.isChecked())
                {
                    offlinealert.setVisibility(View.VISIBLE);
                    red.setVisibility(View.VISIBLE);
                    attention.setVisibility(View.VISIBLE);
                   // termination = true;
                    if(isMyServiceRunning(MyService.class))
                    {
                        Toast.makeText(getApplicationContext(),"Stop Monitoring Services",Toast.LENGTH_SHORT).show();
                       // stopService(new Intent(StatusScreen.this, MyService.class));
                    }
                }

            }
        });


        statusBar = (RelativeLayout)findViewById(R.id.relativeStatusBar);
        animator = (AnimationDrawable)statusBar.getBackground();
        animator.setEnterFadeDuration(2000);
        animator.setExitFadeDuration(2000);
        animator.start();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
             notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            String description = "Family vehicle Accident Alert System";
            NotificationChannel mChannel = new NotificationChannel("260398", "Accident Alert", IMPORTANCE_HIGH);
            // Configure the notification channel.
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            mChannel.setLightColor(Color.RED);
            notificationManager.createNotificationChannel(mChannel);
        }


        String url = "https://api.thingspeak.com/channels/518071/feeds/last.json?api_key=";
        String apikey = "2VTBRXXDBVU9Y5YA";
        final UriApi uriapi01 = new UriApi();
        uriapi01.setUri(url,apikey);

        LoadJSON task = new LoadJSON();
        task.execute(uriapi01.getUri());


        locate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                LoadJSON task = new LoadJSON();
                task.execute(uriapi01.getUri());
                Intent toMaps = new Intent(StatusScreen.this,LocatorScreen.class);
                Bundle bundle = new Bundle();
                bundle.putString("lat",latitude);
                bundle.putString("long",longitude);
                toMaps.putExtras(bundle);
                startActivity(toMaps);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
       // stopService(new Intent(this,MyService.class));
        if (animator != null && !animator.isRunning())
            animator.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!offline.isChecked()) {      startService(new Intent(this,MyService.class));   }
        else stopService(new Intent( this, MyService.class));
        if (animator != null && animator.isRunning())
            animator.stop();
    }

    private class UriApi {
        private String uri,url,apikey;
        protected void setUri(String url, String apikey){
            this.url = url;
            this.apikey = apikey;
            this.uri = url + apikey;
        }
        protected  String getUri(){
            return uri;
        }
    }

    private class LoadJSON extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            return getText(urls[0]);
        }

        @Override
        protected void onPostExecute(String result) {


            try {
                JSONObject json = new JSONObject(result);
                GyroX = String.format("%s", json.getString("field1")); Log.d("gyro",GyroX);
                longitude = String.format("%s", json.getString("field3")); //Log.d("x",longitude);
                latitude  = String.format("%s", json.getString("field2")); //.d("y",latitude);
                //GYRO VALUES :  STRING TO FLOAT
               // GyroXflaot = Float.parseFloat(GyroX);

                //ON ACCIDENT DETECTION
                if(GyroX.equals("5"))
                {
                statusBar.setBackgroundResource(R.drawable.riskanimations);
                //HIGH PRIORITY NOTIFICATION
                    Intent intent1 = new Intent(getApplicationContext(), StatusScreen.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 123, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
                  /*  NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(),"id_product")
                            .setSmallIcon(R.drawable.car) //your app icon
                            .setBadgeIconType(R.drawable.car) //your app icon
                            .setChannelId("260398")
                            .setContentTitle("Accident Occurrence Detected")
                            .setAutoCancel(true).setContentIntent(pendingIntent)
                            .setNumber(1)
                            .setColor(255)
                            .setContentText("Your family vehicle possibly got into an accident")
                            .setWhen(System.currentTimeMillis());
                    notificationManager.notify(1, notificationBuilder.build());


                */
                }
                else
                {
                    statusBar.setBackgroundResource(R.drawable.safeanimations);
                }

            } catch (JSONException e)
            {
                e.printStackTrace();
            }


        }
    }

    private String getText(String strUrl) {
        String strResult = "";
        try {
            URL url = new URL(strUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            strResult = readStream(con.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strResult;
    }

    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
       // stopService(new Intent(this,MyService.class));
        startActivity(intent);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}


