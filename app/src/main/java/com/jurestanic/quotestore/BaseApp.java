package com.jurestanic.quotestore;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import com.google.firebase.database.FirebaseDatabase;


public class BaseApp extends Application {

    public static final String CHANNEL_1_ID = "Silent Notification";

    @Override
    public void onCreate() {
        super.onCreate();
        // Omogucava offline koristenje. Sve operacije izvrsene u offline stanju ce se kasnije updateati na firebase kada se korisnik spoji na internet.
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        // stavaranje notifikacija
        createNoti();
    }

    private void createNoti() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Silent Notification",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel1.setDescription("Silent notifications");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
        }
    }


}
