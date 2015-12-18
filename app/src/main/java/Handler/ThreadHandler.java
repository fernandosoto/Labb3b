package Handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.TextView;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

/**
 * Created by Fernando on 2015-12-18.
 */
public class ThreadHandler {

    private Handler pHandler;
    private LineGraphSeries<DataPoint> lineGraphSeries;
    private TextView pulseView;

    public static void pulseHandler(LineGraphSeries<DataPoint> lineGraph, TextView view)
    {


    }
}
