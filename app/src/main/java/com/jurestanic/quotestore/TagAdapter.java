package com.jurestanic.quotestore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pchmn.materialchips.ChipView;
import com.pchmn.materialchips.model.Chip;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> {

    private static ArrayList<Chip> tagList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(Chip item, int pos, View itemView);
    }

    void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    static class TagViewHolder extends RecyclerView.ViewHolder {

        ChipView chip;

        TagViewHolder(@NonNull final View itemView, final OnItemClickListener listener) {
            super(itemView);
            chip = itemView.findViewById(R.id.chipView);

            chip.setOnChipClicked(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            listener.onItemClick(tagList.get(pos), pos, itemView);
                        }
                    }
                }
            });
        }
    }

    TagAdapter(ArrayList<Chip> tagList) {
        TagAdapter.tagList = tagList;
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chip, parent, false);
        return new TagAdapter.TagViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        Chip currentTag = tagList.get(position);
        holder.chip.setLabel(currentTag.getLabel());

        if(position == 0) mListener.onItemClick( tagList.get(position) ,0,holder.itemView);
    }

    @Override
    public int getItemCount() {
        return tagList.size();
    }
}
