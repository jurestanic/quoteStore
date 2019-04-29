package com.jurestanic.quotestore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.pchmn.materialchips.ChipView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class QuoteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private static ArrayList<Quote> quoteList;
    private static ArrayList<Quote> quoteListFull;

    private OnItemClickListener mListener;

    private Filter quoteFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Quote> filteredList = new ArrayList<>();
            if( constraint == null || constraint.length() == 0) {
                filteredList.addAll(quoteListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for(Quote item : quoteListFull){
                    if(item.getQuote().toLowerCase().contains(filterPattern)){
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            quoteList.clear();
            quoteList.addAll((ArrayList) results.values);
            notifyDataSetChanged();
        }
    };

    @Override
    public Filter getFilter() {
        return quoteFilter;
    }

    public interface OnItemClickListener {
        void onItemClick(Quote item, int pos, View itemView);
    }

    void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class QuoteViewHolder extends RecyclerView.ViewHolder {

        TextView quote;
        TextView author;
        ChipView chip;

        QuoteViewHolder(@NonNull final View itemView, final OnItemClickListener listener) {
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
                            listener.onItemClick(quoteList.get(pos), pos, itemView);
                        }
                    }
                }
            });

        }
    }

    @Override
    public int getItemViewType(int position) {
        if(quoteList.get(position).getTag().equals("untagged")){
            return 0;
        } else {
            return 1;
        }
    }

    QuoteAdapter(ArrayList<Quote> quoteList) {
        QuoteAdapter.quoteList = quoteList;
        QuoteAdapter.quoteListFull = new ArrayList<>(quoteList);
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.quote, parent, false);
        return new QuoteViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()){
            case 0:
                QuoteViewHolder holder0 = (QuoteViewHolder) holder;
                Quote currentItem1 = quoteList.get(position);
                holder0.quote.setText(currentItem1.getQuote());
                holder0.author.setText(currentItem1.getAuthor());
                holder0.chip.setVisibility(View.GONE);
                break;
            case 1:
                QuoteViewHolder holder1 = (QuoteViewHolder) holder;
                Quote currentItem = quoteList.get(position);
                holder1.quote.setText(currentItem.getQuote());
                holder1.author.setText(currentItem.getAuthor());
                holder1.chip.setLabel(currentItem.getTag());
                break;
        }
    }

    @Override
    public int getItemCount() {
        return quoteList.size();
    }
}
