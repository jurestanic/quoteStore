package com.jurestanic.quotestore;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface QuotesApi {

    @GET("qod.json")
    Call<ApiQuote> getApiQuotes();

}
