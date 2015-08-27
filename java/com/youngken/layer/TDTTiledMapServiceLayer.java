package com.youngken.layer;

import android.util.Log;

import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;

import java.util.concurrent.RejectedExecutionException;

/**
 * Created by Young Ken on 2015/8/21.
 */
public class TDTTiledMapServiceLayer extends BaseTiledServiceLayer
{
    Point origin = new Point(-180, 90);
    int levels=21;
    double[] resolutions = {
            1.40625,
            0.703125,
            0.3515625,
            0.17578125,
            0.087890625,
            0.0439453125,
            0.02197265625,
            0.010986328125,
            0.0054931640625,
            0.00274658203125,
            0.001373291015625,
            0.0006866455078125,
            0.00034332275390625,
            0.000171661376953125,
            8.58306884765629E-05,
            4.29153442382814E-05,
            2.14576721191407E-05,
            1.07288360595703E-05,
            5.36441802978515E-06,
            2.68220901489258E-06,
            1.34110450744629E-06
    };
    double[] scales = {
            400000000,
            295497598.5708346,
            147748799.285417,
            73874399.6427087,
            36937199.8213544,
            18468599.9106772,
            9234299.95533859,
            4617149.97766929,
            2308574.98883465,
            1154287.49441732,
            577143.747208662,
            288571.873604331,
            144285.936802165,
            72142.9684010827,
            36071.4842005414,
            18035.7421002707,
            9017.87105013534,
            4508.93552506767,
            2254.467762533835,
            1127.2338812669175,
            563.616940
    };
    public TDTTiledMapServiceLayer(BaseTiledURL tiledURL)
    {
        super(tiledURL);
        init();
    }

    private synchronized void init()
    {
        try
        {
            getServiceExecutor().submit(new Runnable()
            {
                public void run()
                {
                    TDTTiledMapServiceLayer.this.initLayer();
                }
            });
        } catch (RejectedExecutionException rejectedexecutionexception)
        {
            Log.e("Google Map Layer", "initialization of the layer failed.",
                    rejectedexecutionexception);
        }
    }

    protected synchronized void initLayer()
    {
        if (getID() == 0L)
        {
            nativeHandle = create();
            changeStatus(com.esri.android.map.event.OnStatusChangedListener.STATUS.fromInt(-1000));
        } else
        {
            this.setTileInfo(new com.esri.android.map.TiledServiceLayer.TileInfo(origin, scales, resolutions, levels, dpi, tileWidth, tileHeight));
            this.setFullExtent(new Envelope(-180, -90, 180, 90));
            this.setDefaultSpatialReference(SpatialReference.create(4490));   //CGCS2000
            this.setInitialExtent(new Envelope(90.52,33.76,113.59,42.88));
            super.initLayer();
        }
    }
}
