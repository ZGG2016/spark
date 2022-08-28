package com.zgg.core.练习.需求1;

import java.io.Serializable;

public class HotCategory implements Serializable {
    private String categoryId;
    private int clickCount;
    private int orderCount;
    private int payCount;

    public HotCategory() {
    }

    public HotCategory(String categoryId, int clickCount, int orderCount, int payCount) {
        this.categoryId = categoryId;
        this.clickCount = clickCount;
        this.orderCount = orderCount;
        this.payCount = payCount;
    }

    public String getCategoryId() {
        return this.categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public int getClickCount() {
        return this.clickCount;
    }

    public void setClickCount(int clickCount) {
        this.clickCount = clickCount;
    }

    public int getOrderCount() {
        return this.orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public int getPayCount() {
        return this.payCount;
    }

    public void setPayCount(int payCount) {
        this.payCount = payCount;
    }

    @Override
    public String toString() {
        return this.categoryId + " : " +
                this.clickCount + '-' +
                this.orderCount + '-' +
                this.payCount;
    }
}
