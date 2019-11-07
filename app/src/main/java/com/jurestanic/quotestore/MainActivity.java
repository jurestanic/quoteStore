package com.jurestanic.quotestore;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient mGoogleSignInClient;
    public static FirebaseUser user;

    private DrawerLayout drawer;
    public static DatabaseReference db;

    // potrebno za punjenje podataka kada se pristupa podacima iz
    // navbara (kako ne bi vazda hvatali podatke sa servera -> pogledati u DataFetcher primjenu kao i u HomeFragmentu(onCreate))
    public static ArrayList<Quote> quoteList;
    public static Quote qod;
    public static boolean loaded;

    public ActionBarDrawerToggle toggle;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // custom toolbar potreban za prikazivanje hamburger icona (drawera)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // omogucava otvaranje nav drawera i animaciju hamburger icona.
        drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this,drawer,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // nav drawer xml
        NavigationView navView= findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);


        // Potrebno za dohvacanje podataka sa servera
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        db = FirebaseDatabase.getInstance().getReference(user.getUid());

        // stavaranje homeFragmenta i dohvacanje podataka sa servera
        HomeFragment hf = new HomeFragment();
        new DataFetcher(db, hf);


        // stavlja homeFragment za pocetni fragment kada se udje u app.
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                hf).commit();
        navView.setCheckedItem(R.id.nav_home);


        // potrebno za logout iz nav drawera
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            // ako je drawer otvoren i pritisne se back, zatvori se drawer
            drawer.closeDrawer(GravityCompat.START);
        } else {
            // izlazak iz apk kada je drawer zatvoren (mozda implementirati povratak na home fragment pa onda gasenje !!!)
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    // handlanje nav drawer buttona (Potrebno napraviti odgovarajuce fragmente !!!!!!)
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        TagFragment tf = new TagFragment();
        Bundle bundle = new Bundle();
        switch (menuItem.getItemId()){
            case R.id.nav_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new HomeFragment()).commit();
                break;
            case R.id.nav_reminder:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ReminderFragment()).commit();
                break;
            case R.id.nav_all:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new AllQuotesFragment()).commit();
                break;
            case R.id.nav_tag:
                bundle.putString("params", "tags");
                tf.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        tf).commit();
                break;
            case R.id.nav_authors:
                bundle.putString("params", "authors");
                tf.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        tf).commit();
                break;
            case R.id.nav_buy:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ReminderFragment()).commit();
                break;
            case R.id.nav_about:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ReminderFragment()).commit();
                break;
            case R.id.nav_share:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String shareTitle = "Quote Store App";
                String shareBody = "Jurica Purica";
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareTitle);
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(shareIntent, "Share the app"));
                break;
            case R.id.nav_account:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new AccountFragment()).commit();
                break;
            case R.id.nav_logout:
                firebaseAuth.signOut();
                if(mGoogleSignInClient != null) mGoogleSignInClient.signOut();
                if(LoginManager.getInstance() != null) LoginManager.getInstance().logOut();
                finish();
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
