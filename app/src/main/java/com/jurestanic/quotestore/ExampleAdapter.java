package com.jurestanic.quotestore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pchmn.materialchips.ChipView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class ExampleAdapter extends RecyclerView.Adapter<ExampleAdapter.ExampleViewHolder> {

    private static ArrayList<Quote> exampleList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(Quote item, int pos, View itemView);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class ExampleViewHolder extends RecyclerView.ViewHolder {

        public TextView quote;
        public TextView author;
        public ChipView chip;

        public ExampleViewHolder(@NonNull final View itemView, final OnItemClickListener listener) {
            super(itemView);
            quote = itemView.findViewById(R.id.quote);
            author = itemView.findViewById(R.id.author);
            chip = itemView.findViewById(R.id.tag);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            listener.onItemClick(exampleList.get(pos), pos, itemView);
                        }
                    }
                }
            });

        }
    }

    public ExampleAdapter(ArrayList<Quote> exampleList) {
        this.exampleList = exampleList;
    }


    @NonNull
    @Override
    public ExampleAdapter.ExampleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.example_item, parent, false);
        return new ExampleViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ExampleViewHolder holder, int position) {
        Quote currentItem = exampleList.get(position);
        holder.quote.setText(currentItem.getQuote());
        holder.author.setText(currentItem.getAuthor());
        holder.chip.setLabel(currentItem.getTag());
    }

    @Override
    public int getItemCount() {
        return exampleList.size();
    }
}
