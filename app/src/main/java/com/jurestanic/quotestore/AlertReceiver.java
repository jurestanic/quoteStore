package com.jurestanic.quotestore;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static com.jurestanic.quotestore.BaseApp.CHANNEL_1_ID;

public class AlertReceiver extends BroadcastReceiver {

    // ZA NOTIFIKACIJE
    private NotificationManagerCompat nmc;
    private String quote, author;
    private int rand;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private DatabaseReference db;

    private ArrayList<Quote> quoteList;
    private Context context;


    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

         if(HomeFragment.exampleList == null)
            fetchData();
         else
            not(context);

    }

    private void not(Context context){
        nmc = NotificationManagerCompat.from(context);
        Intent action = new Intent(context,MainActivity.class);
        PendingIntent notiIntent = PendingIntent.getActivity(
                context,
                0,
                action,
                0
        );

        if(HomeFragment.exampleList == null) {
            rand = new Random().nextInt(quoteList.size());
            quote = quoteList.get(rand).getQuote();
            author = quoteList.get(rand).getAuthor();
        } else {
            rand = new Random().nextInt(HomeFragment.exampleList.size());
            quote = HomeFragment.exampleList.get(rand).getQuote();
            author = HomeFragment.exampleList.get(rand).getAuthor();
        }

        Notification noti = new NotificationCompat.Builder(context,CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_quote)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setColor(13382987)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(quote))
                .setContentTitle(author + ":")
                .setContentIntent(notiIntent)
                .setAutoCancel(true)
                .build();

        nmc.notify(1, noti);

    }

    private void fetchData(){
        quoteList = new ArrayList<>();
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        db = database.getReference(user.getUid());

        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    if(ds.exists())
                        quoteList.add(0,ds.getValue(Quote.class));
                }
                not(context);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
