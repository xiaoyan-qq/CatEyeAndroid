package com.cateye.android.entity;

import java.util.Map;

/**
 * 上传用户数据对应的实体类，目前用来将用户绘制信息和轨迹信息转换为当前类
 * */
public class UploadRecordEntity {
    private String uuid;
    private int projectId;
    private String name;
    private Map prop;
    private String wkt;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map getProp() {
        return prop;
    }

    public void setProp(Map prop) {
        this.prop = prop;
    }

    public String getWkt() {
        return wkt;
    }

    public void setWkt(String wkt) {
        this.wkt = wkt;
    }
}
