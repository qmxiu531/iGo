package com.gionee.autotest.traversal.common.report;

import com.gionee.autotest.traversal.common.event.EventInfo;
import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.List;

/**
 * Created by viking on 9/1/17.
 *
 * base information for exceptions
 */

public class ExceptionInfo implements Serializable{

    public final boolean isCrash ;

    @Expose(serialize = false)
    public int index ;

    public int rowspan = 7 ;

    public final String timestamp ;

    public final int eventCounter ;

    public final String errorType ;

    public final String processName ;

    public final String pid ;

    public final String shortMsg ;

    public final String longMsg ;

    public String traces ;

    public final boolean hasLastEvent ;

    public final List<EventInfo> lastEventInfos ;

    public ExceptionInfo(String timestamp, int eventCounter, String errorType, String processName, String pid, String shortMsg, String longMsg, String traces, List<EventInfo> lastEventInfo) {
        this.eventCounter = eventCounter;
        this.timestamp = timestamp ;
        this.errorType = errorType;
        this.isCrash    = errorType != null && "CRASH".equals(errorType) ;
        if (!this.isCrash){
            this.rowspan = 6 ;
        }
        this.processName = processName;
        this.pid = pid;
        this.shortMsg = shortMsg;
        this.longMsg = longMsg;
        this.traces = traces;
        this.lastEventInfos = lastEventInfo;
        this.hasLastEvent = lastEventInfos != null && !lastEventInfos.isEmpty();
    }

    public ExceptionInfo(String timestamp, int eventCounter, String errorType, String processName, String pid, String traces, List<EventInfo> lastEventInfo) {
        this(timestamp, eventCounter, errorType, processName, pid, null, null, traces, lastEventInfo) ;
    }
}
