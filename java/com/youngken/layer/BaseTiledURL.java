package com.youngken.layer;

/**
 * 把行列号转换成url
 * @author Young Ken
 * @since 2015/8/26.
 */
public interface BaseTiledURL
{
    String getTiledServiceURL(int level, int col, int row);
    BaseTiledMapServiceType getMapServiceType();
}
