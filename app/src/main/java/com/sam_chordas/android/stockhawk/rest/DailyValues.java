package com.sam_chordas.android.stockhawk.rest;

/**
 * Created by Mauricio on 20-Jul-16.
 */
public class DailyValues {

    private String date;
    private float open;
    private float high;
    private float low;
    private float close;
    private long volume;
    private float adjClose;

    public float getAdjClose() {
        return adjClose;
    }

    public void setAdjClose(String adjClose) {
        this.adjClose = Float.parseFloat(adjClose);
    }

    public float getClose() {
        return close;
    }

    public void setClose(String close) {
        this.close = Float.parseFloat(close);
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public float getHigh() {
        return high;
    }

    public void setHigh(String high) {
        this.high = Float.parseFloat(high);
    }

    public float getLow() {
        return low;
    }

    public void setLow(String low) {
        this.low = Float.parseFloat(low);
    }

    public float getOpen() {
        return open;
    }

    public void setOpen(String open) {
        this.open = Float.parseFloat(open);
    }

    public long getVolume() {
        return volume;
    }

    public void setVolume(long volume) {
        this.volume = volume;
    }
}
