package com.jurestanic.quotestore;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

public class Quote {

    private String quote;
    private String author;
    private String quoteID;
    private String tag;

    @Exclude
    private boolean isSelected;

    public Quote(){

    }

    public Quote(String quote, String author, String quoteID, String tag) {
        this.quote = quote;
        this.author = author;
        this.quoteID = quoteID;
        this.tag = tag;
    }


    public String getQuoteID() {
        return quoteID;
    }

    public void setQuoteID(String quoteID) {
        this.quoteID = quoteID;
    }

    @Exclude
    public boolean isSelected() {
        return isSelected;
    }

    @Exclude
    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getQuote() {
        return quote;
    }

    public String getAuthor() {
        return author;
    }

    @Exclude
    public boolean getIsSelected() {
        return isSelected;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Exclude
    public void setIsSelected(boolean b) {
        isSelected = b;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
