package com.thigiacmaytinh.CascadeDetector;

import android.content.Context;
import android.os.Environment;
import android.provider.Settings;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by i5 on 12/27/2015.
 */
public class Common {

    public static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/CascadeDetector";
    public static String prefName = "CascadeDetectorSetting";
    public static String d = ">>>>>>>>>>>>>>>";

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static byte[] ReadByteFromFile(Context c, String fileName)
    {
        byte[] fileBytes = null;
        try {
            InputStream is = c.getAssets().open(fileName);
            fileBytes = new byte[is.available()];
            is.read(fileBytes);
            is.close();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        //System.out.println(fileBytes);
        return fileBytes;
    }
}
