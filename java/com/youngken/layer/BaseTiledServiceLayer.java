package com.youngken.layer;

import android.util.Log;

import com.esri.android.map.TiledServiceLayer;
import com.esri.core.io.UserCredentials;
import com.youngken.tool.file.ToolFile;

import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

/**
 * 继承自TiledServiceLayer，设置必要参数，重写了getTile方法，重写类必须继承自这个类。
 * @author Young Ken
 * @since 2015/8/26.
 */
public class BaseTiledServiceLayer extends TiledServiceLayer
{
    private static final String TAG = BaseTiledServiceLayer.class.getName();
    protected int minLevel = 0;
    protected int maxLevel = 19;
    protected int dpi = 96;
    protected int tileWidth = 256;
    protected int tileHeight = 256;
    private BaseTiledURL baseTiledURL = null;

    public BaseTiledServiceLayer()
    {
        this(null, null, true);
    }

    public BaseTiledServiceLayer(String url)
    {
        super(url);
    }

    public BaseTiledServiceLayer(BaseTiledURL tiledURL)
    {
        this(tiledURL,null);
    }
    public BaseTiledServiceLayer(BaseTiledURL tiledURL, UserCredentials usercredentials)
    {
        this(tiledURL, usercredentials, true);
    }

    public BaseTiledServiceLayer(BaseTiledURL tiledURL, UserCredentials usercredentials, boolean flag)
    {
        super("");
        this.baseTiledURL = tiledURL;
        setCredentials(usercredentials);
        if (flag)
            try
            {
                getServiceExecutor().submit(new Runnable()
                {
                    public final void run()
                    {
                        a.initLayer();
                    }
                    final BaseTiledServiceLayer a;
                    {
                        a = BaseTiledServiceLayer.this;
                    }
                });
                return;
            } catch (RejectedExecutionException e)
            {
                Log.e(TAG, "初始化方法出错！", e);
                e.printStackTrace();
            }
    }

    /**
     * 刷新方法，清除切片
     */
    public void refresh()
    {
        try
        {
            getServiceExecutor().submit(new Runnable()
            {
                public final void run()
                {
                    if (baseTiledServiceLayer.isInitialized())
                        try
                        {
                            baseTiledServiceLayer.clearTiles();
                            return;
                        } catch (Exception e)
                        {
                            Log.e(TAG, "刷新方法出错！", e);
                        }
                }

                final BaseTiledServiceLayer baseTiledServiceLayer;
                {
                    baseTiledServiceLayer = BaseTiledServiceLayer.this;
                }
            });
            return;
        } catch (RejectedExecutionException e)
        {
            Log.e(TAG, "刷新方法出错！", e);
            return;
        }
    }

    /**
     * 判断本地有没有对应图类型的切片，把切片存在本地，有对应图类型的切片，取出切片返回。
     * @param level 地图切片级数
     * @param col 列号
     * @param row 行号
     * @return 返回 byte[]
     * @throws Exception 如果切片不存在 抛出异常
     */
    @Override
    protected byte[] getTile(int level, int col, int row) throws Exception
    {
       // String ss = baseTiledURL.getMapServiceType().getName();
       // Log.e(TAG,ss+"   "+level+"   "+col+"   "+row);
        if (level > maxLevel || level < minLevel)
        {
            return new byte[0];
        }

        /**
         * 判断有没有对应图类型的切片
         */
        if(ToolFile.isExistByte(baseTiledURL.getMapServiceType().getName(),level, col, row))
        {
            byte[] tempByte = ToolFile.getByte(baseTiledURL.getMapServiceType().getName(),level,col,row);
            //Log.e(TAG,ss+"   "+level+"   "+col+"   "+row+"   "+"     "+tempByte.length);
            return tempByte;
        }

        //String url = baseTiledURL.getTiledServiceURL(level,col,row);
        //Log.e(TAG,ss+"   "+level+"   "+col+"   "+row+"   "+" url "+url);
        Map<String, String> map = null;
        /**
         * 把切片转换成byte[]
         */
        byte[] resultByte = com.esri.core.internal.io.handler.a.a(baseTiledURL.getTiledServiceURL(level,col,row), map);
        /**
         * 保存切片
         */
        ToolFile.saveByte(resultByte, baseTiledURL.getMapServiceType().getName(), level, col, row);
       // Log.e(TAG,"saveImage"+"   "+bb);
        return resultByte;
    }
}
