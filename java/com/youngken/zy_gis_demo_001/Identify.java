package com.youngken.zy_gis_demo_001;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.action.IdentifyResultSpinner;
import com.esri.android.action.IdentifyResultSpinnerAdapter;
import com.esri.android.map.Callout;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.tasks.identify.IdentifyParameters;
import com.esri.core.tasks.identify.IdentifyResult;
import com.esri.core.tasks.identify.IdentifyTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Young Ken on 2015/8/18.
 */
public class Identify extends Activity
{
    private MapView mapView = null;
    private IdentifyParameters params = null;
    public ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.identify_server);
        mapView =(MapView)findViewById(R.id.mapIdentify);
        mapView.addLayer(
                new ArcGISTiledMapServiceLayer(
                        this.getResources().getString(
                                R.string.identify_task_url_for_avghouseholdsize)));

        params = new IdentifyParameters();
        params.setTolerance(20);
        params.setDPI(98);
        params.setLayers(new int[]{4});
        params.setLayerMode(IdentifyParameters.ALL_LAYERS);

        mapView.setOnSingleTapListener(new OnSingleTapListener()
        {
            @Override
            public void onSingleTap(float x, float y)
            {
                if(!mapView.isLoaded())
                    return;

                Point point = mapView.toMapPoint(x,y);
                params.setGeometry(point);
                params.setSpatialReference(mapView.getSpatialReference());
                params.setMapHeight(mapView.getHeight());
                params.setMapWidth(mapView.getWidth());
                params.setReturnGeometry(true);
                Envelope env = new Envelope();
                mapView.getExtent().queryEnvelope(env);
                params.setMapExtent(env);

                MyIdentifyTask task = new MyIdentifyTask(point);
                task.execute(params);
            }
        });
    }
    private ViewGroup createIdentifyContent(final List<IdentifyResult> results)
    {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        IdentifyResultSpinner spinner = new IdentifyResultSpinner(this, results);
        spinner.setClickable(false);
        //spinner.canScrollHorizontally(BIND_ADJUST_WITH_ACTIVITY);
        MyIdentifyAdapter adapter = new MyIdentifyAdapter(this,results);
        spinner.setAdapter(adapter);
        spinner.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        linearLayout.addView(spinner);
        return linearLayout;
    }
    public class MyIdentifyAdapter extends IdentifyResultSpinnerAdapter
    {
        String show = null;
        List<IdentifyResult> results = null;
        int currentDataViewed = -1;
        Context context = null;

        public MyIdentifyAdapter(Context c, List<IdentifyResult> r)
        {
            super(c,r);
            context = c;
            results = r;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            String LSP = System.getProperty("line.separator");
            StringBuilder outputVal = new StringBuilder();
            Resources res = getResources();
            IdentifyResult curResult = this.results.get(position);

            if(curResult.getAttributes().containsKey(res.getString(R.string.NAME)))
            {
                outputVal.append("Place: "+curResult.getAttributes().get(R.string.NAME)).toString();
                outputVal.append(LSP);
            }
            if (curResult.getAttributes().containsKey(
                    res.getString(R.string.ID))) {
                outputVal.append("State ID: "
                        + curResult.getAttributes()
                        .get(res.getString(R.string.ID)).toString());
                outputVal.append(LSP);
            }

            if (curResult.getAttributes().containsKey(
                    res.getString(R.string.ST_ABBREV))) {
                outputVal.append("Abbreviation: "
                        + curResult.getAttributes()
                        .get(res.getString(R.string.ST_ABBREV))
                        .toString());
                outputVal.append(LSP);
            }

            if (curResult.getAttributes().containsKey(
                    res.getString(R.string.TOTPOP_CY))) {
                outputVal.append("Population: "
                        + curResult.getAttributes()
                        .get(res.getString(R.string.TOTPOP_CY))
                        .toString());
                outputVal.append(LSP);

            }

            if (curResult.getAttributes().containsKey(
                    res.getString(R.string.LANDAREA))) {
                outputVal.append("Area: "
                        + curResult.getAttributes()
                        .get(res.getString(R.string.LANDAREA))
                        .toString());
                outputVal.append(LSP);
            }
            TextView textView = null;
            textView = new TextView(this.context);
            textView.setText(outputVal);
            textView.setTextColor(Color.RED);
            textView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            textView.setGravity(Gravity.CENTER_VERTICAL);
            return textView;
        }
    }
    private class MyIdentifyTask extends AsyncTask<IdentifyParameters, Void, IdentifyResult[]>
    {
        IdentifyTask task = new IdentifyTask(Identify.this.getResources().getString(R.string.identify_task_url_for_avghouseholdsize));
        IdentifyResult[] results = null;
        Point point = null;

        public MyIdentifyTask(Point p)
        {
            point = p;
        }

        @Override
        protected void onPostExecute(IdentifyResult[] r) {
            if (dialog.isShowing())
                dialog.dismiss();
            ArrayList<IdentifyResult> resultList = new ArrayList<>();
            IdentifyResult result_1 = null;
            if(r == null)
            {
                Toast.makeText(Identify.this,"���Ϊ�գ�����",Toast.LENGTH_LONG);
                return;
            }

            for (int i = 0; i < r.length; i++)
            {
                result_1 = r[i];
                String displayFieldName = result_1.getDisplayFieldName();
                Map<String, Object> attr = result_1.getAttributes();
                for (String key : attr.keySet())
                {
                    if (key.equalsIgnoreCase(displayFieldName))
                    {
                        resultList.add(result_1);
                    }
                }
            }

            Callout callout = mapView.getCallout();
            callout.setContent(createIdentifyContent(resultList));
            callout.show(point);
        }

        @Override
        protected IdentifyResult[] doInBackground(IdentifyParameters... params)
        {
            if(params != null && params.length >= 0)
            {
                IdentifyParameters parameters = params[0];
                try {
                    results = task.execute(parameters);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return results;
        }

        @Override
        protected void onPreExecute()
        {
            dialog =  ProgressDialog.show(Identify.this, "Identify Task",
                    "Identify query ...");
        }
    }
}
