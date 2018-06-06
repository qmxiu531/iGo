package com.gionee.autotest.traversal.common.report;

import java.io.Serializable;

/**
 * Created by viking on 8/31/17.
 *
 * hold all test configurations
 */

public class ConfigInfo implements Serializable{

    public String sMaxSteps ;

    public String sMaxRuntime ;

    public String sMaxRecycleTimes ;

    public String sLogMode ;

    public String sMaxEventCycle ;

    public String sMaxRestartAppTimes ;

    public String sCurTimeStamp ;

    public String sTargetPackage ;

    public String sRuningCounter ;

    public String sRuningTime ;

}
