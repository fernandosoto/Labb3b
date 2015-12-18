package Listeners;

import android.view.View;
import android.widget.TextView;

import Model.Reader;

/**
 * Created by Fernando on 2015-12-18.
 */
public class StopButtonListener implements View.OnClickListener{

    private TextView pulseView;
    private Reader reader;
    private Thread thread;
    public StopButtonListener(TextView pulseView, Reader reader, Thread thread)
    {
        this.pulseView = pulseView;
        this.reader = reader;
        this.thread = thread;

    }
    @Override
    public void onClick(View v) {
        pulseView.setText("");

        if(reader!=null)
            reader.setRunning(false);
        try {
            if(thread != null)
                thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}