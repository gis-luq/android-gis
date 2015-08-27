package com.youngken.tool.file;

import android.os.Environment;
import android.os.StatFs;

import java.io.File;

/**
 * Created by Young Ken on 2015/8/25.
 */
public class ToolStorage
{
    public static boolean isExistSDCard()
    {
        if(android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED))
            return true;
        else
            return false;
    }


    public static File getSDCordFile()
    {
        if(isExistSDCard())
            return Environment.getExternalStorageDirectory();
        else return null;
    }

    private static StatFs getStatFs()
    {
        File file = getSDCordFile();
        if(file == null)
            return null;
        return new StatFs(file.getPath());
    }

    /**
     *  SD��ʣ��ռ�
     * @return �������sd���󷵻�-1�����ص�λ MB.
     */
    public static long getSDFreeSize()
    {
        StatFs sf = getStatFs();
        if(sf == null)
            return -1;
        long blockSize = sf.getBlockSize();
        long freeBlocks = sf.getAvailableBlocks();
        return (freeBlocks * blockSize)/1048576;  //1048576 = 1024*1024
    }

    /**
     * SD��������
     * @return �������sd���󷵻�-1�����ص�λ MB.
     */
    public static long getSDAllSize()
    {
        StatFs sf = getStatFs();
        if(sf == null)
            return -1;
        long blockSize = sf.getBlockSize();
        long allBlocks = sf.getBlockCount();
        return (allBlocks * blockSize)/1048576; //1048576 = 1024*1024
    }
}
