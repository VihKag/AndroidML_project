package hcmute.vika.appaiandroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;


public class ResultSearchActivity extends AppCompatActivity{
    SearchView title;
    RecyclerView searchRecyclerView;
    private SearchAdapter searchAdapter;
    private List<Item> searchResults = new ArrayList<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_search_layout);
        title=findViewById(R.id.titleSearch);
        searchRecyclerView=findViewById(R.id.recycleView);


//        searchAdapter = new SearchAdapter(searchResults);
        searchRecyclerView.setAdapter(searchAdapter);
        //
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
        searchRecyclerView.addItemDecoration(itemDecoration);
//        //chuyển ảnh từ main sang result
        Intent intent = getIntent();
        //các kkey,api để tra cứu
        String query = intent.getStringExtra("query");

        //Đưa lên thanh tìm kiếm
        title.setQuery(query,false);
        String apiKey = "AIzaSyCuifKj1WJ-PAsidMpEg5WtIiYl3K2RKrk";
        String searchEngineId = "87ef04daad36a4c7e";
        //số kq tra cứu
        int num=10;
        createRecycleView(query,apiKey,searchEngineId,num);
        title.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String apiKey = "AIzaSyCuifKj1WJ-PAsidMpEg5WtIiYl3K2RKrk";
                String searchEngineId = "87ef04daad36a4c7e";
                //số kq tra cứu
                int num=10;
                createRecycleView(query,apiKey,searchEngineId,num);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    private void createRecycleView(String query,String apiKey,String searchEngineId, int numResults){
        // Khởi tạo Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.googleapis.com/")  // Thêm dấu gạch chéo chéo vào cuối URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Tạo instance của Retrofit Service
        GoogleSearchAPI searchAPI = retrofit.create(GoogleSearchAPI.class);
        // Gửi yêu cầu tìm kiếm
        Call<SearchResponse> call = searchAPI.search(apiKey, searchEngineId, query, numResults);
        call.enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                if (response.isSuccessful()) {
                    // Xử lý kết quả tìm kiếm
                    SearchResponse searchResponse = response.body();
                    List<Item> searchResults = searchResponse.getItems();
//                    searchResults.addAll(searchResponse.getItems());
                    for (Item item : searchResults) {
                        String title = item.getTitle();
                        String link = item.getLink();
                        String snippet = item.getSnippet();
                    }
                    // Thiết lập LayoutManager cho RecyclerView
                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ResultSearchActivity.this);
                    searchRecyclerView.setLayoutManager(layoutManager);

//                    // Tạo một SearchAdapter mới và gắn kết dữ liệu
                    searchAdapter = new SearchAdapter(searchResults, new ItemClickListener() {
                        @Override
                        public void onItemClick(Item item) {
                            onClickGoToWeb(item);
                        }
                    });
                    searchRecyclerView.setAdapter(searchAdapter);
                    // Update adapter with search results
                } else {
                    // Xử lý lỗi
                    Log.e("Error", "Search request failed with code: " + response.code() + ", message: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                // Xử lý lỗi
                Log.e("Error", "Search request failed: " + t.getMessage());
            }
        });
    }

    public void onClickGoToWeb(Item item) {
        Intent intent1 = new Intent(ResultSearchActivity.this, WebViewActivity.class);
        intent1.putExtra("url", item.getLink());
        startActivity(intent1);
    }

    public interface GoogleSearchAPI {
        @GET("customsearch/v1")
        Call<SearchResponse> search(@Query("key") String apiKey,
                                    @Query("cx") String searchEngineId,
                                    @Query("q") String query,
                                    @Query("num") int numResults);
    }
}
