package com.cateye.android.entity;

import java.io.Serializable;

/**
 * Created by xiaoxiao on 2018/12/3.
 */

public class AirPlanCRS implements Serializable {
    private String type = "name";
    private AirPlanCRSProperties properties = new AirPlanCRSProperties();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public AirPlanCRSProperties getProperties() {
        return properties;
    }

    public void setProperties(AirPlanCRSProperties properties) {
        this.properties = properties;
    }

    protected class AirPlanCRSProperties implements Serializable {
        private String name = "urn:ogc:def:crs:OGC:1.3:CRS84";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
