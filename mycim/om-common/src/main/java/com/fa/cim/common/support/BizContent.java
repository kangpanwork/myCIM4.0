package com.fa.cim.common.support;

import lombok.Data;

import java.util.Objects;

@Data
public class BizContent {

    private Class<?> boType;
    private String primaryKey;

    public BizContent(Class<?> boType, String primaryKey) {
        this.boType = boType;
        this.primaryKey = primaryKey;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        BizContent that = (BizContent) object;
        return primaryKey.equals(that.primaryKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(primaryKey);
    }
}
