package com.skbturbina.apn;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Locale;

public class MiniLineChart extends View {

    private static final String TAG = "MiniLineChart";

    //максиимально возможный зум
    // maximum possible zoom
    private int MaxZoom;

    //серии
    //series
    private final ArrayList<MiniLineChartSeries> SeriesArr  = new ArrayList<>();

    //линии осей
    //axis lines
    private Paint AxisLinePaint;

    //текст значений на осях
    //text of values on axes
    private Paint AxisTextPaint;

    //кол-во сеток
    //number of grids
    private int AGridCount;

    //формат для вывод текста значений но осях
    //format for displaying the text of values along the axes
    private String AAxisTextFormat;

    //current zoom
    //текущии zoom
    private int AZoom;

    //текуща точка zoom-а
    //current zoom point
    private PointF AZoomPoint;

    private RectF MiniLineChartGridPos;

    //границы сетки в значениях графиков
    //grid borders on MiniLineChart in pixels
    private RectF GridPos;

    //ширина сетки
    //grid width
    private float GridWidth;

    //высота сетки
    //grid height
    private float GridHeight;

    //ширина границы сетки на MiniLineChart в пикселях
    //width of grid border on MiniLineChart in pixels
    private float MiniLineChartGridWidth;

    //высота границы сетки на MiniLineChart в пикселях
    //height of the grid border on MiniLineChart in pixels
    private float MiniLineChartGridHeight;

    //минимумы и максимумы графикоф
    //lows and highs of the chart
    private RectF GraphMinMax;

    //жесты
    //gestures
    private GestureDetector gestureDetector;


    private void init(){
        AxisLinePaint = new Paint();
        AxisLinePaint.setColor(Color.GRAY);
        AxisLinePaint.setStrokeWidth(1);
        AxisLinePaint.setStyle(Paint.Style.STROKE);

        AxisTextPaint = new Paint();

        AGridCount = 5;
        AAxisTextFormat = "%.1f";
        AZoom = 1;
        AZoomPoint = new PointF(MiniLineChartSeries.INVALID_FLOAT, MiniLineChartSeries.INVALID_FLOAT);

        MiniLineChartGridPos = new RectF();
        GridPos = new RectF();
        MaxZoom = 10;

        GraphMinMax = new RectF(MiniLineChartSeries.INVALID_FLOAT, MiniLineChartSeries.INVALID_FLOAT, MiniLineChartSeries.INVALID_FLOAT, MiniLineChartSeries.INVALID_FLOAT);

        //слушатель жестов
        gestureDetector = new GestureDetector(this.getContext(), new GestureDetector.OnGestureListener() {

            @Override
            public boolean onDown(MotionEvent e) {
                Log.d(TAG, "onDown");
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {
                Log.d(TAG, "onShowPress");

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                Log.d(TAG, "onSingleTapUp");
                if (IsPointOutMiniLineChartGridPos(e.getX(), e.getY())) return false;
                float x =  MiniLineChartXToGridX(e.getX());
                float y =  MiniLineChartYToGridY(e.getY());
                setZoomPoint(AZoom + 1, x, y);
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                Log.d(TAG, "onScroll");
                if (IsPointOutMiniLineChartGridPos(e1.getX(), e1.getY())) return false;
                float x =  MiniLineChartXToGridX(e1.getX());
                float y =  MiniLineChartYToGridY(e1.getY());
                setZoomPoint(AZoom, x, y);
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                Log.d(TAG, "onLongPress");
                if (IsPointOutMiniLineChartGridPos(e.getX(), e.getY())) return;
                float x =  MiniLineChartXToGridX(e.getX());
                float y =  MiniLineChartYToGridY(e.getY());
                setZoomPoint(1, x, y);
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                Log.d(TAG, "onFling");
                return false;
            }
        });


        Log.d(TAG, "Init Finish");
    }

    public MiniLineChart(Context context) {
        super(context);
        init();
    }

    public MiniLineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MiniLineChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MiniLineChart(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    //возращает отформатированный текст значения
    //returns the formatted text of the value
    private String FloatToGraph(float value) {
        return String.format(Locale.getDefault(), AAxisTextFormat, value);
    }

    //возващает коорд y точки на линии с координатой x
    //raises the y-coordinate of the point on the line with the x-coordinate
    private float getLineY(float x, float x1, float y1, float x2, float y2) {
        float k = (y2 - y1) / (x2 - x1);
        float b = y2 - k * x2;
        return k * x + b;
    }

    //возващает коорд x точки на линии с координатой y
    //raises the x-coordinate of the point on the line with the y-coordinate
    private float getLineX(float y, float x1, float y1, float x2, float y2) {
        float k = (y2 - y1) / (x2 - x1);
        float b = y2 - k * x2;
        return (y - b) / k;
    }

    //для перерисовки вызываем invalidate - ом
    //call invalidate to redraw
    @Override
    protected void onDraw(Canvas canvas) {

        MiniLineChartSeries Series;
        PointF point;
        float
                minX = MiniLineChartSeries.INVALID_FLOAT,
                maxX  = MiniLineChartSeries.INVALID_FLOAT,
                minY  = MiniLineChartSeries.INVALID_FLOAT,
                maxY = MiniLineChartSeries.INVALID_FLOAT;


        //ищем минимумы и максимумы
        //looking for lows and highs
        for (int i = 0; i < SeriesArr.size(); i++) {
            Series = SeriesArr.get(i);
            if ( (minX == MiniLineChartSeries.INVALID_FLOAT) || (Series.getMinX() < minX) ) minX = Series.getMinX();
            if ( (maxX == MiniLineChartSeries.INVALID_FLOAT) || (Series.getMaxX() > maxX) ) maxX = Series.getMaxX();
            if ( (minY == MiniLineChartSeries.INVALID_FLOAT) || (Series.getMinY() < minY) ) minY = Series.getMinY();
            if ( (maxY == MiniLineChartSeries.INVALID_FLOAT) || (Series.getMaxY() > maxY) ) maxY = Series.getMaxY();
        }

        //Log.d(MiniLineChartSeries.TAG, "minX:" + minX + " maxX:" + maxX + " minY:" + minY + " maxY:" + maxY);

        //закрашиваем все белым
        //paint everything white
        canvas.drawColor(Color.WHITE);


        if ( (minX == MiniLineChartSeries.INVALID_FLOAT) || (maxX == MiniLineChartSeries.INVALID_FLOAT) ||
                (minY == MiniLineChartSeries.INVALID_FLOAT) || (maxY == MiniLineChartSeries.INVALID_FLOAT) ) {
            //выходим если что-то не нашли
            //exit if something was not found
            return;
        }

        GraphMinMax.left = minX;
        GraphMinMax.right = maxX;
        GraphMinMax.top = minY;
        GraphMinMax.bottom = maxY;

        //заужаем по Y чтоб графики были не по границе
        //narrow down on Y so that the charts are not on the border
        float dY = (float) ((maxY - minY) / 10.0);
        minY -= dY;
        maxY += dY;

        //вычисляем края сетки в зависимости от zoom и точки зума
        //calculate the edges of the grid depending on the zoom and the zoom point
        if (AZoom == 1) {
            GridPos.left = minX;
            GridPos.right = maxX;
            GridPos.top = minY;
            GridPos.bottom = maxY;
        } else {
            GridPos.left = AZoomPoint.x - (maxX - minX) / AZoom;
            if (GridPos.left < minX) GridPos.left = minX;
            GridPos.right = GridPos.left + 2 * (maxX - minX) / AZoom;
            if (GridPos.right > maxX) GridPos.right = maxX;
            GridPos.top =  AZoomPoint.y - (maxY - minY) / AZoom;
            if (GridPos.top < minY) GridPos.top = minY;
            GridPos.bottom =  AZoomPoint.y + (maxY - minY) / AZoom;
            if (GridPos.bottom > maxY) GridPos.bottom = maxY;
            //Log.d(MiniLineChartSeries.TAG, "GridPos:" + GridPos);
        }

        //расчитываем максимальный размер строки значений по Y
        //calculate the maximum size of the string of values by Y
        String str;
        int maxYTextLength = 1;
        str = FloatToGraph(minY);
        if (str.length() > maxYTextLength) maxYTextLength = str.length();
        str = FloatToGraph(maxY);
        if (str.length() > maxYTextLength) maxYTextLength = str.length();

        //слева отступ + ширина текста
        //left indent + text width
        MiniLineChartGridPos.left = 10 + maxYTextLength * AxisTextPaint.getTextSize() / 2;
        //отступ 10
        //indent 10
        MiniLineChartGridPos.right = getWidth() - 10;
        //отступ 10
        //indent 10
        MiniLineChartGridPos.top = 10;
        //снизу отнимаем высоту текста
        //subtract the height of the text from the bottom
        MiniLineChartGridPos.bottom = getHeight() -  AxisTextPaint.getTextSize() * 2;

        GridWidth = GridPos.width();
        GridHeight = GridPos.height();

        MiniLineChartGridWidth = MiniLineChartGridPos.width();
        MiniLineChartGridHeight = MiniLineChartGridPos.height();

        float div, x, y, x2, y2;
        float px, py, px2, py2;


        //рисуем сетку
        //draw the grid
        for (int i = 0; i <= AGridCount; i++) {
            div =  (float)i / AGridCount;
            x = MiniLineChartGridPos.left + MiniLineChartGridWidth * div;
            y = MiniLineChartGridPos.top + MiniLineChartGridHeight * div;
            //вертикальные
            //vertical
            canvas.drawLine(x, MiniLineChartGridPos.top, x, MiniLineChartGridPos.bottom, AxisLinePaint);
            //горизонтальные
            //horizontal
            canvas.drawLine(MiniLineChartGridPos.left, y, MiniLineChartGridPos.right, y, AxisLinePaint);
            //текст знчений по X
            //text of X values
            str = FloatToGraph(GridPos.left + GridWidth * div);
            canvas.drawText(str, x - str.length() * AxisTextPaint.getTextSize() / 4, MiniLineChartGridPos.bottom + AxisTextPaint.getTextSize(), AxisTextPaint);
            if (i < AGridCount) {
                //текст знчений по Y
                //text of Y values
                str = FloatToGraph(GridPos.bottom - GridHeight * div);
                canvas.drawText(str, 0, y + AxisTextPaint.getTextSize() / 2, AxisTextPaint);
            }
        }

        //draw graphs
        //рисуем графики
        for (int i = 0; i < SeriesArr.size(); i++) {
            Series = SeriesArr.get(i);
            if (Series.getSize() == 0) continue;
            x = MiniLineChartSeries.INVALID_FLOAT;
            y =  MiniLineChartSeries.INVALID_FLOAT;
            for (int j = 0; j < Series.getSize(); j++) {
                point = Series.getPoint(j);
                if (point.x < GridPos.left) {
                    //не нужно рисовать
                    //no need to paint
                    continue;
                }
                //расчитываем реальные координата на MiniLineChartGridPos
                //calculate real coordinates on MiniLineChartGridPos
                x2 = MiniLineChartGridPos.left + MiniLineChartGridWidth * (point.x - GridPos.left)/GridWidth;
                y2 = MiniLineChartGridPos.top +  MiniLineChartGridHeight * (GridPos.bottom - point.y)/GridHeight;
                if ( (x != MiniLineChartSeries.INVALID_FLOAT) && (y !=  MiniLineChartSeries.INVALID_FLOAT)) {
                    //обе точки отрезка определены
                    //both points of the line segment are defined
                    px = x;
                    py = y;
                    px2 = x2;
                    py2 = y2;
                    //выходы отрезков са сетку
                    //the outputs of the segments to the grid
                    //выход справа
                    //exit right
                    if (x2 > MiniLineChartGridPos.right) {
                        py2 = getLineY(MiniLineChartGridPos.right, x, y, x2, y2);
                        px2 = MiniLineChartGridPos.right;
                    }
                    //выход сверху
                    //exit from the top
                    if (y < MiniLineChartGridPos.top) {
                        px = getLineX(MiniLineChartGridPos.top, x, y, x2, y2);
                        py = MiniLineChartGridPos.top;
                    }
                    if (y2 < MiniLineChartGridPos.top) {
                        px2 = getLineX(MiniLineChartGridPos.top, x, y, x2, y2);
                        py2 = MiniLineChartGridPos.top;
                    }
                    //выход снизу
                    //bottom exit
                    if (y > MiniLineChartGridPos.bottom) {
                        px = getLineX(MiniLineChartGridPos.bottom, x, y, x2, y2);
                        py = MiniLineChartGridPos.bottom;
                    }
                    if (y2 > MiniLineChartGridPos.bottom) { //выход снизу
                        px2 = getLineX(MiniLineChartGridPos.bottom, x, y, x2, y2);
                        py2 = MiniLineChartGridPos.bottom;
                    }

                    //рисуем график собственно
                    // draw the graph itself
                    canvas.drawLine(px, py, px2, py2, Series.getPaint());
                }
                x = x2;
                y = y2;

                if (point.x > GridPos.right) {
                    //нересуем ненужные точки справа
                    //don't cut unnecessary points to the right
                    break;
                }
            }
        }


    }

    //X из координат сетки в реальные
    //X from grid coordinates to real
    private float MiniLineChartXToGridX(float x) {
        return GridPos.left + GridWidth * (x - MiniLineChartGridPos.left) / MiniLineChartGridWidth;
    }

    //Y из координат сетки в реальные
    //Y from grid coordinates to real
    private float MiniLineChartYToGridY(float y) {
        return GridPos.top + GridHeight * (MiniLineChartGridPos.bottom - y) / MiniLineChartGridHeight;
    }

    //точка снаружи rectF?
    //point outside rectF?
    private boolean IsPontOutRectF(float x, float y, RectF rectF) {
        return ( (x < rectF.left) || (x > rectF.right) || (y < rectF.top) || (y > rectF.bottom) );
    }

    //точка снаружи MiniLineChartGridPos?
    //point outside MiniLineChartGridPos?
    private boolean IsPointOutMiniLineChartGridPos(float x, float y) {
        return IsPontOutRectF(x, y, MiniLineChartGridPos);
    }

    //нормализация float
    //float normalization
    private float norm(float min, float val, float max) {
        if ( (min == MiniLineChartSeries.INVALID_FLOAT) || (max == MiniLineChartSeries.INVALID_FLOAT) ) return val;
        if (val < min) return min;
        else return Math.min(val, max);
    }

    public void AddSeries(MiniLineChartSeries Series) { SeriesArr.add(Series);  }

    public int getSeriesSize() { return SeriesArr.size(); }

    public void RemoveSeries(int index) { SeriesArr.remove(index); }

    public void Clear() { SeriesArr.clear(); }

    public void setGridCount(int GridCount) { AGridCount = GridCount; }

    public void setAxisTextFormat(String AxisTextFormat) { AAxisTextFormat = AxisTextFormat; }

    public Paint getAxisLinePaint() { return  AxisLinePaint; }

    public Paint getAxisTextPaint() { return  AxisTextPaint; }

    public int getZoom() { return AZoom; }

    public void setZoom(int Zoom) {
        if ( (AZoomPoint.x == MiniLineChartSeries.INVALID_FLOAT) || (AZoomPoint.y == MiniLineChartSeries.INVALID_FLOAT) ) {
            AZoomPoint.x = (GraphMinMax.right -  GraphMinMax.left) / 2;
            AZoomPoint.y = (GraphMinMax.bottom -  GraphMinMax.top) / 2;
        }
        setZoomPoint(Zoom, AZoomPoint.x, AZoomPoint.y);
    }

    public void setZoomPoint(int Zoom, float x, float y) {
        AZoom = Math.min(Zoom, MaxZoom);
        AZoomPoint.x = norm(GraphMinMax.left, x, GraphMinMax.right);
        AZoomPoint.y = norm(GraphMinMax.top, y, GraphMinMax.bottom);
    }

    public RectF getGridPos() { return GridPos; }

    public RectF getMiniLineChartGridPos() { return MiniLineChartGridPos; }

    public RectF getGraphMinMax() { return GraphMinMax; }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }

}
