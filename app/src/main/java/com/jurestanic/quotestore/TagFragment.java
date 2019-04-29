package com.jurestanic.quotestore;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.Chip;
import com.pchmn.materialchips.model.ChipInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TagFragment extends Fragment {

    private RecyclerView tagListView, quoteListView;

    private ArrayList<Quote> quoteList = new ArrayList<>();
    private ArrayList<Chip> tagList = new ArrayList<>();

    private LinearLayoutManager tagListManager, quoteListManager;
    private QuoteAdapter quoteAdapter;
    private TagAdapter tagAdapter;

    // addDialog i editDialog
    private View addView,editView;
    private AlertDialog addDialog,editDialog;
    private EditText addQuote,addAuthor,editQuote, editAuthor;

    // dodavanje i editovanje tagova
    private MyChipsInput chipsInput, editChips;

    // potrebno za CRUD operacije nad podatcima ( ova db se dohvaca iz MainActivity-a)
    private DatabaseReference db;

    // menu buttons
    private MenuItem removeBtn;
    private  MenuItem editBtn;

    private Quote selectedItem;
    private View selectedView;

    // pos selektiranog itema
    private  int itemPos = -1;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        db = MainActivity.db;

        View myView = inflater.inflate(R.layout.tag_layout, container, false);
        final String args = getArguments().getString("params");

        for(Quote q : MainActivity.quoteList){
            Chip c = null;
            assert args != null;
            if(args.equals("tags")){
                c = new Chip(q.getTag(),null);
            } else if(args.equals("authors")){
                c = new Chip(q.getAuthor(),null);
            }

            boolean unique = true;
            for(Chip chip : tagList){
                assert c != null;
                if(chip.getLabel().equals(c.getLabel())) {
                    unique = false;
                }
            }
            if(unique) tagList.add(c);
            //   quoteList.add(q);
        }

        quoteListView = myView.findViewById(R.id.quoteList);

        quoteListView.setHasFixedSize(true);
        quoteListManager = new LinearLayoutManager(getContext());
        quoteAdapter = new QuoteAdapter(quoteList);

        quoteListView.setLayoutManager(quoteListManager);
        quoteListView.setAdapter(quoteAdapter);

        quoteAdapter.setOnItemClickListener(new QuoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Quote item, int pos, View itemView) {
                selectedView = itemView;
                selectedItem = item;
                itemPos = pos;
                setSelected();
            }
        });

        tagListView = myView.findViewById(R.id.tagList);
        tagListView.setHasFixedSize(true);
        tagListManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        tagAdapter = new TagAdapter(tagList);
        tagAdapter.setOnItemClickListener(new TagAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Chip item, int pos, View itemView) {
                quoteList.clear();
                for(Quote q : MainActivity.quoteList){
                    if(args == "tags"){
                        if(q.getTag().equals(item.getLabel())) quoteList.add(q);
                    } else if(args == "authors"){
                        if(q.getAuthor().equals(item.getLabel())) quoteList.add(q);
                    }
                }
                quoteAdapter.notifyDataSetChanged();
            }
        });
        tagListView.setLayoutManager(tagListManager);
        tagListView.setAdapter(tagAdapter);

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
        MainActivity.quoteList.remove(position);
        quoteList.remove(position);
        setSelected();
        quoteAdapter.notifyItemRemoved(position);
    }

    private void editItem(int position, String quote, String author, String tag) {
        if(author.isEmpty()) author = "Anonymous";
        if(tag.isEmpty()) tag = "untagged";

        quoteList.get(position).setQuote(quote);
        quoteList.get(position).setAuthor(author);
        quoteList.get(position).setTag(tag);

        db.child(quoteList.get(position).getQuoteID()).setValue(quoteList.get(position));
        setSelected();
        quoteAdapter.notifyItemChanged(position);

    }

    private void setSelected() {
        if(selectedItem.getIsSelected()) {
            // unselektiranje itema ako je selektiran
            select(selectedView, false);
        } else {
            // prilikom selektiranja itema, unselektiraj sve iteme prvo pa onda taj selektiraj (samo jedan moze bit selektiran)
            for(int i = 0; i< quoteList.size(); i++){
                if(quoteList.get(i).getIsSelected()) {
                    select(quoteListManager.findViewByPosition(i),false);
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
}
