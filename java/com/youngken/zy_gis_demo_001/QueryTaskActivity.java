package com.youngken.zy_gis_demo_001;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Feature;
import com.esri.core.map.FeatureResult;
import com.esri.core.map.Graphic;
import com.esri.core.renderer.SimpleRenderer;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.tasks.query.QueryParameters;
import com.esri.core.tasks.query.QueryTask;

/**
 * Created by Young Ken on 2015/8/17.
 */
public class QueryTaskActivity extends Activity
{

    private GraphicsLayer graphicsLayer = null;
    private String queryUrl = null;
    MapView mapView = null;
    private ProgressDialog progress = null;
    boolean boolQuery = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = (MapView) findViewById(R.id.map);

        queryUrl = getResources().getString(R.string.query_service);

        mapView.setOnStatusChangedListener(new OnStatusChangedListener()
        {
            @Override
            public void onStatusChanged(Object o, STATUS status)
            {
                if(o == mapView && status == STATUS.INITIALIZED)
                {
                    graphicsLayer = new GraphicsLayer();
                    SimpleFillSymbol simpleFillSymbol = new SimpleFillSymbol(Color.RED);
                    SimpleRenderer simpleRenderer = new SimpleRenderer(simpleFillSymbol);
                    graphicsLayer.setRenderer(simpleRenderer);
                    mapView.addLayer(graphicsLayer);
                }
            }
        });
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
             if(mapView != null && mapView.isLoaded())
             {
                 mapView.zoomout();
             }
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.action, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.avg_household:
                String target = queryUrl.concat("/3");
                String[] queryArray = {target,"AVGHHSZ_CY>0"};
                AsyncQueryTask queryTask = new AsyncQueryTask();
                queryTask.execute(queryArray);
                return true;
            case R.id.reset:
                graphicsLayer.removeAll();
                boolQuery = true;
        }
        return true;
    }

    private class AsyncQueryTask extends AsyncTask<String , Void, FeatureResult>
    {

        protected void onPreExecute()
        {
            progress = new ProgressDialog(QueryTaskActivity.this);
            progress = ProgressDialog.show(QueryTaskActivity.this, "",
                    "Please wait....query task is executing");
        }

        protected FeatureResult doInBackground(String... queryArray)
        {
            if(queryArray == null || queryArray.length <= 1)
                return null;
            String url = queryArray[0];
            QueryParameters parameters = new QueryParameters();
            String whereCase = queryArray[1];
            SpatialReference reference = SpatialReference.create(102100);
            parameters.setGeometry(mapView.getCenter());
            parameters.setOutSpatialReference(reference);
            parameters.setReturnGeometry(true);
            parameters.setWhere(whereCase);

            QueryTask queryTask = new QueryTask(url);

            try {
                FeatureResult featureResult = queryTask.execute(parameters);
                return  featureResult;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(FeatureResult results)
        {
            String message = "No result comes back";

            if(results != null)
            {
                int size = (int) results.featureCount();
                for(Object element : results)
                {
                    progress.incrementProgressBy(size / 100);
                    if(element instanceof Feature)
                    {
                        Feature feature = (Feature) element;
                        Graphic graphic = new Graphic(feature.getGeometry(),feature.getSymbol(),feature.getAttributes());
                        graphicsLayer.addGraphic(graphic);
                    }
                }
            }
            message = String.valueOf(results.featureCount())
                    + " results have returned from query.";

            progress.dismiss();
            Toast toast = Toast.makeText(QueryTaskActivity.this, message,
                    Toast.LENGTH_LONG);
            toast.show();
            boolQuery = false;
        }
    }
}
