package com.cateye.android.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xiaoxiao on 2018/12/3.
 */

public class AirPlanEntity implements Serializable {
    private String type = "FeatureCollection";
    private String name;
    private AirPlanCRS crs = new AirPlanCRS();
    private List<AirPlanFeature> features;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AirPlanCRS getCrs() {
        return crs;
    }

    public void setCrs(AirPlanCRS crs) {
        this.crs = crs;
    }

    public List<AirPlanFeature> getFeatures() {
        return features;
    }

    public void setFeatures(List<AirPlanFeature> features) {
        this.features = features;
    }
}
