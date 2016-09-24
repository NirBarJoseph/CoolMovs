package com.example.user.coolmovs;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MoviesFragment extends Fragment {

    ImageAdapter mMoviesAdapter;

    public MoviesFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
       inflater.inflate(R.menu.moviesfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id= item.getItemId();
        if (id == R.id.action_refresh) {
            FetchMoviesTask movieTask = new FetchMoviesTask();
            movieTask.execute();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMoviesAdapter = new ImageAdapter(getActivity());

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_movies);
        gridView.setAdapter(mMoviesAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String movie = mMoviesAdapter.getTiteList().get(position);
                Toast.makeText(getActivity(), movie, Toast.LENGTH_SHORT).show();

                String id = mMoviesAdapter.getidlist().get(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, id);
                startActivity(intent);
            }
        });

        return rootView;
    }

    private void updateMovies() {
        FetchMoviesTask movieTask = new FetchMoviesTask();
        movieTask.execute();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    public class FetchMoviesTask extends AsyncTask<Void, Void, ArrayList<ArrayList<String>>> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        @Override
        protected ArrayList<ArrayList<String>> doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String moviesJSONString = null;


            try {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String sort = prefs.getString("sort_by", "popularity.desc");

                final String MOVIES_BASE_URL ="http://api.themoviedb.org/3/discover/movie?";
                final String SORT_BY = "sort_by";
                final String PARAM_API_KEY = "api_key";
                final String API_KEY = "5a1cc311bb4372b772fafe4ef2eb12b9";

                Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_BY, sort)
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
                return getWeatherDataFromJson(moviesJSONString);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }



            return null;
        }

        private ArrayList<ArrayList<String>> getWeatherDataFromJson(String moviesJSONString)throws JSONException {

            ArrayList<ArrayList<String>> result_lists = new ArrayList<>();
            ArrayList<String> id_list = new ArrayList<>();
            ArrayList<String> url_list = new ArrayList<>();
            ArrayList<String> title_list = new ArrayList<>();

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "results";
            final String OWM_ID = "id";
            final String OWM_POSTER_PATH = "poster_path";
            final String OWM_TITLE = "title";

            JSONObject moviesJson = new JSONObject(moviesJSONString);
            JSONArray moviesArray = moviesJson.getJSONArray(OWM_LIST);

            for(int i = 0; i < moviesArray.length(); i++) {

                // Get the JSON object representing the movie
                JSONObject aMovie = moviesArray.getJSONObject(i);

                // description is in a child array called "weather", which is 1 element long.
                id_list.add(aMovie.getString(OWM_ID));
                url_list.add("http://image.tmdb.org/t/p/w185//" + aMovie.getString(OWM_POSTER_PATH));
                title_list.add(aMovie.getString(OWM_TITLE));
            }
            result_lists.add(url_list);
            result_lists.add(id_list);
            result_lists.add(title_list);

            return result_lists;
        }


        @Override
        protected void onPostExecute(ArrayList<ArrayList<String>> result) {
            if (result != null) {
                mMoviesAdapter.clear();
                for(int i = 0; i < result.get(0).size(); i++) {
                   mMoviesAdapter.refresh(result);
                }
                // New data is back from the server.  Hooray!
            }
        }


    }

}
