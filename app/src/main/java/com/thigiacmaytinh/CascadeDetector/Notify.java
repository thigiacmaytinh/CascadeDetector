package com.thigiacmaytinh.CascadeDetector;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

/**
 * Created by i5 on 1/1/2016.
 */
public class Notify
{

    public static void ShowToast(Context c, String text)
    {
        Toast.makeText(c, text, Toast.LENGTH_SHORT).show();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static void ShowPopup(Context c, String text)
    {
        if (c !=null)
        {
            new AlertDialog.Builder(c)
                    .setMessage(text)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static void ShowDialog(Context c, String title, String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle(title) //
                .setMessage(message) //
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // TODO
                        dialog.dismiss();
                    }
                }) //
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // TODO
                        dialog.dismiss();
                    }
                });
        builder.show();
    }
}
