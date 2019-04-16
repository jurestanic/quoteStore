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

import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;


import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.Chip;
import com.pchmn.materialchips.model.ChipInterface;
import com.pchmn.materialchips.views.ChipsInputEditText;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class HomeFragment extends Fragment {

    /*
    * Ovo se poziva svaki put kad kliknem sa nav bara -> znaci trebalo bi podatke i mozda usera stavit samo u main da se sto manje poziva i sto
    * manje podataka poziva svaki put kad se udje sa navbara
    *
    * */

    private RecyclerView mRecyclerView;
    private ExampleAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Quote selectedItem;
    private View selectedView;

    // REMOVE BUTTON
    private MenuItem removeBtn;
    // EDIT BUTTON
    private MenuItem editBtn;

    private int itemPos = -1;

    public static ArrayList<Quote> exampleList;

    private DatabaseReference db;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    //OCR
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1001;

    private String cameraPerm[];
    private String storagePerm[];

    private Uri image_uri;
    private View addView,editView;

    private AlertDialog addDialog,editDialog;
    private EditText addQuote,addAuthor,editQuote, editAuthor;

    private ChipsInput chipsInput, editChips;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        db = database.getReference(user.getUid());

        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                exampleList.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    if(ds.exists())
                        exampleList.add(0,ds.getValue(Quote.class));
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View myView = inflater.inflate(R.layout.home_layout,container,false);
        mRecyclerView = myView.findViewById(R.id.recyclerView);
        addView = inflater.inflate(R.layout.add_quote,container,false);
        editView = inflater.inflate(R.layout.add_quote,container,false);

        chipsInput = addView.findViewById(R.id.chips_input);

        editChips = editView.findViewById(R.id.chips_input);

        createExampleList();
        buildRecycleView();

        createAddDialog();
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
                        editItem(itemPos,String.valueOf(editQuote.getText()),String.valueOf(editAuthor.getText()), selectedTags.get(0).getLabel());
                    }
                })
                .setNegativeButton("cancel", null)
                .create();
    }

    private List<Chip> createTags(final ChipsInput chips){

        com.pchmn.materialchips.model.Chip c = new com.pchmn.materialchips.model.Chip("motivation",null);
        com.pchmn.materialchips.model.Chip c2 = new com.pchmn.materialchips.model.Chip("health",null);
        com.pchmn.materialchips.model.Chip c3 = new com.pchmn.materialchips.model.Chip("money",null);
        com.pchmn.materialchips.model.Chip c4 = new com.pchmn.materialchips.model.Chip("success",null);
        com.pchmn.materialchips.model.Chip c6 = new com.pchmn.materialchips.model.Chip("life",null);
        com.pchmn.materialchips.model.Chip c7 = new com.pchmn.materialchips.model.Chip("philosophy",null);

        ArrayList<Chip> chipList = new ArrayList<>();
        chipList.add(c);
        chipList.add(c2);
        chipList.add(c3);
        chipList.add(c4);
        chipList.add(c6);
        chipList.add(c7);

        chips.addChipsListener(new ChipsInput.ChipsListener() {
            @Override
            public void onChipAdded(final ChipInterface chip, int newSize) {

            }

            @Override
            public void onChipRemoved(ChipInterface chip, int newSize) {

            }

            @Override
            public void onTextChanged(CharSequence text) {
                if(chips.getSelectedChipList().size() >= 1) {

                } else if(text.toString().contains(" ") || text.toString().contains(",")) {
                    chips.addChip(text.toString().substring(0,text.length()-1),null);
                    chips.getChipView().setEnabled(false);
                }
            }
        });

        final List<Chip> selectedTags = (List<Chip>) chips.getSelectedChipList();
      //  chips.setFilterableList(chipList);

        return selectedTags;
    }

    private void createAddDialog() {
        addQuote = addView.findViewById(R.id.quoteTxt);
        addQuote.setSingleLine(false);
        addAuthor  = addView.findViewById(R.id.authorTxt);

        final List<Chip> selectedTags = createTags(chipsInput);

        // ADD DIALOG
        addDialog = new AlertDialog.Builder(getContext())
                .setView(addView)
                .setPositiveButton("add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addItem(String.valueOf(addQuote.getText()), String.valueOf(addAuthor.getText()),selectedTags.get(0).getLabel());
                        addQuote.setText("");
                        addAuthor.setText("");
                    }
                })
                .setNegativeButton("cancel", null)
                .create();

    }


    private void addItem(String quote, String author, String tag){
        String quoteID = db.push().getKey();
        Quote q = new Quote(quote,author,quoteID, tag);

        db.child(q.getQuoteID()).setValue(q);
        exampleList.add(0,q);
        mAdapter.notifyItemInserted(0);
        // mRecyclerView.smoothScrollToPosition(0);
        mRecyclerView.scrollToPosition(0);


    }

    private void removeItem(int position) {
        db.child(exampleList.get(position).getQuoteID()).removeValue();
        exampleList.remove(position);
        mAdapter.notifyItemRemoved(position);
        setSelected();
    }

    private void editItem(int position, String quote, String author, String tag) {
        exampleList.get(position).setQuote(quote);
        exampleList.get(position).setAuthor(author);
        exampleList.get(position).setTag(tag);

        db.child(exampleList.get(position).getQuoteID()).setValue(exampleList.get(position));

        mAdapter.notifyItemChanged(position);
        setSelected();
    }

    public void createExampleList(){
        exampleList = new ArrayList<>();

    }

    public void setSelected() {

        if(selectedItem.getIsSelected()) {
            removeBtn.setVisible(false);
            editBtn.setVisible(false);
            ((CardView) selectedView).setCardBackgroundColor(Color.parseColor("#FFFFFF"));
            selectedItem.setIsSelected(false);
        } else {
            for(int i=0;i<exampleList.size();i++){
                if(exampleList.get(i).getIsSelected()) {
                    removeBtn.setVisible(false);
                    editBtn.setVisible(false);
                    View view = mLayoutManager.findViewByPosition(i);
                    ((CardView) view).setCardBackgroundColor(Color.parseColor("#FFFFFF"));
                    exampleList.get(i).setIsSelected(false);
                }
            }

            removeBtn.setVisible(true);
            editBtn.setVisible(true);
            ((CardView) selectedView).setCardBackgroundColor(Color.parseColor("#b7b7b7"));
            selectedItem.setIsSelected(true);
        }
    }

    public void buildRecycleView() {
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mAdapter = new ExampleAdapter(exampleList);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        // OVDJE MOGU DODAT PREDEFINIRANE CITATE

        mAdapter.setOnItemClickListener(new ExampleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Quote item, int pos, View itemView) {
                selectedView = itemView;
                selectedItem = item;
                itemPos = pos;
                setSelected();
            }
        });
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.addbutton, menu);
        removeBtn = menu.findItem(R.id.rmvBtn);
        editBtn = menu.findItem(R.id.editBtn);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.addBtn:
                addQuote.requestFocus();
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
                System.out.println(selectedItem.getTag());
                editChips.addChip(selectedItem.getTag(),null);
                editDialog.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showOCRDialog(){

        storagePerm = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        cameraPerm = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};


        String[] items = {" Camera ", " Gallery "};
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
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
        ActivityCompat.requestPermissions(getActivity(),cameraPerm, CAMERA_REQUEST_CODE);
    }

    private  void requestStoragePerm(){
        ActivityCompat.requestPermissions(getActivity(),storagePerm, STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePerm() {
        return ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkCamPerm(){
        boolean result = ContextCompat.checkSelfPermission(getContext(),
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
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, vals);

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
                Intent intent = CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON).getIntent(getActivity());
                startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);
            } else if(requestCode == IMAGE_PICK_CAMERA_CODE){
                Intent intent = CropImage.activity(image_uri).setGuidelines(CropImageView.Guidelines.ON).getIntent(getActivity());
                startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        }




        // CROPPED IMAGE
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == Activity.RESULT_OK){
                System.out.println("GOTHERE");
                Uri resultUri = result.getUri();

                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), resultUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                TextRecognizer recognizer = new TextRecognizer.Builder(getContext()).build();
                if(!recognizer.isOperational()){
                    Toast.makeText(getContext(),"Error",Toast.LENGTH_SHORT).show();
                } else {
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
                System.out.println("OHNO");
                Exception error = result.getError();
                Toast.makeText(getContext(),""+error, Toast.LENGTH_SHORT).show();

            }
        }
    }

}
