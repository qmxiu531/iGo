package com.gionee.autotest.traversal.common.report;

import com.gionee.autotest.traversal.common.util.Util;

import java.io.Serializable;

/**
 * Created by viking on 8/31/17.
 *
 * class for hold all device information
 */

public class DeviceInfo implements Serializable{

    public final String modelName ;

    //ro.build.display.id
    public final String sysVersion ;

    public final String androidVersion ;

    //ro.gn.gnznvernumber
    public final String gnVersionNum ;

    public DeviceInfo(String modelName, String androidVersion) {
        this.modelName = modelName;
        this.androidVersion = androidVersion;
        this.sysVersion = Util.getProperty("ro.build.display.id", "N/A") ;
        this.gnVersionNum = Util.getProperty("ro.gn.gnznvernumber", "N/A") ;
    }
}
