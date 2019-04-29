package com.jurestanic.quotestore;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

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

    private ArrayList<Quote> quoteList;
    private Context context;
    public static String tagFilter,authorFilter;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        // ako bude problema vratiti na MainActivity.quotelist, jer se homeFragment unistava kad se prebaci na drugi fragment (valjda), ali za sad radi
        if(MainActivity.quoteList == null) {
            fetchData();
        } else {
            this.quoteList = new ArrayList<>(MainActivity.quoteList);
            not(context);
        }

    }
    private void not(Context context){
        NotificationManagerCompat nmc = NotificationManagerCompat.from(context);
        String quote, author;
        int rand;

        Intent action = new Intent(context,MainActivity.class);
        PendingIntent notiIntent = PendingIntent.getActivity(
                context,
                0,
                action,
                0
        );

        // Get from the SharedPreferences
        SharedPreferences settings = context.getSharedPreferences("Filters",0);
        tagFilter = settings.getString("tagFilter", "any");
        authorFilter = settings.getString("authorFilter","any");

        if(!tagFilter.equals("any")) {
            ArrayList<Quote> removeList = new ArrayList<>();
            for (Quote q : quoteList) {
                if (!q.getTag().equals(tagFilter)) removeList.add(q);
            }
            quoteList.removeAll(removeList);
        }

        if(!authorFilter.equals("any")) {
            ArrayList<Quote> removeList = new ArrayList<>();
            for (Quote q : quoteList) {
                if (!q.getAuthor().equals(authorFilter)) removeList.add(q);
            }
            quoteList.removeAll(removeList);
        }

        rand = new Random().nextInt(quoteList.size());
        quote = quoteList.get(rand).getQuote();
        author = quoteList.get(rand).getAuthor();

        // stvaranje notifikacija
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

    // hvatanje podataka kada je apk ugasena
    private void fetchData(){
        quoteList = new ArrayList<>();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        assert user != null;
        DatabaseReference db = FirebaseDatabase.getInstance().getReference(user.getUid());

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
