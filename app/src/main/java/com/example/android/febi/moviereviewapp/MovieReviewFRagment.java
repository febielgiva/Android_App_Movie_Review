package com.example.android.febi.moviereviewapp;

/**
 * Created by FEBIELGIVA on 4/28/2016.
 */

import android.content.AsyncTaskLoader;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieReviewFRagment extends Fragment {

    private final String LOG_TAG = MovieReviewFRagment .class.getSimpleName();
    public ArrayAdapter movieReviewAdapter;
    public MovieReviewFRagment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_fragment,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int idOfItemClicked = item.getItemId();
        switch(idOfItemClicked){
            case R.id.sction_refresh :
            {
                FetchMovieReview fetchMoviewReviewObj = new FetchMovieReview();
                fetchMoviewReviewObj.execute();

            }
            case R.id.action_help:
                return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.list_view_review);
        //creating a string array
        String[] movieReviewStringArray = {
               "Movie Name 1",
                "Movie Name 2"
        };
        ArrayList<String> movieReviewStringList = new ArrayList<>(Arrays.asList(movieReviewStringArray));

       // List<String> movieReviewStringList = new ArrayList<String>();
//        FetchMovieReview fetchMoviewReviewObj = new FetchMovieReview();
//        fetchMoviewReviewObj.execute();


        movieReviewAdapter = new ArrayAdapter<String>(getActivity(),
                                                    R.layout.list_view_review,
                                                    R.id.text_in_listViewXML,
                movieReviewStringList);
        listView.setAdapter(movieReviewAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String movieReview = movieReviewAdapter.getItem(position).toString();
                Toast toast= Toast.makeText(getActivity(),movieReview,Toast.LENGTH_LONG);
                toast.show();
            }
        });

        Log.e(LOG_TAG,"error ");
        return rootView;

    }


    public class FetchMovieReview extends AsyncTask<Void,Void,List<String>>{

        private final String LOG_TAG = FetchMovieReview.class.getSimpleName();


        @Override
        protected List<String> doInBackground(Void... params) {

            HttpURLConnection httpConnection = null;
            BufferedReader reader = null;
            String jsonMoviewReviewString = null;


            //declaration for URI builder
            final String MOVIE_REVIEW_BASE_URL =
                    "http://api.nytimes.com/svc/movies/v2/reviews/search.json?";
            final String THOUSAND_BEST_PARAM = "thousand-best";
            final String APPID_PARAM="api-key";

            final String thousandBestValueIndicator = "Y";
            final String appKey = BuildConfig.MOVIE_REVIEW_API_KEY;

            try {
                //building a URL
                    Uri builtUri = Uri.parse(MOVIE_REVIEW_BASE_URL)
                        .buildUpon()
                        .appendQueryParameter(THOUSAND_BEST_PARAM, thousandBestValueIndicator)
                        .appendQueryParameter(APPID_PARAM, appKey)
                        .build();


                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG,"URL Build" + builtUri.toString());



                httpConnection = (HttpURLConnection) url.openConnection();
                httpConnection.setRequestMethod("GET");
                httpConnection.connect();

                //connected to url now response will be a inputStream so storing it to inputstream object
                InputStream inputStream = httpConnection.getInputStream();
                //BufferReader help to read the string
                reader = new BufferedReader(new InputStreamReader(inputStream));
                //now we can read line by line

                StringBuffer buffer = new StringBuffer();
                String line ="";
                while((line = reader.readLine())!= null){
                    buffer.append(line);
                }

                if(buffer.length() == 0){
                    return null;
                }
                else
                {
                    jsonMoviewReviewString = buffer.toString();
                }


                Log.v(LOG_TAG,"Moview Review Json String" + jsonMoviewReviewString);

            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, "Error MalformedURLException", e);
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error URIConnection IOException", e);
                e.printStackTrace();
            }finally {
                if(httpConnection != null) {
                    httpConnection.disconnect();
                }
                if(reader != null){
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Error Reader IOException", e);
                        e.printStackTrace();
                    }
                }
            }


            List<String> movieTitle = new ArrayList<String>();
            movieTitle = getStringAfterJSONParsing(jsonMoviewReviewString);
            if(movieTitle != null)
                return movieTitle;

            else
                return null;


        }

        public List<String> getStringAfterJSONParsing(String jsonMovieReview){
            List<String> movieTitleArray = new ArrayList<String>();

            JSONObject jsonObject = null,eachResultJsonObject = null;
            JSONArray resultArray = null;

            String eachmovieTitle = null;

            try {
                jsonObject = new JSONObject(jsonMovieReview);
                resultArray = jsonObject.getJSONArray("results");


            for(int i = 0; i < resultArray.length(); i++) {
                eachResultJsonObject = resultArray.getJSONObject(i);
                eachmovieTitle = eachResultJsonObject.getString("display_title");
                movieTitleArray.add(eachmovieTitle);
            }


            } catch (JSONException e) {
                e.printStackTrace();
            }
            return movieTitleArray;
        }


        @Override
        protected void onPostExecute(List<String> result) {
            if(result != null){
                movieReviewAdapter.clear();
                for (String eachResult : result) {
                    movieReviewAdapter.add(eachResult);
                }
            }
        }
    }
}

