package hcmute.vika.appaiandroid;

import android.app.appsearch.SearchResult;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class SearchResponse {
    // Define the properties that match the response structure
    private List<Item> items;

    // Create getters and setters for the properties

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
    public void onResponse(Call<SearchResult> call, Response<SearchResult> response) {
        if (response.isSuccessful()) {
            // Declare and initialize the searchResult variable
            SearchResult searchResult = response.body();

            // Rest of your code...
        }
        // Rest of your code...
    }
}


