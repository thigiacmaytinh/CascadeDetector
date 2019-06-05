package com.thigiacmaytinh.CascadeDetector;

import android.content.Context;

import org.opencv.core.Mat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Scanner;

import static org.opencv.imgcodecs.Imgcodecs.imwrite;

/**
 * Created by vi.vohung on 1/7/2016.
 */
public class FileProcess {


    public static boolean Exist(String filePath)
    {
        File file = new File(filePath);
        return file.exists();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String ReadAllText(String filePath)
    {
        String result = "";
        try
        {
            Scanner scan = new Scanner(new File(filePath));
            while (scan.hasNext()) {
                result += scan.nextLine() + "\n";
            }
            scan.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static String ReadAllTextFromResource(Context c, int fileID)
    {
        String data;
        InputStream in= c.getResources().openRawResource(fileID);
        InputStreamReader inputStreamReader =new InputStreamReader(in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder builder=new StringBuilder();
        if(in!=null)
        {
            try
            {
                while((data = bufferedReader.readLine())!=null)
                {
                    builder.append(data);
                    builder.append("\n");
                }
                in.close();
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return builder.toString();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static void WriteAllText(Context c, String filePath, String content)
    {
        try {

            FileOutputStream out = c.openFileOutput(filePath, Context.MODE_PRIVATE);
            OutputStreamWriter writer = new OutputStreamWriter(out);
            writer.write(content);

            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static void AppendAllText(Context c, String filePath, String content)
    {
        try {

            FileOutputStream out = c.openFileOutput(filePath, Context.MODE_APPEND);
            OutputStreamWriter writer = new OutputStreamWriter(out);
            writer.write(content);

            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    static void WriteToDisk(Mat m, String fileName)
    {
        if(!Registry.enableCheat)
            return;


        File file = new File(Common.path, fileName + ".jpg");
        boolean success = imwrite(file.getAbsolutePath(), m);
    }
}
