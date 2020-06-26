package com.example.flixster;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.RequestParams;
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

    AsyncHttpClient client;

    YouTubePlayerView player;
    String ytKey;

    RatingBar myRating;
    Float currentRating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        client = new AsyncHttpClient();

        // Adds the movie title, overview, and rating to the view
        addBasicInfo();
        // Sets the Youtube video key based on the movie and loads the proper trailer into the view
        setYtKey();
        // Displays movies similar to the current movie
        similarMovies();
        // Gets the user's personal rating, if it exists, to display
        // and submits a new rating if the user changes it
        setRating();
    }

    public void makeRating() {
        myRating = (RatingBar) findViewById(R.id.myRating);

        myRating.setRating(currentRating);

        myRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                float rating = myRating.getRating();
                currentRating = rating;
                sendRating();
            }
        });
    }

    private void setRating() {
        currentRating = (float) 0;
        String getRating = "https://api.themoviedb.org/3/movie/" + movie.getId() + "/account_states?api_key=" + API_KEY + "&session_id=" + SESSION_ID;
        client.get(getRating, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d("Rating", "onSuccess");
                JSONObject jsonObject = json.jsonObject;
                try {
                    JSONObject results = jsonObject.getJSONObject("rated");
                    double rating = results.getDouble("value") / 2;
                    currentRating = (float) rating;
                    makeRating();
                } catch (JSONException e) {
                    // make rating with current rating set to 0, since is no data to get
                    makeRating();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d("Rating", "onFailure");
            }
        });
    }

    private void sendRating() {
        String ratingUrl = "https://api.themoviedb.org/3/movie/" + movie.getId() + "/rating?api_key=" + API_KEY + "&session_id=" + SESSION_ID;

        final RequestParams params = new RequestParams();
        float sendRate = currentRating * 2;
        params.put("value", (int) sendRate);
        client.post(ratingUrl, params, "", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d("Rating", "onSuccess");
                Toast.makeText(getApplicationContext(), "Rating made", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d("Rating", "onFailure");
                Log.d("Rating", statusCode + ": " + response);
            }
        });
    }

    public void similarMovies() {
        String similarUrl = "https://api.themoviedb.org/3/movie/" + movie.getId() + "/similar?api_key=" + API_KEY;

        RecyclerView rvSimilar = findViewById(R.id.rvSimilar);
        final List<Movie>  similar = new ArrayList<>();

        final SimilarAdapter similarAdapter = new SimilarAdapter(this, similar);
        rvSimilar.setAdapter(similarAdapter);
        rvSimilar.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

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
