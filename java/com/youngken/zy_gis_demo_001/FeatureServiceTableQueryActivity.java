package com.youngken.zy_gis_demo_001;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.MapView;
import com.esri.core.geodatabase.GeodatabaseFeatureServiceTable;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.CodedValueDomain;
import com.esri.core.map.Feature;
import com.esri.core.map.FeatureResult;
import com.esri.core.map.Field;
import com.esri.core.tasks.query.QueryParameters;


/**
 * Created by Young Ken on 2015/8/18.
 */
public class FeatureServiceTableQueryActivity extends Activity
{
    final String FEATURE_SERVICE_URL = "http://sampleserver6.arcgisonline.com/arcgis/rest/services/DamageAssessment/FeatureServer";
    final String DAMAGE_FIELD_NAME = "typdamage";
    final String CAUSE_FIELD_NAME = "primcause";
    public FeatureLayer featureLayer;
    public GeodatabaseFeatureServiceTable featureServiceTable;
    private MapView mapView;
    Spinner mDamageSpinner;
    Spinner mCauseSpinner;
    ArrayAdapter<String> damageAdapter;
    ArrayAdapter<String> causeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feature_service);
        mapView = (MapView) findViewById(R.id.mapServer);
        featureServiceTable = new GeodatabaseFeatureServiceTable(FEATURE_SERVICE_URL, 0);
        featureServiceTable.initialize(new CallbackListener<GeodatabaseFeatureServiceTable.Status>()
        {
            @Override
            public void onCallback(GeodatabaseFeatureServiceTable.Status status)
            {
                featureLayer = new FeatureLayer(featureServiceTable);
                featureLayer.setSelectionColor(-16711936);
                featureLayer.setSelectionColorWidth(20);
                mapView.addLayer(featureLayer);
                setupQuerySpinners();
                Field damageField = featureServiceTable.getField(DAMAGE_FIELD_NAME);
                Field causeField = featureServiceTable.getField(CAUSE_FIELD_NAME);

                CodedValueDomain damageDomain = (CodedValueDomain) damageField.getDomain();
                CodedValueDomain causeDomain = (CodedValueDomain) causeField.getDomain();
                damageAdapter.addAll(damageDomain.getCodedValues().values());
                causeAdapter.addAll(causeDomain.getCodedValues().values());

                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mDamageSpinner.setAdapter(damageAdapter);
                        mCauseSpinner.setAdapter(causeAdapter);
                    }
                });
            }

            @Override
            public void onError(Throwable throwable)
            {
                showToast("Error initializing FeatureServiceTable");
            }
        });
    }

    public void onClick_okButton(View v)
    {
        if (featureLayer == null)
        {
            showToast("Feature layer is not set.");
            return;
        }

        featureLayer.clearSelection();
        String damageType = String.valueOf(mDamageSpinner.getSelectedItem());
        String primCause = String.valueOf(mCauseSpinner.getSelectedItem());
        String whereClause = "typdamage LIKE '" + damageType + "' AND primcause LIKE '" + primCause + "'";
        QueryParameters queryParams = new QueryParameters();
        queryParams.setWhere(whereClause);
        featureServiceTable.queryFeatures(queryParams, new CallbackListener<FeatureResult>()
        {
            @Override
            public void onCallback(FeatureResult objects)
            {
                if (objects.featureCount() < 1)
                {
                    showToast("No results");
                    return;
                }
                showToast("Found " + objects.featureCount() + " features.");
                for (Object objFeature : objects)
                {
                    Feature feature = (Feature) objFeature;
                    featureLayer.selectFeature(feature.getId());
                }
            }

            @Override
            public void onError(Throwable throwable)
            {
                showToast("Error querying FeatureServiceTable");
            }
        });
    }

    public void showToast(final String message)
    {
        // Show toast message on the main thread only; this function can be
        // called from query callbacks that run on background threads.
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(FeatureServiceTableQueryActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setupQuerySpinners()
    {
        mDamageSpinner = (Spinner) findViewById(R.id.damageSpinner);
        mCauseSpinner = (Spinner) findViewById(R.id.causeSpinner);
        damageAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item);
        causeAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item);
    }
}
