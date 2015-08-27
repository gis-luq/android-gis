package com.youngken.tool.file;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Young Ken on 2015/8/25.
 */
public class ToolFile
{

    public static final String MAPCACHE_PATH = "MapCache";

    public static File createFile(String path)
    {
        return new File(path);
    }

    public static boolean createNewFile(String path)
    {
        File file = createFile(path);
        if(null == file)
            return false;
        try
        {
            return file.createNewFile();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isDirectory(String path)
    {
        File file = createFile(path);
        if(null == file)
            return false;
        return file.isDirectory();
    }

    public static boolean isFile(String path)
    {
        File file = createFile(path);
        if(null == file)
            return false;
        return file.isFile();
    }

    private static String getMapCachePath(String path,int level, int col, int row)
    {
        StringBuffer restltPath = new StringBuffer();
        File sdPath = ToolStorage.getSDCordFile();
        restltPath.append(sdPath);
        restltPath.append(File.separator);
        restltPath.append(MAPCACHE_PATH);
        restltPath.append(File.separator);
        restltPath.append(path);
        restltPath.append(String.format(File.separator+"%d"+File.separator+"%d_%d.ZY", level, col, row));
        return restltPath.toString();
    }


    public static boolean isExistByte(String path,int level, int col, int row)
    {
        File file = createFile(getMapCachePath(path, level, col, row));
        if(null == file)
            return false;
       return file.exists();
    }


    public synchronized static boolean saveByte(byte[] bytes, String path,int level, int col, int row)
    {
        File file = createFile(getMapCachePath(path, level, col, row));
        if(null == file)
            return false;
        File parentFile = createFile(file.getParent());

        if(!parentFile.exists())
        {
            if(parentFile.mkdirs())
            {
                if(writeToBytes(bytes,file))
                    return true;
                else
                    return false;
            }else
            {
                return false;
            }
        }else
        {
            if(file.isDirectory())
            {
                return true;
            }
            else
            {
                if(writeToBytes(bytes,file))
                    return true;
                else
                    return false;
            }
        }
    }

    public synchronized static byte[] getByte(String path,int level, int col, int row)
    {
        InputStream is = null;
        ByteArrayOutputStream bos = null;
        try
        {
            is = new FileInputStream(getMapCachePath(path,level, col, row));
            bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];

            int bytesRead = -1;
            while ((bytesRead = is.read(b)) != -1)
            {
                bos.write(b, 0, bytesRead);
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        byte[] bytes = bos.toByteArray();
        return bytes;
    }


    private static boolean mkdir(String path)
    {
        try
        {
            if (!path.endsWith(File.separator))
                path = path + File.separator;

            File file = createFile(path);
            boolean isExists = file.exists();
            if (isExists == false)
            {
                isExists = file.mkdir();
                if (isExists == true)
                {
                    return true;
                } else
                {
                    return false;
                }
            } else
            {
                return false;
            }
        } catch (Exception err)
        {
            err.printStackTrace();
            return false;
        }
    }



    public static boolean writeToBytes(byte bytes[],File file)
    {
        return writeToBytes(bytes,file.getPath());
    }

    public static boolean writeToBytes(byte bytes[],String fileName)
    {
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(fileName, true);
            fos.write(bytes);
            fos.flush();
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                if(null != fos)
                    fos.close();
                return true;

            } catch (IOException e)
            {
                e.printStackTrace();
                return false;
            }
        }
    }
}
