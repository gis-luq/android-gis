package com.youngken.zy_gis_demo_001;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.esri.android.map.MapOptions;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnZoomListener;
import com.esri.core.tasks.identify.IdentifyResult;
import com.youngken.layer.GoogelURL;
import com.youngken.layer.GoogleTiledMapServiceLayer;
import com.youngken.layer.GoogleTiledMapServiceTypes;
import com.youngken.map.BaseMap;
import com.youngken.query.IQueryIdentityResult;

import java.util.List;

/**
 * Created by Young Ken on 2015/8/19.
 */
public class TestActivity extends Activity implements IQueryIdentityResult
{

    private MapView mapView = null;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_layou);
        final BaseMap map = BaseMap.getMapInstance(this);
        map.initMap();

        map.removeAll();

    /*    final TDTUrl tdtUrlBZ = new TDTUrl(TDTTiledMapServiceType.TDT_VECTOR_BZ);

        final TDTUrl tdtUrl = new TDTUrl(TDTTiledMapServiceType.TDT_VECTOR);
        map.addLayer(new TDTTiledMapServiceLayer(tdtUrl));
        map.addLayer(new TDTTiledMapServiceLayer(tdtUrlBZ));*/

        GoogelURL googelURL = new GoogelURL(GoogleTiledMapServiceTypes.GOOGLE_IMAGE);
        map.addLayer(new GoogleTiledMapServiceLayer(googelURL));

        //map.addLayer(new TDTTiledMapServiceLayer(new TDTUrl(Enum.valueOf(TDTTiledMapServiceType.class, TDTTiledMapServiceType.TDT_VECTOR_BZ.name().toString()))));
        //map.addLayer(new TDTTiledMapServiceLayer(new TDTUrl(Enum.valueOf(TDTTiledMapServiceType.class, TDTTiledMapServiceType.TDT_VECTOR.name().toString()))));

        map.setOnZoomListener(new OnZoomListener()
        {
            @Override
            public void preAction(float paramFloat1, float paramFloat2,
                                  double paramDouble)
            {
                // TODO Auto-generated method stub

                //���ź�
                //map_tidiOld2.clearTiles();
                //map_tidiOld2.refresh();
            }

            @Override
            public void postAction(float paramFloat1, float paramFloat2,
                                   double paramDouble)
            {
                // TODO Auto-generated method stub
                //����ǰ ��ֹ��ע�ص�
                //map_tidiOld2.clearTiles();
                //t_cva.refresh();
            }
        });
        MapOptions options = map.getOptions();
        setContentView(map);

/*
        Button zoomOutButton = (Button) findViewById(R.id.zoomOut);

        zoomInButon.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                map.zoomout();
            }
        });
*/
        //mapView = (MapView) findViewById(R.id.map);


      /*  Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                QueryIdentify queryIdentify = new QueryIdentify(mapView,TestActivity.this);
                String s = getResources().getString(R.string.identify_task_url_for_avghouseholdsize);
                queryIdentify.setQueryUrl(s);
                queryIdentify.execute();
            }
        });*/
    }

    @Override
    public void setRestut(List<IdentifyResult> list)
    {
        Log.d("ssss","sssss");
    }
}
