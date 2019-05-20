package com.cateye.android.entity;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 用户轨迹的条数记录，记录轨迹的开始、结束时间
 * */
@Table(name = "TravelRecord")
public class TravelRecord {
    @Column(name = "_id",autoGen = true,isId = true)
    private int id;
    @Column(name = "travelName")
    private String travelName;//轨迹记录的名称
    @Column(name = "sTime")
    private String sTime;//轨迹记录的开始时间
    @Column(name = "eTime")
    private String eTime;//轨迹记录的结束时间
    @Column(name = "description")
    private String description;//轨迹记录的描述信息

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTravelName() {
        return travelName;
    }

    public void setTravelName(String travelName) {
        this.travelName = travelName;
    }

    public String getsTime() {
        return sTime;
    }

    public void setsTime(String sTime) {
        this.sTime = sTime;
    }

    public String geteTime() {
        return eTime;
    }

    public void seteTime(String eTime) {
        this.eTime = eTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
