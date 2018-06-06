package com.gionee.autotest.traversal.common.model;

/**
 * Created by viking on 8/29/17.
 *
 * Activity Screen to record visit information
 */

public class ActivityScreen {

    private AInfo aInfo ;

    private int visitedCount ;

    private boolean isVisited ;

    public ActivityScreen(AInfo activityInfo) {
        this.aInfo = activityInfo;
    }

    public int getVisitedCount(){
        return visitedCount ;
    }

    public void plusVisitedCount(){
        visitedCount++ ;
    }

    public boolean isVisited(){
        return isVisited ;
    }

    public void setVisited(boolean isVisited){
        this.isVisited = isVisited ;
    }

    /**
     * Fetch ActivityInfo
     * @return return current ActivityInfo
     */
    public AInfo getActivityInfo(){
        return aInfo ;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ActivityScreen screen = (ActivityScreen) o;

        return aInfo.equals(screen.getActivityInfo());
    }

    @Override
    public int hashCode() {
        return aInfo.hashCode();
    }

    @Override
    public String toString() {
        return "AScreen{" + "activityInfo=" + aInfo + '}';
    }
}
