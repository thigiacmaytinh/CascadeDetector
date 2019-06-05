package com.thigiacmaytinh.CascadeDetector;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static org.opencv.core.Core.flip;
import static org.opencv.core.Core.transpose;
import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.drawContours;
import static org.opencv.imgproc.Imgproc.getRotationMatrix2D;
import static org.opencv.imgproc.Imgproc.warpAffine;

/**
 * Created by vi.vohung on 12/30/2015.
 */
public class Image {

    static Mat Rotate(Mat img, int angle)
    {
        int len = max(img.cols(), img.rows());
        Point pt  = new Point(len / 2, len / 2);
        Mat r = getRotationMatrix2D(pt, angle, 1.0);
        Mat dst = new Mat();
        warpAffine(img, dst, r, new Size(len, len));

        return dst;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    static Mat ConvertToGray(Mat input)
    {
        if (input.channels() == 1)
        {
            return input;
        }
        else
        {
            Mat gray = new Mat();
            cvtColor(input, gray, Imgproc.COLOR_BGR2GRAY);
            return gray;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    static List<MatOfPoint> GetValidContour(List<MatOfPoint> contours, Size min, Size max)
    {
        List<MatOfPoint> result = new ArrayList<MatOfPoint>();
        for (int i = 0; i < contours.size(); ++i)
        {
            Rect r = boundingRect(contours.get(i));
            if (min.width <= r.width && min.height <= r.height && r.width <= max.width && r.height <= max.height)
            {
                result.add(contours.get(i));
            }
        }
        return result;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    static List<MatOfPoint> GetValidContour(List<MatOfPoint> contours, int min, int max)
    {
        List<MatOfPoint> result = new ArrayList<MatOfPoint>();
        for (int i = 0; i < contours.size(); ++i)
        {
            Rect r = boundingRect(contours.get(i));
            int size = max(r.width, r.height);
            if (min <= size && size <= max)
            {
                result.add(contours.get(i));
            }
        }
        return result;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    static Mat DrawContours(int matWidth, int matHeight, List<MatOfPoint> contours, boolean multiColor)
    {
        Random rng = new Random();
        Scalar color = new Scalar(255);
        Mat matDraw = Mat.zeros(matWidth, matHeight, CvType.CV_8UC3);
        for (int i = 0; i < contours.size(); ++i)
        {
            if(multiColor)
            color = new Scalar(rng.nextInt(255), rng.nextInt(255), rng.nextInt(255));
            drawContours(matDraw, contours, i, color, 1);
        }
        return matDraw;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    static List<MatOfPoint> ClampContourBySize(List<MatOfPoint> contours, int minValue, int maxValue)
    {
        List<MatOfPoint> result = new ArrayList<MatOfPoint>();
        for (int i = 0; i < contours.size(); ++i)
        {
            Rect r = boundingRect(contours.get(i));

            float distance = abs(r.width - r.height) / (r.width + r.height);
            if(distance < 0.5 )
            {
                if ((minValue <= r.width && r.width <= maxValue) && (minValue <= r.height && r.height <= maxValue))
                {
                    result.add(contours.get(i));
                }
            }
        }
        return result;
    }
}
