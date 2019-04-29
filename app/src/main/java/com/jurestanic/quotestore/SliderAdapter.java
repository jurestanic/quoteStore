package com.jurestanic.quotestore;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class SliderAdapter extends PagerAdapter {

    Context context;
    LayoutInflater layoutInflater;

    private ArrayList<String> quotes = new ArrayList<>();
    private ArrayList<String> authors = new ArrayList<>();

    public FloatingActionButton editFab;
    public FloatingActionButton deleteFab;
    public FloatingActionButton shareFab;


    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int id, View view);
    }

    public SliderAdapter(Context context, ArrayList<Quote> quoteList){
        this.context = context;
        for(Quote q : quoteList){
            quotes.add(q.getQuote());
            authors.add(q.getAuthor());
        }
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return quotes.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }


    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        final View view = layoutInflater.inflate(R.layout.slide_quote_item,container,false);
        view.findViewById(R.id.slideQuoteTxt).setFocusableInTouchMode(false);
        view.findViewById(R.id.slideAuthorTxt).setFocusableInTouchMode(false);

        editFab = view.findViewById(R.id.editFab);
        deleteFab = view.findViewById(R.id.deleteFab);
        shareFab = view.findViewById(R.id.shareFab);

        editFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(R.id.editFab,view);
            }
        });

        shareFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(R.id.shareFab,view);
            }
        });

        deleteFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(R.id.deleteFab,view);
            }
        });

        EditText slideQuoteTxt = view.findViewById(R.id.slideQuoteTxt);
//        slideQuoteTxt.setVerticalScrollBarEnabled(true);
//        slideQuoteTxt.setMovementMethod(new ScrollingMovementMethod());

        EditText slideAuthorTxt = view.findViewById(R.id.slideAuthorTxt);
//        slideAuthorTxt.setVerticalScrollBarEnabled(true);
//        slideAuthorTxt.setMovementMethod(new ScrollingMovementMethod());

        slideQuoteTxt.setText(quotes.get(position));
        slideAuthorTxt.setText(authors.get(position));

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((RelativeLayout) object);
    }
}
