package com.youngken.query;

import com.esri.android.map.MapView;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.tasks.identify.IdentifyParameters;
import com.youngken.tool.ApplicationContext;
import com.youngken.zy_gis_demo_001.R;

/**
 * Created by Young Ken on 2015/8/18.
 */
public class QueryIdentfyParameters extends IdentifyParameters
{

    private MapView mapView = null;
    public QueryIdentfyParameters()
    {
        super();
    }
    public QueryIdentfyParameters(MapView map)
    {
        mapView = map;
        initDefaultParamters();
    }

    public QueryIdentfyParameters(MapView map, Geometry geometry, Envelope mapExtent, SpatialReference extentSR, int[] layers, int mapWidth, int mapHeight, int dpi, boolean returnGeometry)
    {
        super(geometry,mapExtent,extentSR,layers,mapWidth,mapHeight,dpi,returnGeometry);
        mapView = map;
    }
    private void initDefaultParamters()
    {
        setTolerance(20);
        setDPI(98);
        setLayers(new int[]{4});
        try
        {
            if(mapView == null)
                throw new RuntimeException(ApplicationContext.getContext().getResources().getString(R.string.mapview_is_null));
            if(!mapView.isLoaded())
                throw new RuntimeException(ApplicationContext.getContext().getResources().getString(R.string.mapview_is_not_load));
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        setGeometry(mapView.getCenter());
        setSpatialReference(mapView.getSpatialReference());
        setMapHeight(mapView.getHeight());
        setMapWidth(mapView.getWidth());
        setReturnGeometry(true);
        Envelope env = new Envelope();
        mapView.getExtent().queryEnvelope(env);
        setMapExtent(env);
    }
}
