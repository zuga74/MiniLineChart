package com.skbturbina.apn;

import android.graphics.Paint;
import android.graphics.PointF;

import java.util.ArrayList;

public class MiniLineChartSeries {

    //точки
    //points
    private final ArrayList<PointF> Points  = new ArrayList<>();

    //минимумы и максимумы
    //lows and highs
    private float minX, maxX, minY, maxY;

    //линия графика
    //graph line
    private Paint LinePaint;

    //максимальное кол-во точек
    //maximum number of points
    private int AMaxPoints;

    //тег
    //tag
    private String ATag;

    public static final float INVALID_FLOAT = (float) -3.4E+38;

    private static final String TAG = "MiniLineChartSeries";

    private void Init() {
        LinePaint = new Paint();
        LinePaint.setStrokeWidth(1);
        LinePaint.setStyle(Paint.Style.FILL);
        Clear();
        AMaxPoints = 1000;
        ATag = "";
    }

    public MiniLineChartSeries() {
        Init();
    }

    public MiniLineChartSeries(int color) {
        Init();
        LinePaint.setColor(color);
    }

    public MiniLineChartSeries(int color, String Tag) {
        Init();
        LinePaint.setColor(color);
        ATag = Tag;
    }

    //вычисление минимума по Y
    //calculate the minimum by Y
    private float getMin_Y() {
        float Min_Y = Points.get(0).y;
        PointF point;

        for (int i = 1; i < Points.size(); i++) {
            point = Points.get(i);
            if (point.y < Min_Y) Min_Y = point.y;
        }
        return Min_Y;
    }

    //вычисление максимума по Y
    //calculate the maximum by Y
    private float getMax_Y() {
        float Max_Y = Points.get(0).y;
        PointF point;

        for (int i = 1; i < Points.size(); i++) {
            point = Points.get(i);
            if (point.y > Max_Y) Max_Y = point.y;
        }
        return Max_Y;
    }

    //удаление нулевой точки с вычислением минимумов и максимумов
    //remove zero point with calculation of minimums and maximums
    private void PointRemove0() {
        if (Points.size() == 0) return;
        PointF old_point = Points.get(0);
        Points.remove(0);
        minX = Points.get(0).x;
        if (old_point.y == minY) {
            minY = getMin_Y();
        }
        else if (old_point.y == maxY) {
            maxY = getMax_Y();
        }
    }

    //добавление точки
    //add point
    public void AddPoint(float x, float y) {

        if (Points.size() >= AMaxPoints) {
            PointRemove0();
        }

        PointF point = new PointF(x, y);

        if ( (minX == INVALID_FLOAT) || (point.x < minX)) minX = point.x;
        if ( (maxX == INVALID_FLOAT) || (point.x > maxX) ) maxX = point.x;

        if ( (minY == INVALID_FLOAT) || (point.y < minY) ) minY = point.y;
        if ( (maxY == INVALID_FLOAT) || (point.y > maxY) ) maxY = point.y;

        Points.add(point);
    }

    public float getMinX() { return minX; }

    public float getMaxX() { return maxX; }

    public float getMinY() { return minY; }

    public float getMaxY() { return maxY; }

    public int getSize() { return Points.size(); }

    public PointF getPoint(int index) { return Points.get(index); }

    public Paint getPaint() { return LinePaint; }

    public String getTag() { return ATag; }

    public void Clear() {
        minX = INVALID_FLOAT;
        maxX  = INVALID_FLOAT;
        minY  = INVALID_FLOAT;
        maxY = INVALID_FLOAT;
        Points.clear();
    }

    public void setMaxPonts(int MaxPoints) { AMaxPoints = MaxPoints; }

}
