package com.example.myapplication;

import android.hardware.Sensor;

public class SensorEntity {
    private Sensor sensor;
    private int index;
    private boolean selectable;

    public SensorEntity(Sensor sensor, int index, boolean selectable) {
        this.sensor = sensor;
        this.index = index;
        this.selectable = selectable;
    }

    //Getters and Setters
    public Sensor getSensor() {
        return sensor;
    }
    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

}
