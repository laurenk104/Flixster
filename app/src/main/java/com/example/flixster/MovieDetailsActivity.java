package com.example.flixster;

import android.os.Bundle;
import android.util.Log;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.flixster.adapters.MovieAdapter;
import com.example.flixster.adapters.SimilarAdapter;
import com.example.flixster.models.Movie;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class MovieDetailsActivity extends YouTubeBaseActivity {

    public static final String API_KEY = "62619cea068f3b73089089ab88d78890";
    public static final String SESSION_ID = "b39aeb072fcc4e4aa1493f921014270fe1cdac21";

    Movie movie;

    TextView tvTitle;
    TextView tvOverview;
    RatingBar rbVoteAverage;

    YouTubePlayerView player;
    String ytKey;

    RatingBar myRating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        // Adds the movie title, overview, and rating to the view
        addBasicInfo();
        // Sets the Youtube video key based on the movie and loads the proper trailer into the view
        setYtKey();
        // Displays movies similar to the current movie
        similarMovies();
        //
        makeRating();
    }

    public void makeRating() {
        String ratingUrl = "https://api.themoviedb.org/3/movie/" + movie.getId() + "/rating?api_key=" + API_KEY + "&session_id=" + SESSION_ID;

        myRating = (RatingBar) findViewById(R.id.myRating);

        float rating = myRating.getRating();
        Log.d("rating", "Rating is: "+rating);

        float voteAverage = movie.getVoteAverage().floatValue();
        myRating.setRating(voteAverage = voteAverage > 0 ? voteAverage / 2.0f : voteAverage);
    }

    public void similarMovies() {
        String similarUrl = "https://api.themoviedb.org/3/movie/" + movie.getId() + "/similar?api_key=" + API_KEY;

        RecyclerView rvSimilar = findViewById(R.id.rvSimilar);
        final List<Movie>  similar = new ArrayList<>();

        final SimilarAdapter similarAdapter = new SimilarAdapter(this, similar);
        rvSimilar.setAdapter(similarAdapter);
        rvSimilar.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(similarUrl, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d("Similar", "onSuccess");
                JSONObject jsonObject = json.jsonObject;
                try {
                    JSONArray results = jsonObject.getJSONArray("results");
                    Log.i("Similar", "Results: " + results.toString());
                    similar.addAll(Movie.fromJsonArray(results));
                    similarAdapter.notifyDataSetChanged();
                    Log.i("Similar", "Similar movies: " + similar.size());
                } catch (JSONException e) {
                    Log.e("Similar", "Hit json exception", e);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d("Similar", "onFailure");
            }
        });
    }

    public void addBasicInfo() {
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvOverview = (TextView) findViewById(R.id.tvOverview);
        rbVoteAverage = (RatingBar) findViewById(R.id.rbVoteAverage);

        // unwrap the movie passed in via intent, using its simple name as a key
        movie = (Movie) Parcels.unwrap(getIntent().getParcelableExtra(Movie.class.getSimpleName()));
        Log.d("MovieDetailsActivity", String.format("Showing details for '%s'", movie.getTitle()));

        // set the title and overview
        tvTitle.setText(movie.getTitle());
        tvOverview.setText(movie.getOverview());

        // converts the vote average from a base-10 to base-5 rating system
        float voteAverage = movie.getVoteAverage().floatValue();
        rbVoteAverage.setRating(voteAverage = voteAverage > 0 ? voteAverage / 2.0f : voteAverage);
    }

    private void setYtKey() {
        // sets an initial default video of a windows error screen remix, indicating that something wrong
        // happened if this is the video that plays
        ytKey = "5BZLz21ZS_Y";
        String trailersUrl = "https://api.themoviedb.org/3/movie/" + movie.getId() + "/trailers?api_key=" + API_KEY;

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(trailersUrl, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d("Videos", "onSuccess");
                JSONObject jsonObject = json.jsonObject;
                try {
                    JSONArray videos = jsonObject.getJSONArray("youtube");
                    JSONObject video = videos.getJSONObject(0);
                    ytKey = video.getString("source");
                    addVideo();
                } catch (JSONException e) {
                    Log.e("Videos", "Hit json exception", e);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d("Videos", "onFailure");
            }
        });
    }

    private void addVideo() {
        player = (YouTubePlayerView) findViewById(R.id.player);

        player.initialize(API_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                youTubePlayer.cueVideo(ytKey);
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Log.e("MovieTrailerActivity", "Error initializing YouTube player");
            }
        });
    }
}
