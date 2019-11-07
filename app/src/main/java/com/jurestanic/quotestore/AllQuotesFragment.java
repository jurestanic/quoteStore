package com.jurestanic.quotestore;


import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.database.DatabaseReference;
import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.Chip;
import com.pchmn.materialchips.model.ChipInterface;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.asynclayoutinflater.view.AsyncLayoutInflater;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class AllQuotesFragment extends Fragment {

    // potrebno za listu
    private RecyclerView mRecyclerView;
    private QuoteAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Quote selectedItem;
    private View selectedView;

    // pos selektiranog itema
    private  int itemPos = -1;

    static ArrayList<Quote> quoteList = new ArrayList<>();

    // menu buttons
    private  MenuItem removeBtn;
    private  MenuItem editBtn;

    private View editView;
    private AlertDialog editDialog;
    private EditText editQuote, editAuthor;


    // dodavanje i editovanje tagova
    private MyChipsInput editChips;

    // potrebno za CRUD operacije nad podatcima ( ova db se dohvaca iz MainActivity-a)
    private DatabaseReference db;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://quotes.rest/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        QuotesApi quotesApi = retrofit.create(QuotesApi.class);
        Call<ApiQuote> call = quotesApi.getApiQuotes();
        call.enqueue(new Callback<ApiQuote>() {
            @Override
            public void onResponse(Call<ApiQuote> call, Response<ApiQuote> response) {
                ApiQuote q = response.body();
                System.out.println(q);
                Quote quotePrep = new Quote();
                quotePrep.setQuote(q.getContents().getQuotes()[0].getQuote());
                quotePrep.setAuthor(q.getContents().getQuotes()[0].getAuthor());
                quotePrep.setTag(q.getContents().getQuotes()[0].getTags()[0]);
                quoteList.add(quotePrep);
                }
            @Override
            public void onFailure(Call<ApiQuote> call, Throwable t) {
                System.out.println(t.getMessage());
                Toast.makeText(getContext(),t.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // prikazivanje liste
        setHasOptionsMenu(true);

        db = MainActivity.db;

        View myView = inflater.inflate(R.layout.allquotes_layout,container,false);
        mRecyclerView = myView.findViewById(R.id.recyclerView);

        final androidx.appcompat.widget.SearchView searchView = myView.findViewById(R.id.searchBar);
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false);
            }
        });

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });

        // Ako je aplikacija vec pokrenuta ne dohvacaju se podaci sa servera nego iz MainActivitya
        if(MainActivity.loaded) {
           // quoteList = MainActivity.quoteList;
            buildRecycleView();
        }

        // editDialog view
        editView = inflater.inflate(R.layout.add_quote,container,false);
        // editovanje tagova dohvaceno iz editDialog
        editChips = editView.findViewById(R.id.chips_input);
        createEditDialog();

        return myView;
    }


    private void createEditDialog() {
        editQuote = editView.findViewById(R.id.quoteTxt);
        editQuote.setSingleLine(false);
        editAuthor  = editView.findViewById(R.id.authorTxt);

        final List<Chip> selectedTags = createTags(editChips);

        editDialog = new AlertDialog.Builder(getContext())
                .setView(editView)
                .setPositiveButton("edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton("cancel", null)
                .create();

        editDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button button = editDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                editQuote.requestFocus();
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (editQuote.getText().toString().isEmpty()) {
                            editQuote.requestFocus();
                            editQuote.setHint("Enter the quote!");
                        } else {
                            String tag;
                            if(editChips.getSelectedChipList().isEmpty())
                                tag = "";
                            else
                                tag = editChips.getSelectedChipList().get(0).getLabel();
                            editItem(quoteList.indexOf(selectedItem),String.valueOf(editQuote.getText()), String.valueOf(editAuthor.getText()), tag);
                            editQuote.setHint("Quote");
                            editQuote.setText("");
                            editAuthor.setText("");
                            editDialog.dismiss();
                        }

                    }
                });
            }
        });
    }

    private List<Chip> createTags(final ChipsInput chips){

        com.pchmn.materialchips.model.Chip c = new com.pchmn.materialchips.model.Chip("motivation",null);
        com.pchmn.materialchips.model.Chip c2 = new com.pchmn.materialchips.model.Chip("health",null);
        com.pchmn.materialchips.model.Chip c3 = new com.pchmn.materialchips.model.Chip("money",null);
        com.pchmn.materialchips.model.Chip c4 = new com.pchmn.materialchips.model.Chip("success",null);
        com.pchmn.materialchips.model.Chip c6 = new com.pchmn.materialchips.model.Chip("life",null);
        com.pchmn.materialchips.model.Chip c7 = new com.pchmn.materialchips.model.Chip("philosophy",null);

        final ArrayList<Chip> chipList = new ArrayList<>();
        chipList.add(c);
        chipList.add(c2);
        chipList.add(c3);
        chipList.add(c4);
        chipList.add(c6);
        chipList.add(c7);

        chips.addChipsListener(new ChipsInput.ChipsListener() {
            @Override
            public void onChipAdded(final ChipInterface chip, int newSize) {
                if(chips.getSelectedChipList().size() >= 1) {
                    chips.setEnabled(false);
                }
            }

            @Override
            public void onChipRemoved(ChipInterface chip, int newSize) {
                chips.setEnabled(true);
            }

            @Override
            public void onTextChanged(CharSequence text) {

                if(text.toString().contains(" ") || text.toString().contains(",")) {
                    Chip c = new Chip(text.toString().substring(0,text.length()-1),null);
                    chips.addChip(c);
                    chips.getChipView().setEnabled(false);
                }
            }
        });

        final List<Chip> selectedTags = (List<Chip>) chips.getSelectedChipList();
        chips.setFilterableList(chipList);

        return selectedTags;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.addbutton, menu);
        menu.findItem(R.id.addBtn).setVisible(false);
        menu.findItem(R.id.ocr).setVisible(false);
        removeBtn = menu.findItem(R.id.rmvBtn);
        editBtn = menu.findItem(R.id.editBtn);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.rmvBtn:
                removeItem(itemPos);
                break;
            case R.id.editBtn:
                editQuote.requestFocus();
                editQuote.setText(selectedItem.getQuote());
                editAuthor.setText(selectedItem.getAuthor());
                if(!selectedItem.getTag().isEmpty()) editChips.addChip(selectedItem.getTag(),null);
                editDialog.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void removeItem(int position) {
        db.child(quoteList.get(position).getQuoteID()).removeValue();
        quoteList.remove(position);
        setSelected();
        mAdapter.notifyItemRemoved(position);
    }

    private void editItem(int position, String quote, String author, String tag) {
        if(author.isEmpty()) author = "Anonymous";
        if(tag.isEmpty()) tag = "untagged";

        quoteList.get(position).setQuote(quote);
        quoteList.get(position).setAuthor(author);
        quoteList.get(position).setTag(tag);

        db.child(quoteList.get(position).getQuoteID()).setValue(quoteList.get(position));
        setSelected();
        mAdapter.notifyItemChanged(position);
    }

    private void setSelected() {
        if(selectedItem.getIsSelected()) {
            // unselektiranje itema ako je selektiran
            select(selectedView, false);
        } else {
            // prilikom selektiranja itema, unselektiraj sve iteme prvo pa onda taj selektiraj (samo jedan moze bit selektiran)
            for(int i = 0; i< quoteList.size(); i++){
                if(quoteList.get(i).getIsSelected()) {
                    select(mLayoutManager.findViewByPosition(i),false);
                }
            }
            // selektiraj item
            select(selectedView, true);
        }
    }

    private void select(View view, boolean isSelected){
        String colorString = isSelected ? "#b7b7b7" : "#FFFFFF";
        removeBtn.setVisible(isSelected);
        editBtn.setVisible(isSelected);
        ((CardView) view).setCardBackgroundColor(Color.parseColor(colorString));
        selectedItem.setIsSelected(isSelected);
    }

    void buildRecycleView() {
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new QuoteAdapter(quoteList);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new QuoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Quote item, int pos, View itemView) {
                selectedView = itemView;
                selectedItem = item;
                itemPos = pos;
                setSelected();
            }
        });
    }


}

