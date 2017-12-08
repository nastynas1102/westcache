package com.github.bingoohuang.westcache.flusher;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/28.
 */
public class WestCacheFlusherBean {
    private String cacheKey;
    private String keyMatch;
    private int valueVersion;
    private String valueType;
    private String specs;

    public WestCacheFlusherBean() {
    }

    public WestCacheFlusherBean(String cacheKey, String keyMatch, int valueVersion, String valueType, String specs) {
        this.cacheKey = cacheKey;
        this.keyMatch = keyMatch;
        this.valueVersion = valueVersion;
        this.valueType = valueType;
        this.specs = specs;
    }

    @Override
    public String toString() {
        return "WestCacheFlusherBean{" +
                "cacheKey='" + cacheKey + '\'' +
                ", keyMatch='" + keyMatch + '\'' +
                ", valueVersion=" + valueVersion +
                ", valueType='" + valueType + '\'' +
                ", specs='" + specs + '\'' +
                '}';
    }

    public String getCacheKey() {
        return cacheKey;
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    public String getKeyMatch() {
        return keyMatch;
    }

    public void setKeyMatch(String keyMatch) {
        this.keyMatch = keyMatch;
    }

    public int getValueVersion() {
        return valueVersion;
    }

    public void setValueVersion(int valueVersion) {
        this.valueVersion = valueVersion;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public String getSpecs() {
        return specs;
    }

    public void setSpecs(String specs) {
        this.specs = specs;
    }
}
