package com.raj.grs.accidentalertsystem;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
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

public class MyService extends Service {

    NotificationManager notificationManager;
    String longitude,latitude;
    String GyroX,GyroY;
    float GyroXflaot,GyroYfloat;
   // StatusScreen SC;

    public MyService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onTaskRemoved(intent);



        // SC = new StatusScreen();

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

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                //performe the deskred task
              // Toast.makeText(getApplicationContext(),"Vehicle Monitoring is Live",Toast.LENGTH_SHORT).show();
                LoadJSON task = new LoadJSON();
                task.execute(uriapi01.getUri());
            }
        }, 5000);

    /*    Timer timer = new Timer();
        TimerTask tasknew = new TimerTask(){
            public void run() {
                LoadJSON task = new LoadJSON();
                task.execute(uriapi01.getUri());
            }
        };
        timer.scheduleAtFixedRate(tasknew,1*5000,1*5000);
        */

        return START_STICKY;
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


    @Override
    public boolean stopService(Intent name) {
        stopSelf();
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(getApplicationContext(),"Accident Alert System Offline", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartService = new Intent(getApplicationContext(),this.getClass());
        restartService.setPackage(getPackageName());
        startService(restartService);
        super.onTaskRemoved(rootIntent);
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
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
                GyroX = String.format("%s", json.getString("field1"));
                longitude = String.format("%s", json.getString("field3"));
                latitude  = String.format("%s", json.getString("field2"));
                //GYRO VALUES :  STRING TO FLOAT
                GyroXflaot = Float.parseFloat(GyroX);

                //ON ACCIDENT DETECTION
                if(GyroX.equals("3"))
                {
                    //SC.statusBar.setBackgroundResource(R.drawable.riskanimations);
                    //HIGH PRIORITY NOTIFICATION
                    Intent intent1 = new Intent(getApplicationContext(), StatusScreen.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 123, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(),"id_product")
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



                }
                else
                {
                    //SC.statusBar.setBackgroundResource(R.drawable.safeanimations);
                    notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancelAll();
                }

            } catch (JSONException e)
            {
                e.printStackTrace();
            }
            Log.d("X",""+longitude);
            Log.d("Y",""+latitude);

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
}
