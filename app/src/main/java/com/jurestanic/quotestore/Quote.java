package com.jurestanic.quotestore;

import com.google.firebase.database.Exclude;
import com.google.gson.annotations.SerializedName;

public class Quote {

    private String Quote;
    private String Author;
    private String quoteID;
    @SerializedName("Tags")
    private String tag;

    @Exclude
    private boolean isSelected;

    public Quote(){

    }

    Quote(String quote, String author, String quoteID, String tag) {
        this.Quote = quote;
        this.Author = author;
        this.quoteID = quoteID;
        this.tag = tag;
    }


    public String getQuoteID() {
        return quoteID;
    }

    public void setQuoteID(String quoteID) {
        this.quoteID = quoteID;
    }

    public String getQuote() {
        return Quote;
    }

    public String getAuthor() {
        return Author;
    }

    @Exclude
    boolean getIsSelected() {
        return isSelected;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Exclude
    void setIsSelected(boolean b) {
        isSelected = b;
    }

    public void setQuote(String quote) {
        this.Quote = quote;
    }

    public  void setAuthor(String author) {
        this.Author = author;
    }
}
