package com.example.user.coolmovs;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private static final String MOVIE_SHARE_HASHTAG = " #CoolMovsApp";
    ArrayAdapter<String> mMovieAdapter;
    private String mIdStr;
    protected String backdoorPath;
    protected ImageView imageView;

    public DetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMovieAdapter = new ArrayAdapter<String>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_movie, // The name of the layout ID.
                        R.id.list_item_movie_textview, // The ID of the textview to populate.
                        new ArrayList<String>());

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.detail_text);
        listView.setAdapter(mMovieAdapter);
        imageView = (ImageView) rootView.findViewById(R.id.detail_poster);

        String url = backdoorPath;
//        imageView.setImageResource(backdoorPath);
//        Picasso.with(this.getContext()).load(url).into(imageView);

        // The detail Activity called via intent.  Inspect the intent for forecast data.
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            mIdStr = intent.getStringExtra(Intent.EXTRA_TEXT);
        }

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        ShareActionProvider mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // Attach an intent to this ShareActionProvider.  You can update this at any time,
        // like when the user selects a new piece of data they might like to share.
/*        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        } else {
            Log.d(LOG_TAG, "Share Action Provider is null?");
        }*/
    }

    private void updateMovie() {
        FetchAMovieTask movieTask = new FetchAMovieTask(imageView);
        movieTask.execute();
    }


    @Override
    public void onStart() {
        super.onStart();
        updateMovie();
    }

/*    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                mMovieAdapter.getItem(0) + MOVIE_SHARE_HASHTAG);
        return shareIntent;
    }*/

    public class FetchAMovieTask extends AsyncTask<Void, Void, ArrayList<String>> {

        private final String LOG_TAG = FetchAMovieTask.class.getSimpleName();
        private ImageView imageView;

        public FetchAMovieTask(ImageView imageView){
            this.imageView = imageView;
        }

        @Override
        protected ArrayList<String> doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String moviesJSONString = null;


            try {
                final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/movie/" + mIdStr + "?";
                final String PARAM_API_KEY = "api_key";
                final String API_KEY = "5a1cc311bb4372b772fafe4ef2eb12b9";

                Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                        .appendQueryParameter(PARAM_API_KEY, API_KEY).build();

                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                moviesJSONString = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMovieDataFromJson(moviesJSONString);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        private ArrayList<String> getMovieDataFromJson(String moviesJSONString) throws JSONException {

            ArrayList<String> details = new ArrayList<>();

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_TITLE = "title";
            final String OWM_BACKDROP_PATH = "backdrop_path";
            final String OWM_SYNOPSIS = "overview";
            final String OWM_USER_RATING = "vote_average";
            final String OWM_RELEASE_DATE = "release_date";

            JSONObject movieJson = new JSONObject(moviesJSONString);

            // description is in a child array called "weather", which is 1 element long.
            details.add("http://image.tmdb.org/t/p/w185" + movieJson.getString(OWM_BACKDROP_PATH));
            details.add(movieJson.getString(OWM_TITLE));
            backdoorPath = "http://image.tmdb.org/t/p/w185" + movieJson.getString(OWM_BACKDROP_PATH);
            details.add(movieJson.getString(OWM_SYNOPSIS));
            details.add(movieJson.getString(OWM_USER_RATING));
            details.add(movieJson.getString(OWM_RELEASE_DATE));

            return details;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            if (result != null) {
                Picasso.with(getContext()).load(result.get(0)).into(imageView);
                mMovieAdapter.clear();
                for (int i = 1; i < result.size(); i++) {
                    mMovieAdapter.add(result.get(i));
                }
                // New data is back from the server.  Hooray!
            }
        }

    }

}
