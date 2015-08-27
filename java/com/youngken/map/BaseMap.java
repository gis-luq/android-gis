package com.youngken.map;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.esri.android.action.IdentifyResultSpinner;
import com.esri.android.action.IdentifyResultSpinnerAdapter;
import com.esri.android.map.Callout;
import com.esri.android.map.MapOptions;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISImageServiceLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.tasks.identify.IdentifyResult;
import com.youngken.layer.LayerType;
import com.youngken.query.IQueryIdentityResult;
import com.youngken.query.QueryIdentify;
import com.youngken.zy_gis_demo_001.R;

import java.util.List;


/**
 * @author Young Ken
 * @version 0.1
 * BaseMap的子类都是受保护的构造函数，只能用单例模式去创建。
 * 这里也考虑到手机端没有开图的需求
 * 如果有开图的需求将封装mapManger
 */

public class BaseMap extends MapView implements OnStatusChangedListener,OnSingleTapListener,IQueryIdentityResult
{
    private static BaseMap mapInstance = null;
    private MapOptions options = null;
    private static Context parent = null;
    private BaseMap(Context context)
    {
        super(context);
    }

    public static BaseMap getMapInstance(Context context)
    {
        parent = context;
        if(null == mapInstance)
        {
            mapInstance = new BaseMap(context);
           // mapInstance.addLayer(new ArcGISTiledMapServiceLayer(
            //        "http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer"));
        }
        return mapInstance;
    }

    public void setServerLayer(String layerType, String url)
    {
        if(null == layerType || null == null || mapInstance == null)
            return;
        if(layerType == LayerType.IMAGE_LAYER_SERVER)
            mapInstance.addLayer(new ArcGISImageServiceLayer(url,null));
        else if(layerType == LayerType.DYNAMIC_LAYER_SERVER)
            mapInstance.addLayer(new ArcGISDynamicMapServiceLayer(url));
        else if(layerType == LayerType.TILED_LAYER_SERVER)
            mapInstance.addLayer(new ArcGISTiledMapServiceLayer(url));
    }

    /**
     * 调用初始化方法
     */
    public void initMap()
    {
        initListener();
    }

    private void initListener()
    {
        mapInstance.setOnStatusChangedListener(this);
        mapInstance.setOnSingleTapListener(this);
    }

    @Override
    public void onStatusChanged(Object o, STATUS status)
    {
        if(status == STATUS.INITIALIZED)
        {
            if(options == null)
            {
                options = new MapOptions(MapOptions.MapType.STREETS);
                options.setZoom(1);
               // options.setCenter(33.666354, -117.903557);
            }
            mapInstance.setMapOptions(options);
        }
    }
    @Override
    public void onSingleTap(float v, float v1)
    {
        int x = 0;
        if(!mapInstance.isLoaded())
            return;
        QueryIdentify queryIdentify = new QueryIdentify(mapInstance, this);
        String s = getResources().getString(R.string.identify_task_url_for_avghouseholdsize);
        queryIdentify.setQueryUrl(s);
        queryIdentify.execute();
    }
    /**
     * 这个方法要注意，如果map没有有加载完成不能设置，也就是设置无效
     * 要想设置map的属性可以调用getOptions得到MapOptions进行操作
     * @param options MapOptions
     */
    public void setOptions(MapOptions options)
    {
        if(mapInstance.isLoaded() && mapInstance != null)
            this.options = options;
    }

    public MapOptions getOptions()
    {
        return options;
    }

    /**
     *  设置天地图的图层
     * @param url 天地图服务
     */
    public void setTDTLayer(String url)
    {

    }

    /**
     * 设置高德地图的图层
     * @param url 高德地图服务
     */
    public void setALayer(String url)
    {

    }

    @Override
    public void setRestut(List<IdentifyResult> list)
    {
        Callout callout = mapInstance.getCallout();
        callout.setContent(createIdentifyContent(list));
        callout.show(mapInstance.getCenter());
    }
    private ViewGroup createIdentifyContent(final List<IdentifyResult> results)
    {
        LinearLayout linearLayout = new LinearLayout(parent);
        linearLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        IdentifyResultSpinner spinner = new IdentifyResultSpinner(parent, results);
        spinner.setClickable(false);
        //spinner.canScrollHorizontally(BIND_ADJUST_WITH_ACTIVITY);
        MyIdentifyAdapter adapter = new MyIdentifyAdapter(parent,results);
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
}
