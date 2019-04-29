package com.jurestanic.quotestore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AccountFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.account_layout,container,false);

        TextView userNameTxt = myView.findViewById(R.id.userNameTxt);
        userNameTxt.setText(MainActivity.user.getDisplayName());

        Button changePass = myView.findViewById(R.id.changePassBtn);
        Button syncQuotes = myView.findViewById(R.id.syncBtn);
        Button deleteAllQuotes = myView.findViewById(R.id.deleteAllQuotesBtn);

        changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // CHANGE PASS
            }
        });

        syncQuotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // SYNC
            }
        });

        deleteAllQuotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // DELETE ALL QUOTES
            }
        });

        return myView;
    }
}
