package com.foxy.photogallery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends Fragment {

    private static final String TAG = "PhotoGalleryFragment";

    private RecyclerView recyclerView;
    private List<GalleryItem> items = new ArrayList<>();

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);              // удержание фрагмента
        new FetchItemsTask(items).execute();  // запуск фонового потока
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        setupAdapter();
        return view;
    }

    private void setupAdapter() {
        // проверка на то, что фрагмент был присоединен к activity и что getActivity() != null
        // необходимо из-за использования AsyncTask
        if (isAdded()) {
            recyclerView.setAdapter(new PhotoAdapter(items));
        }
    }


    private class PhotoHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;

        public PhotoHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView;
        }

        public void bind(GalleryItem item) {
            titleTextView.setText(item.toString());
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        private List<GalleryItem> galleryItems;

        public PhotoAdapter(List<GalleryItem> galleryItems) {
            this.galleryItems = galleryItems;
        }

        @NonNull
        @Override
        public PhotoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView textView = new TextView(getActivity());
            return new PhotoHolder(textView);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoHolder holder, int position) {
            GalleryItem item = galleryItems.get(position);
            holder.bind(item);
        }

        @Override
        public int getItemCount() {
            return galleryItems.size();
        }
    }


    private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>> {
        private List<GalleryItem> items;

        public FetchItemsTask(List<GalleryItem> items) {
            this.items = items;
        }

        @Override
        protected List<GalleryItem> doInBackground(Void... voids) {
            return new FlickrFletchr().fetchItems();
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items) {
            this.items = items;
            setupAdapter();
            Log.i(TAG, "GalleryItems size: " + items.size());
        }
    }
}
