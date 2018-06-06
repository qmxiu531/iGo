package com.gionee.autotest.traversal.common.event;

import java.io.Serializable;

/**
 * Created by viking on 8/31/17.
 *
 * model for record current operation
 */

public class EventInfo implements Serializable{

    public int eventCounter ;

    public String eventType ;

    public String itemInfo ;

    public EventInfo(String eventType, String itemInfo){
        this.eventType = eventType ;
/*        // shorten the standard class names, otherwise it takes up too much space on UI
        String shortInfo = itemInfo.replace("android.widget.", "");
        shortInfo = shortInfo.replace("android.view.", "");*/
        this.itemInfo = itemInfo ;
    }

    public int getEventCounter() {
        return eventCounter;
    }

    public void setEventCounter(int eventCounter) {
        this.eventCounter = eventCounter;
    }
}
