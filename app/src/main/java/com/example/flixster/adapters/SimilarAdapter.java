package com.example.flixster.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.flixster.MovieDetailsActivity;
import com.example.flixster.R;
import com.example.flixster.models.Movie;

import org.parceler.Parcels;

import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class SimilarAdapter extends RecyclerView.Adapter<SimilarAdapter.ViewHolder> {

    Context context;
    List<Movie> similar;

    public SimilarAdapter(Context context, List<Movie> movies) {
        this.context = context;
        this.similar = movies;
    }

    // Inflates a layout from XML and returns it to the holder
    @NonNull
    @Override
    public SimilarAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("SimilarAdapter", "onCreateViewHolder");
        View similarView = LayoutInflater.from(context).inflate(R.layout.similar_movie, parent, false);
        return new SimilarAdapter.ViewHolder(similarView);
    }

    // Populates data into the item through holder
    @Override
    public void onBindViewHolder(@NonNull SimilarAdapter.ViewHolder holder, int position) {
        Log.d("SimilarAdapter", "onBindViewHolder " + position);
        // Get the movie at thr passed in position
        Movie movie = similar.get(position);
        // Bind the movie data into the VH
        holder.bind(movie);
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return similar.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView ivPoster;

        // when the user clicks on a row, show MovieDetailsActivity for the selected movie
        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Movie movie = similar.get(position);
                Intent intent = new Intent(context, MovieDetailsActivity.class);
                intent.putExtra(Movie.class.getSimpleName(), Parcels.wrap(movie));
                Log.d("intent", intent.toString());
                context.startActivity(intent);
            }
        }

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivPoster);
            itemView.setOnClickListener(this);
        }

        public void bind(Movie movie) {
            String imageUrl = movie.getPosterPath();
            int placeholder = R.drawable.flicks_backdrop_placeholder;
            int radius = 30;
            int margin = 10;
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(placeholder)
                    .transform(new RoundedCornersTransformation(radius, margin))
                    .into(ivPoster);
        }
    }
}
