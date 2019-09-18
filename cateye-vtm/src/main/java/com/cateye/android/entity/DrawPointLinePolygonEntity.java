package com.cateye.android.entity;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.util.ArrayList;
import java.util.List;

@Table(name = "DrawPointLinePolygonEntity")
public class DrawPointLinePolygonEntity {
    @Column(name = "_id", isId = true)
    private String _id;
    @Column(name = "name")
    private String name;
    @Column(name = "remark")
    private String remark;
    @Column(name = "userName")
    private String userName;
    @Column(name = "projectId")
    private int projectId;
    @Column(name = "imgUrlListStr")
    private String imgUrlListStr;
    @Column(name = "isUpload")
    private boolean isUpload = false;
    @Column(name = "geometry")
    private String geometry;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public List<String> getImgUrlList() {
        List<String> imgArrayList = new ArrayList<>();
        if (getImgUrlListStr()!=null&&getImgUrlListStr().length()>0) {
            if (getImgUrlListStr().contains(";")){
                for (String imgUrl:getImgUrlListStr().split(";")) {
                    imgArrayList.add(imgUrl);
                }
            }else {
                imgArrayList.add(getImgUrlListStr());
            }
            return imgArrayList;
        }
        return imgArrayList;
    }

    public void setImgUrlList(List<String> imgUrlList) {
        if (imgUrlList!=null&&!imgUrlList.isEmpty()){
            StringBuilder sb=new StringBuilder("");
            for (String img: imgUrlList){
                sb.append(img);
                sb.append(";");
            }
            this.setImgUrlListStr(sb.toString().substring(0,sb.length()-1));
        }else {
            this.imgUrlListStr = null;
        }
        this.isUpload = false; // 用户修改照片数据，则数据被修改，将当前数据状态重置为未上传
    }

    public String getImgUrlListStr() {
        return imgUrlListStr;
    }

    protected void setImgUrlListStr(String imgUrlListStr) {
        this.imgUrlListStr = imgUrlListStr;
    }

    public boolean isUpload() {
        return isUpload;
    }

    public void setUpload(boolean upload) {
        isUpload = upload;
    }

    public String getGeometry() {
        return geometry;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }
}
