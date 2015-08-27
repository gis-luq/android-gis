package com.youngken.zy_gis_demo_001;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.MatrixCursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;

import com.esri.android.map.MapView;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.toolkit.map.MapViewHelper;
import com.esri.core.tasks.geocode.Locator;
import com.esri.core.tasks.geocode.LocatorFindParameters;
import com.esri.core.tasks.geocode.LocatorGeocodeResult;
import com.esri.core.tasks.geocode.LocatorSuggestionParameters;
import com.esri.core.tasks.geocode.LocatorSuggestionResult;

import java.util.List;


public class MainActivity extends Activity
{
    private static final String TAG = "PlaceSearch";
    private static final String COLUMN_NAME_ADDRESS = "address";
    private static final String COLUMN_NAME_X = "x";
    private static final String COLUMN_NAME_Y = "y";
    private static final String LOCATION_TITLE = "Location";
    private static ProgressDialog mProgressDialog = null;
    private MapViewHelper mMapViewHelper = null;
    private String mMapViewState = null;
    private MenuItem searchMenuItem = null;
    private Locator mLocator = null;
    private MatrixCursor mSuggestionCursor = null;
    private SearchView mSearchView = null;
    private SimpleCursorAdapter mSuggestionAdapter = null;
    MapView mapView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView =(MapView)findViewById(R.id.map);

        mProgressDialog = new ProgressDialog(this)
        {
            @Override
            public void onBackPressed()
            {
                mProgressDialog.dismiss();
            }
        };
        mMapViewHelper = new MapViewHelper(mapView);
        mLocator = Locator.createOnlineLocator();
        mapView.setEsriLogoVisible(true);
        mapView.enableWrapAround(true);

        mapView.setOnStatusChangedListener(new OnStatusChangedListener()
        {
            @Override
            public void onStatusChanged(Object o, STATUS status)
            {
                if (o == mapView && status == STATUS.INITIALIZED)
                {
                    if (mMapViewState == null)
                    {
                        Log.i(TAG, "MapView.setOnStatusChangedListener() status=" + status.toString());
                    } else
                    {
                        mapView.restoreState(mMapViewState);
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if(id == R.id.action_search)
        {
            searchMenuItem = item;
            initSearchView();
            item.setActionView(mSearchView);
            return  true;
        }else if (id == R.id.action_clear)
        {
            if(mMapViewHelper != null)
                mMapViewHelper.removeAllGraphics();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        if (mSearchView != null && !mSearchView.isIconified())
        {
            if (searchMenuItem != null) {
                searchMenuItem.collapseActionView();
                invalidateOptionsMenu();
            }
        } else{
            super.onBackPressed();
        }
    }

    private void suggestPlace(String placeName)
    {
        if(mLocator == null)
            return;
        new SuggestPlaceTask(mLocator).execute(placeName);
    }

    private void applySuggestionCursor()
    {
        String[] cols = new String[]{COLUMN_NAME_ADDRESS};
        int[] to = new int[]{R.id.suggestion_item_address};
        mSuggestionAdapter = new SimpleCursorAdapter(mapView.getContext(), R.layout.suggestion_item, mSuggestionCursor, cols, to, 0);
        mSearchView.setSuggestionsAdapter(mSuggestionAdapter);
        mSuggestionAdapter.notifyDataSetChanged();
    }

    private void initSearchView() {
        if (mapView == null || !mapView.isLoaded())
            return;

        mSearchView = new SearchView(this);
        mSearchView.setFocusable(true);
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setQueryHint(getResources().getString(R.string.search_hint));

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {

            @Override
            public boolean onQueryTextSubmit(String query)
            {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                suggestPlace(newText);
                return true;
            }
        });

        mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener()
        {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                // Obtain the content of the selected suggesting place via cursor
                MatrixCursor cursor = (MatrixCursor) mSearchView.getSuggestionsAdapter().getItem(position);
                int indexColumnSuggestion = cursor.getColumnIndex(COLUMN_NAME_ADDRESS);
                int indexColumnX = cursor.getColumnIndex(COLUMN_NAME_X);
                int indexColumnY = cursor.getColumnIndex(COLUMN_NAME_Y);
                String address = cursor.getString(indexColumnSuggestion);
                double x = cursor.getDouble(indexColumnX);
                double y = cursor.getDouble(indexColumnY);

                if ((x == 0.0) && (y == 0.0)) {
                    // Place has not been located. Find the place
                    new FindPlaceTask(mLocator).execute(address);
                } else {
                    // Place has been located. Zoom to the place and add a marker for this place
                    mMapViewHelper.addMarkerGraphic(y, x, LOCATION_TITLE, address, android.R.drawable.ic_menu_myplaces, null, false, 1);
                    mapView.centerAndZoom(y, x, 14);
                    mSearchView.setQuery(address, true);
                }
                cursor.close();

                return true;
            }
        });
    }

    private class SuggestPlaceTask extends AsyncTask<String, Void, List<LocatorSuggestionResult>> {
        private Locator mLocator;

        public SuggestPlaceTask(Locator locator) {
            mLocator = locator;
        }

        @Override
        protected List<LocatorSuggestionResult> doInBackground(String... queries) {
            for (String query : queries) {
                // Create suggestion parameter
                LocatorSuggestionParameters params = new LocatorSuggestionParameters(query);
                //Set the location to be used for proximity based suggestion
                params.setLocation(mapView.getCenter(), mapView.getSpatialReference());
                // Set the radial search distance in meters
                params.setDistance(500.0);

                List<LocatorSuggestionResult> results = null;
                try {
                    results = mLocator.suggest(params);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return results;
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<LocatorSuggestionResult> results) {
            if (results == null) {
                return;
            }

            int key = 0;
            // Add suggestion list to a cursor
            initSuggestionCursor();
            for (LocatorSuggestionResult result : results) {
                mSuggestionCursor.addRow(new Object[]{key++, result.getText(), "0", "0"});
            }

            applySuggestionCursor();
        }
    }

    private class FindPlaceTask extends AsyncTask<String, Void, List<LocatorGeocodeResult>> {
        private static final String SUGGESTION_ADDRESS_DELIMNATOR = ", ";
        private Locator mLocator;

        public FindPlaceTask(Locator locator) {
            mLocator = locator;
        }

        @Override
        protected List<LocatorGeocodeResult> doInBackground(String... queries) {
            for (String query : queries) {
                // Create Locator parameters from single line address string
                LocatorFindParameters params;
                int index = query.indexOf(SUGGESTION_ADDRESS_DELIMNATOR);
                if (index > 0) {
                    params = new LocatorFindParameters(query.substring(index + SUGGESTION_ADDRESS_DELIMNATOR.length()));
                } else {
                    params = new LocatorFindParameters(query);
                }
                // Use the centre of the current map extent as the find location point
                params.setLocation(mapView.getCenter(), mapView.getSpatialReference());
                // Set the radial search distance in meters
                params.setDistance(500.0);

                // Execute the task
                List<LocatorGeocodeResult> results = null;
                try {
                    results = mLocator.find(params);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return results;
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            // Display progress dialog on UI thread
            mProgressDialog.setMessage(getString(R.string.address_search));
            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(List<LocatorGeocodeResult> results) {
            // Dismiss progress dialog
            mProgressDialog.dismiss();
            if ((results == null) || (results.size() == 0))
                return;

            // Add the first result to the map and zoom to it
            LocatorGeocodeResult result = results.get(0);
            double x = result.getLocation().getX();
            double y = result.getLocation().getY();
            String address = result.getAddress();
            // Add a marker at the found place. When tapping on the marker, a Callout with the address
            // will be displayed
            mMapViewHelper.addMarkerGraphic(y, x, LOCATION_TITLE, address, android.R.drawable.ic_menu_myplaces, null, false, 1);
            mapView.centerAndZoom(y, x, 14);
            mSearchView.setQuery(address, true);

            // Hide soft keyboard
            mSearchView.clearFocus();
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
        }
    }
    private void initSuggestionCursor() {
        String[] cols = new String[]{BaseColumns._ID, COLUMN_NAME_ADDRESS, COLUMN_NAME_X, COLUMN_NAME_Y};
        mSuggestionCursor = new MatrixCursor(cols);
    }
}

