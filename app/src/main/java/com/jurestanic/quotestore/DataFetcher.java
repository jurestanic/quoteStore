package com.jurestanic.quotestore;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import androidx.annotation.NonNull;


class DataFetcher {

    // arraylist za spremanje podataka
    private ArrayList<Quote> quoteList = new ArrayList<>();
    private HomeFragment hf;

    DataFetcher(DatabaseReference db, HomeFragment hf){
        this.hf = hf;
        fetchData(db);
    }

    // dohvacanje podataka
    private void fetchData(DatabaseReference db){
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                quoteList.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    if(ds.exists())
                        quoteList.add(0,ds.getValue(Quote.class));
                }
                // postavljanje quoteList u Mainu, kako bi kasnije mogao dohvatiti
                // te podatke iz nav drawera bez da opet dohvacam podatke sa servera
                MainActivity.quoteList = quoteList;
                MainActivity.loaded = true;

                // stavlja podatke u homeFragment i set-upa ga (samo na pocetku aplikacije)
                hf.quoteList = quoteList;
                hf.setAdapter();

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
