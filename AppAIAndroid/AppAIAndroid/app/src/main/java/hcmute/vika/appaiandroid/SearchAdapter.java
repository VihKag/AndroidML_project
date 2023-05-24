package hcmute.vika.appaiandroid;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {
    private List<Item> searchResults;
    private ItemClickListener mListener ;


    public SearchAdapter(List<Item> searchResults,ItemClickListener listener) {
        this.searchResults = searchResults;
        this.mListener=listener;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder,int position) {
        final Item item = searchResults.get(position);
        if(item==null){
            return;
        }
        holder.titleTextView.setText(item.getTitle());
        holder.linkTextView.setText(item.getLink());
        holder.snippetTextView.setText(item.getSnippet());

        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onItemClick(item);
            }
        });

    }

    @Override
    public int getItemCount() {
        if(searchResults!=null){
            return searchResults.size();
        }
        return 0;
    }

    public Item getItem(int position) {
        return searchResults.get(position);
    }

    public static class SearchViewHolder extends RecyclerView.ViewHolder {
        LinearLayout item;
        TextView titleTextView;
        TextView linkTextView;
        TextView snippetTextView;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            item=itemView.findViewById(R.id.item);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            linkTextView = itemView.findViewById(R.id.linkTextView);
            snippetTextView = itemView.findViewById(R.id.snippetTextView);
        }
    }
}
