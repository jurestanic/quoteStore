package com.jurestanic.quotestore;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ReminderFragment extends DialogFragment implements AdapterView.OnItemSelectedListener, TimePickerDialog.OnTimeSetListener{


    private ArrayList<String> tagList = new ArrayList<>();
    private ArrayList<String> authorList = new ArrayList<>();

    private TextView timeTxt;

    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    public String tagFilter, authorFilter;
    private int setSelectedCounter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // stvaranje time pickera
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        return new TimePickerDialog(getActivity(),this,hour,minute, DateFormat.is24HourFormat(getContext()));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        timeTxt = getActivity().findViewById(R.id.setTimeTxt);

        timeTxt.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));

        editor = settings.edit();

        editor.putInt("hour", hourOfDay);
        editor.putInt("minute", minute);
        editor.apply();
    }

    private void startAlarm(Calendar c) {

        AlarmManager alarmManager = (AlarmManager) Objects.requireNonNull(getActivity()).getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getContext(),AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(),1,intent,0);

        if(c.before(Calendar.getInstance())){
            c.add(Calendar.DATE,1);
        }
        // implementirati mogucnost svakodnevnog ponavljanja,
        // kao i random svakodnevnog ponavljanja ako korisnik ne postavi nista
        // staviti postavljanje remindera s obzirom na tag ili autora
        // staviti mozda i mogucnost odabira dana kojima se ponavlja
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), 24*60*60*1000, pendingIntent);
    }

    private  void cancelAlarm(){
        AlarmManager alarmManager = (AlarmManager) Objects.requireNonNull(getActivity()).getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getContext(),AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(),1,intent,0);

        alarmManager.cancel(pendingIntent);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        settings = getContext().getSharedPreferences("Filters", 0);

        tagList.add("any");
        for(Quote q : MainActivity.quoteList) {
            boolean unique = true;
            for (String tag : tagList) {
                if (tag.equals(q.getTag())) unique = false;
            }
            if(unique) tagList.add(q.getTag());
        }

        authorList.add("any");
        for(Quote q : MainActivity.quoteList) {
            boolean unique = true;
            for (String author : authorList) {
                if (author.equals(q.getAuthor())) unique = false;
            }
            if(unique) authorList.add(q.getAuthor());
        }


        View myView = inflater.inflate(R.layout.reminder_layout,container,false);
        timeTxt = myView.findViewById(R.id.setTimeTxt);

        ;
        final SharedPreferences settings = getContext().getSharedPreferences("Filters",0);
        timeTxt.setText( String.format("%02d", settings.getInt("hour",10)) + ":" + String.format("%02d", settings.getInt("minute",24)));

        final Spinner tagSpinner = myView.findViewById(R.id.tagSpinner);
        final ArrayAdapter<String> tagAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_spinner_item,tagList);
        tagAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tagSpinner.setAdapter(tagAdapter);
        tagSpinner.post(new Runnable() {
            @Override
            public void run() {
                 tagSpinner.setSelection(tagAdapter.getPosition(settings.getString("tagFilter","any")));
            }
        });
        tagSpinner.setOnItemSelectedListener(this);

        final Spinner authorSpinner = myView.findViewById(R.id.authorSpinner);
        final ArrayAdapter<String> authorAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_spinner_item,authorList);
        authorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        authorSpinner.setAdapter(authorAdapter);
        authorSpinner.post(new Runnable() {
            @Override
            public void run() {
                authorSpinner.setSelection(authorAdapter.getPosition(settings.getString("authorFilter","any")));
            }
        });
        authorSpinner.setOnItemSelectedListener(this);


        Button reminderBtn = myView.findViewById(R.id.reminderBtn);
        Button cancelBtn = myView.findViewById(R.id.cancelBtn);
        Button saveBtn = myView.findViewById(R.id.saveBtn);

        reminderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment timePicker = new ReminderFragment();
                assert getFragmentManager() != null;
                timePicker.show(getFragmentManager(),"time picker");
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, settings.getInt("hour",10));
                calendar.set(Calendar.MINUTE, settings.getInt("minute",24));
                calendar.set(Calendar.SECOND,0);
                startAlarm(calendar);
                Toast.makeText(getContext(),"Reminder Set!",Toast.LENGTH_SHORT).show();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelAlarm();
                Toast.makeText(getContext(),"Reminder Canceled!",Toast.LENGTH_SHORT).show();
            }
        });
        return myView;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        setSelectedCounter++;
        if(setSelectedCounter == 1) return;

        switch (parent.getId()){
            case R.id.tagSpinner:
                tagFilter = parent.getItemAtPosition(position).toString();

                if(!tagFilter.equals("any")) {
                    authorList.removeAll(authorList);
                    for (Quote q : MainActivity.quoteList) {
                        System.out.println(q.getTag() + " - " + tagFilter);
                        if (!q.getTag().equals(tagFilter)) continue;
                        boolean unique = true;
                        for (String author : authorList) {
                            System.out.println(q.getAuthor() + " - " + author);
                            if (author.equals(q.getAuthor())) unique = false;
                        }
                        if (unique) {

                            authorList.add(q.getAuthor());
                        }
                    }
                } else {
                    authorList.removeAll(authorList);
                    for(Quote q : MainActivity.quoteList) {
                        boolean unique = true;
                        for (String author : authorList) {
                            if (author.equals(q.getAuthor())) unique = false;
                        }
                        if(unique) authorList.add(q.getAuthor());
                    }
                }

                if(!authorList.contains("any")) authorList.add(0,"any");

                break;
            case R.id.authorSpinner:
                authorFilter = parent.getItemAtPosition(position).toString();

                if(!authorFilter.equals("any")) {
                    tagList.removeAll(tagList);
                    for (Quote q : MainActivity.quoteList) {
                        if (!q.getAuthor().equals(authorFilter)) continue;
                        boolean unique = true;
                        for (String tag : tagList) {
                            if (tag.equals(q.getTag())) unique = false;
                        }
                        if (unique) tagList.add(q.getTag());

                    }
                } else {
                    tagList.removeAll(tagList);
                    tagList.add("any");
                    for(Quote q : MainActivity.quoteList) {
                        boolean unique = true;
                        for (String tag : tagList) {
                            if (tag.equals(q.getTag())) unique = false;
                        }
                        if(unique) tagList.add(q.getTag());
                    }
                }
                if(!tagList.contains("any")) tagList.add(0,"any");
                break;
        }

        editor = settings.edit();
        editor.putString("tagFilter", tagFilter);
        editor.putString("authorFilter", authorFilter);
        editor.apply();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }



}
