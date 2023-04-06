package com.zgg.sql.bean;

import java.io.Serializable;
import java.util.HashMap;

public class BufferZone implements Serializable {
    private Long total;
    private HashMap<String,Long> cityMap;

    public BufferZone() {}

    public BufferZone(Long total, HashMap<String, Long> cityMap) {
        this.total = total;
        this.cityMap = cityMap;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public HashMap<String, Long> getCityMap() {
        return cityMap;
    }

    public void setCityMap(HashMap<String, Long> cityMap) {
        this.cityMap = cityMap;
    }
}
