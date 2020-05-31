package com.loyid.weatherforecast;


// [Assignment_1+2] android.content.Intent
import android.content.Intent;

// [Assignment_5] Fetching data Using SharedPreference which is set above.
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

// [Assignment_6]
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

// [Assignment_7+8]
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

// [Assignment_9+10]
import android.database.Cursor;
import android.os.SystemClock;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.widget.TextView;
import com.loyid.weatherforecast.provider.WeatherContract;


// [Assignment_3] add android.util.log
import android.util.Log;

// [Assignment_4] add android.os.AsyncTask;
import android.os.AsyncTask;

// [Assignment_5] add multiple packages...
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

// [Assignment_5]
import java.text.SimpleDateFormat;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
// [Assignment_9+10]
public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int FORECAST_LOADER = 0;

    //private ArrayAdapter<String> mForecastAdapter;
    private WeatherForecastAdapter mForecastAdapter;
    private IFetchWeatherService mService;

    // [Assignment_9+10]
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherColumns._ID,
            WeatherContract.WeatherColumns.COLUMN_DATE,
            WeatherContract.WeatherColumns.COLUMN_SHORT_DESC,
            WeatherContract.WeatherColumns.COLUMN_MAX_TEMP,
            WeatherContract.WeatherColumns.COLUMN_MIN_TEMP,
    };


    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;

    // [Assignment_6] BroadcastReceiver
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(FetchWeatherService.ACTION_RETRIEVE_WEATHER_DATA)) {
               // String[] data = intent.getStringArrayExtra(FetchWeatherService.EXTRA_WEATHER_DATA);
                //mForecastAdapter.clear();
                //for(String dayForecastStr : data) {
                  //  mForecastAdapter.add(dayForecastStr);
                //}
            }
        }
    };
    // ~[Assignment_6]

    // [Assignment_7+8]
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IFetchWeatherService.Stub.asInterface(service);

            try {
                mService.registerFetchDataListener(mFetchDataListener);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    private IFetchDataListener.Stub mFetchDataListener = new IFetchDataListener.Stub() {
        @Override
        public void onWeatherDataRetrieved(String[] data) throws RemoteException {
           // mForecastAdapter.clear();
           // for(String dayForecastStr : data) {
            //    mForecastAdapter.add(dayForecastStr);
            //}
        }
    } ;

    // ~[Assignment_7+8]

    public MainFragment() {
        // Required empty public constructor
    }

    // [Assignment_9+10]
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);

    }


    //[Assignmet_5] add json methods...
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().bindService(new Intent(getActivity(), FetchWeatherService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        if (mService != null) {
            try {
                mService.unregisterFetchDataListener(mFetchDataListener);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
        getActivity().unbindService(mServiceConnection);
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            //FetchWeatherTask weatherTask = new FetchWeatherTask();
            //weatherTask.execute("1835847");
            //[Assignment_5]add method()
            refreshWeatherData();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void refreshWeatherData() {
        //FetchWeatherTask weatherTask = new FetchWeatherTask();
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //String cityId = prefs.getString("city", "1835847");
        //weatherTask.execute(cityId);
        // [Assignment_6]
/*        Intent intent = new Intent(getActivity(), FetchWeatherService.class);
        intent.setAction(FetchWeatherService.ACTION_RETRIEVE_WEATHER_DATA);
        getActivity().startService(intent);*/
        //~[Assignment_6]

        if (mService != null) {
            try {
                mService.retrieveWeatherData();
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }
    //~[Assignment_5]


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Create some dummy data for the ListView.  Here's a sample weekly forecast
        String[] data = {
                "Mon 6/23 - Sunny - 31/17",
                "Tue 6/24 - Foggy - 21/8",
                "Wed 6/25 - Cloudy - 22/17",
                "Thurs 6/26 - Rainy - 18/11",
                "Fri 6/27 - Foggy - 21/10",
                "Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18",
                "Sun 6/29 - Sunny - 20/7"
        };

        List<String> weekForecast = new ArrayList<String>(Arrays.asList(data));

        // Now that we have some dummy forecast data, create an ArrayAdapter.
        // The ArrayAdapter will take data from a source (like our dummy forecast) and
        // use it to populate the ListView it's attached to.
/*        mForecastAdapter =
                new ArrayAdapter<String>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_forecast, // The name of the layout ID.
                        R.id.list_item_forecast_textview, // The ID of the textview to populate.
                        weekForecast);*/

        mForecastAdapter = new WeatherForecastAdapter(getActivity(), null, 0);

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
/*
                String forecast = mForecastAdapter.getItem(position);
                Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show();


                //[Assignment_1+2] 추가하는 부분.
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("data", forecast);
                //startActivity(intent);
*/
            }
        });

        //[Assignment_5] 아래 Assignment_4 주석처리

        /*
        //[Assignment_4] 추가!
        FetchWeatherTask weatherTask = new FetchWeatherTask();
        weatherTask.execute();*/

        // [Assignment_6] Broadcast action
        // IntentFilter intentFilter = new IntentFilter(FetchWeatherService.ACTION_RETRIEVE_WEATHER_DATA);
        //getActivity().registerReceiver(mBroadcastReceiver, intentFilter);

        return rootView;
    }


/*  //[Assignment_6]
    //[Assignment_5] Changing AsyncTask's parmeter (void->string)
    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
        *//*
        //[Assignment_4] Add AsyncTask to resolve NetworkOnMainException.
    public class FetchWeatherTask extends AsyncTask<Void, Void, Void> {*//*

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();


        //[Assignment_5] adding!
        *//* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         *//*
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        *//**
         * Prepare the weather high/lows for presentation.
         *//*
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        *//**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         *//*
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            for (String s : resultStrs) {
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }
            return resultStrs;

        }

        //~[Assignment_5]




        //protected Void doInBackground(Void... params) {
        //[Assignment_5]~
        @Override
        protected String[] doInBackground(String... params) {

            // If there's no zip code, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }
            //~[Assignment_5]


            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            //[Assignment_5]~
            String format = "json";
            String units = "metric";
            int numDays = 7;
            //~[Assignment_5]


            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast

                //String baseUrl = "http://api.openweathermap.org/data/2.5/forecast/daily?id=1835847&mode=json&units=metric&cnt=7";
                //String apiKey = "a12c25f594effdcee803248d900b3da3";
                //URL url = new URL(baseUrl.concat(apiKey));

                //[Assignment_5]~
                final String FORECAST_BASE_URL =
                        "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "id";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAM = "APPID";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .appendQueryParameter(APPID_PARAM, "5fd2f2cde90c1533efb95b19c048a528")
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());
                //~[Assignment_5]


                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
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
                forecastJsonStr = buffer.toString();

                //[Assignment_5]
                Log.v(LOG_TAG, "Forecast string: " + forecastJsonStr);
                //~[Assignment_5]
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

            //[Assignment_5]
            try {
                return getWeatherDataFromJson(forecastJsonStr, numDays);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            //~[Assignment_5]
            return null;
        }
        //[Assignment_5]
        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                mForecastAdapter.clear();
                for(String dayForecastStr : result) {
                    mForecastAdapter.add(dayForecastStr);
                }
                // New data is back from the server.  Hooray!
            }
        }
        //~[Assignment_5]*/

    //[Assignment_6]
    @Override
    public void onDestroyView() {
        //getActivity().unregisterReceiver(mBroadcastReceiver);
        super.onDestroyView();
    }
    //~[Assignment_6]

    // [Assignment_9+10]
    private class WeatherForecastAdapter extends CursorAdapter {
        public class ViewHolder {
            public final TextView mTextView;

            public ViewHolder(View view) {
                mTextView = (TextView)view.findViewById(R.id.list_item_forecast_textview);
            }
        }

        public WeatherForecastAdapter(Context context, Cursor cursor, int flags) {
            super(context, cursor, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);

            ViewHolder viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);

            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder)view.getTag();

            StringBuilder sb = new StringBuilder();
            long dateInMillis = cursor.getLong(COL_WEATHER_DATE);
            sb.append(getReadableDateString(dateInMillis));

            String description = cursor.getString(COL_WEATHER_DESC);
            sb.append(" - " + description);

            double high = cursor.getDouble(COL_WEATHER_MAX_TEMP);

            double low = cursor.getDouble(COL_WEATHER_MIN_TEMP);

            String highAndLow = formatHighLows(high, low);
            sb.append(" - " + highAndLow);

            holder.mTextView.setText(sb.toString());
        }
    }

    private String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, filter the query to return weather only for
        // dates after or including today.

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherColumns.COLUMN_DATE + " ASC";

        long now = System.currentTimeMillis();
        long normalizedDate = normalizeDate(now);

        return new CursorLoader(getActivity(),
                WeatherContract.WeatherColumns.CONTENT_URI,
                FORECAST_COLUMNS,
                WeatherContract.WeatherColumns.COLUMN_DATE + " >= ?",
                new String[] {Long.toString(now)},
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }

}