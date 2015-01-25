package com.android.carair.api;

public class Timer implements Comparable<Timer>{
    private int index;
    private String hour;
    private String min;
    private String repeat;
    private String title;
    
    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
    }
    public String getRepeat() {
        return repeat;
    }
    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }
    public String getHour() {
        return hour;
    }
    public void setHour(String hour) {
        this.hour = hour;
    }
    public String getMin() {
        return min;
    }
    public void setMin(String min) {
        this.min = min;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    @Override
    public int compareTo(Timer another) {
        if(this.index > another.index){
            return 1;
        }else if(this.index == another.index){
            return 0;
        }else{
            return -1;
        }
    }
}

