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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

public class HomeFragment extends Fragment implements View.OnClickListener{

    public ViewPager viewPager;
    private SliderAdapter sliderAdapter;

    public ArrayList<Quote> quoteList = new ArrayList<>();
    // addDialog i editDialog
    private View addView,editView;
    private AlertDialog addDialog,editDialog;
    private EditText addQuote,addAuthor,editQuote, editAuthor;

    // dodavanje i editovanje tagova
    private MyChipsInput chipsInput, editChips;

    // potrebno za CRUD operacije nad podatcima ( ova db se dohvaca iz MainActivity-a)
    private DatabaseReference db;

    // menu buttons
    private  MenuItem removeBtn;
    private  MenuItem editBtn;

    private FloatingActionButton editFab, deleteFab;

    //OCR
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1001;

    private String cameraPerm[];
    private String storagePerm[];

    private Uri image_uri;

    private Quote selectedItem;
    // pos selektiranog itema
    private  int itemPos = -1;

    View.OnClickListener myListener;
    private EditText quoteTxt, authorTxt;
    private View itemView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View myView = inflater.inflate(R.layout.slide_quote,container,false);
        itemView = inflater.inflate(R.layout.slide_quote_item,container,false);
        viewPager = myView.findViewById(R.id.viewPager);

        db = MainActivity.db;

        // addDialog i editDialog view
        addView = inflater.inflate(R.layout.add_quote,container,false);
        editView = inflater.inflate(R.layout.add_quote,container,false);
        // dodavanje i editovanje tagova dohvaceno iz addDialogi editDialog
        chipsInput = addView.findViewById(R.id.chips_input);
        editChips = editView.findViewById(R.id.chips_input);

        createAddDialog();
        createEditDialog();


        if(MainActivity.loaded) {
            quoteList = MainActivity.quoteList;
            setAdapter();
        }


        return myView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.addbutton, menu);
        removeBtn = menu.findItem(R.id.rmvBtn);
        editBtn = menu.findItem(R.id.editBtn);
    }

    public void setAdapter(){
        sliderAdapter = new SliderAdapter(getContext(),quoteList);
        sliderAdapter.setOnItemClickListener(new SliderAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int id, View view) {
                if(id == R.id.editFab){
                    editDialog(id,view);
                } else if(id == R.id.deleteFab){
                    removeItem(viewPager.getCurrentItem());
                } else if (id == R.id.shareFab){
                    shareQuote(view);
                }
            }
        });
        viewPager.setAdapter(sliderAdapter);
    }

    private void shareQuote(View v){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String shareTitle = "Quote Store App";
        String shareBody = quoteList.get(viewPager.getCurrentItem()).getQuote() + "\n" + "- " + quoteList.get(viewPager.getCurrentItem()).getAuthor();
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareTitle);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(shareIntent, "Share The Quote"));
    }

    private void editDialog(int pos, View view){
        quoteTxt = view.findViewById(R.id.slideQuoteTxt);
        authorTxt = view.findViewById(R.id.slideAuthorTxt);
        ImageView iv = view.findViewById(R.id.editFab);

        if(quoteTxt.isFocusableInTouchMode()){
            iv.setImageResource(R.drawable.ic_edit_black_24dp);
            editItem(pos, quoteTxt.getText().toString(),authorTxt.getText().toString());
            quoteTxt.setFocusableInTouchMode(false);
            authorTxt.setFocusableInTouchMode(false);
        } else {
            iv.setImageResource(R.drawable.ic_cancel);
            quoteTxt.setFocusableInTouchMode(true);
            authorTxt.setFocusableInTouchMode(true);

            quoteTxt.requestFocus();
            quoteTxt.setSelection(quoteTxt.length());
        }



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

    private void createAddDialog() {
        addQuote = addView.findViewById(R.id.quoteTxt);
        addQuote.setSingleLine(false);
        addAuthor  = addView.findViewById(R.id.authorTxt);

        final List<Chip> selectedTags = createTags(chipsInput);


        // ADD DIALOG
        addDialog = new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                .setView(addView)
                .setPositiveButton("add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton("cancel", null)
                .create();

        addDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {


                Button button = addDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                addQuote.requestFocus();

                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if(addQuote.getText().toString().isEmpty()) {
                            addQuote.requestFocus();
                            addQuote.setHint("Enter the quote!");
                        } else {
                            String tag;
                            if(chipsInput.getSelectedChipList().isEmpty())
                                tag = "";
                            else
                                tag = chipsInput.getSelectedChipList().get(0).getLabel();
                            addItem(String.valueOf(addQuote.getText()), String.valueOf(addAuthor.getText()),tag);
                            addQuote.setHint("Quote");
                            addQuote.setText("");
                            addAuthor.setText("");
                            addDialog.dismiss();
                        }

                    }
                });
            }
        });

    }

    private void addItem(String quote, String author, String tag){
        String quoteID = db.push().getKey();
        if(author.isEmpty()) author = "Anonymous";
        if(tag.isEmpty()) tag = "untagged";
        Quote q = new Quote(quote,author,quoteID, tag);
        db.child(q.getQuoteID()).setValue(q);
        quoteList.add(0,q);
        // ovdje mozda uradti nesto drugo jer nez koliko je efikasno ovo
        setAdapter();
      // sliderAdapter.notifyDataSetChanged();
    }

    private void removeItem(int position) {
        db.child(quoteList.get(position).getQuoteID()).removeValue();
        quoteList.remove(position);
        // ovdje mozda uradti nesto drugo jer nez koliko je efikasno ovo
        setAdapter();
     //   sliderAdapter.notifyDataSetChanged();
    }

    private void editItem(int pos, String quote, String author) {
        if(author.isEmpty()) author = "Anonymous";

        quoteList.get(viewPager.getCurrentItem()).setQuote(quote);
        quoteList.get(viewPager.getCurrentItem()).setAuthor(author);

        int idCurr = viewPager.getCurrentItem();

        db.child(quoteList.get(viewPager.getCurrentItem()).getQuoteID()).setValue(quoteList.get(viewPager.getCurrentItem()));
        setAdapter();
        viewPager.setCurrentItem(idCurr);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.addBtn:
                if(chipsInput.getSelectedChipList().size() != 0) {
                    chipsInput.removeChip(chipsInput.getSelectedChipList().get(0));
                }
                addQuote.setHint("Quote");
                chipsInput.setEnabled(true);
                addDialog.show();
                break;
            case R.id.ocr:
                showOCRDialog();
                break;
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

    private void showOCRDialog(){
        storagePerm = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        cameraPerm = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        String[] items = {" Camera ", " Gallery "};
        AlertDialog.Builder dialog = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        dialog.setTitle("Select Image");
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // CAMERA CHOOSED
                if(which == 0) {
                    if(!checkCamPerm()){
                        requestCamPerm();
                    } else {
                        pickCamera();
                    }
                    // GALLERY CHOOSED
                } else if (which == 1){
                    if(!checkStoragePerm()){
                        requestStoragePerm();
                    } else {
                        pickGallery();
                    }
                }
            }
        });
        dialog.create().show();

    }

    // OCR PERM
    private void requestCamPerm(){
        ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()),cameraPerm, CAMERA_REQUEST_CODE);
    }

    private  void requestStoragePerm(){
        ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()),storagePerm, STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePerm() {
        return ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkCamPerm(){
        boolean result = ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean result2 = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return result && result2;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if(grantResults.length>0){
                    boolean camAccepted = (grantResults[0] == PackageManager.PERMISSION_GRANTED);
                    boolean storageAccepted = (grantResults[1] == PackageManager.PERMISSION_GRANTED);
                    if(camAccepted && storageAccepted) {
                        pickCamera();
                    } else {
                        Toast.makeText(getContext(),"Permission denied",Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case STORAGE_REQUEST_CODE:
                if(grantResults.length>0){
                    boolean storageAccepted = (grantResults[0] == PackageManager.PERMISSION_GRANTED);
                    if(storageAccepted) {
                        pickGallery();
                    } else {
                        Toast.makeText(getContext(),"Permission denied",Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    // OCR FUNC
    private void pickCamera(){
        ContentValues vals = new ContentValues();
        vals.put(MediaStore.Images.Media.TITLE, "Quote Picture");
        vals.put(MediaStore.Images.Media.DESCRIPTION, "Image to text");
        image_uri = Objects.requireNonNull(getActivity()).getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, vals);

        Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(camIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickGallery(){
        Intent gallIntent = new Intent(Intent.ACTION_PICK);
        gallIntent.setType("image/*");
        startActivityForResult(gallIntent, IMAGE_PICK_GALLERY_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // CROP IMAGE

        if(resultCode == Activity.RESULT_OK){
            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                assert data != null;
                Intent intent = CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON).getIntent(Objects.requireNonNull(getContext()));
                startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);
            } else if(requestCode == IMAGE_PICK_CAMERA_CODE){
                Intent intent = CropImage.activity(image_uri).setGuidelines(CropImageView.Guidelines.ON).getIntent(Objects.requireNonNull(getContext()));
                startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        }

        // CROPPED IMAGE
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == Activity.RESULT_OK){
                assert result != null;
                Uri resultUri = result.getUri();

                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(Objects.requireNonNull(getActivity()).getContentResolver(), resultUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                TextRecognizer recognizer = new TextRecognizer.Builder(getContext()).build();
                if(!recognizer.isOperational()){
                    Toast.makeText(getContext(),"Error",Toast.LENGTH_SHORT).show();
                } else {
                    assert bitmap != null;
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> items = recognizer.detect(frame);
                    StringBuilder sb = new StringBuilder();
                    for(int i=0;i<items.size();i++){
                        TextBlock myItem = items.valueAt(i);
                        sb.append(myItem.getValue());
                        sb.append("\n");
                    }

                    addQuote.setText(sb.toString().trim().replaceAll("\n", " ").replaceAll(" +", " "));
                    addQuote.requestFocus();
                    addDialog.show();
                }
            } else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                assert result != null;
                Exception error = result.getError();
                Toast.makeText(getContext(),""+error, Toast.LENGTH_SHORT).show();

            }
        }
    }

    @Override
    public void onClick(View v) {

    }
}
