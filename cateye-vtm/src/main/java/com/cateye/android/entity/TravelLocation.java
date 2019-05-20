package com.cateye.android.entity;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;
/**
 * 轨迹记录表，用户采集的轨迹记录在当前表中
 * */
@Table(name = "travel")
public class TravelLocation {
    @Column(name = "_id", isId = true, autoGen = true)
    private long id;
    @Column(name = "locationTime")
    private String locationTime;//当前点采集的时间点，精确到秒
    @Column(name = "geometry")
    private String geometry;//点位位置

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLocationTime() {
        return locationTime;
    }

    public void setLocationTime(String locationTime) {
        this.locationTime = locationTime;
    }

    public String getGeometry() {
        return geometry;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }
}
