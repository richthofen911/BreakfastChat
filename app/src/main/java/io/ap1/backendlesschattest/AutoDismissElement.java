package io.ap1.backendlesschattest;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Yichao Li on 16/03/16.
 * !!! Context must loop the containerArrayList and cancel all AutoDismissElements' timers when onDestroy()
 */
public class AutoDismissElement {

    private Timer timer;
    private ArrayList<AutoDismissElement> containerArrayList;
    private int autoDismissTime;

    public AutoDismissElement(int autoDismissTime, final ArrayList<AutoDismissElement> containerArrayList){
        this.containerArrayList = containerArrayList;
        this.autoDismissTime = autoDismissTime;
        timer = new Timer();
    }

    public void countdown(){
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                containerArrayList.remove(AutoDismissElement.this);
                timer.cancel();
            }
        }, autoDismissTime);
    }

    public void cancelTimer(){
        timer.cancel();
    }
}
