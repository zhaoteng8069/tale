package com.tale.kits.json;

import com.tale.kits.StringKit;

import java.util.LinkedHashMap;

/**
 * @ClassName Ason
 * @Desc TODO
 * @Author zhaoteng
 * @Date 2020/4/12 8:44
 * @Version 1.0
 **/
public class Ason<K, V> extends LinkedHashMap<K, V> {

    public Ason() {
    }

    public Ason(int size) {
        super(size);
    }

    public String getString(String key) {
        return this.get(key).toString();
    }

    public Integer getInt(String key) {
        String val = getString(key);
        if (StringKit.isNotBlank(val)) {
            return Integer.parseInt(val);
        }
        return null;
    }

    public Long getLong(String key) {
        String val = getString(key);
        if (StringKit.isNotBlank(val)) {
            return Long.parseLong(val);
        }
        return null;
    }

    public Boolean getBoolean(String key) {
        String val = getString(key);
        if (StringKit.isNotBlank(val)) {
            return Boolean.parseBoolean(val);
        }
        return Boolean.FALSE;
    }

    public Double getDouble(String key) {
        String val = getString(key);
        if (StringKit.isNotBlank(val)) {
            return Double.parseDouble(val);
        }
        return null;
    }

    public Float getFloat(String key) {
        String val = getString(key);
        if (StringKit.isNotBlank(val)) {
            return Float.parseFloat(val);
        }
        return null;
    }

    public Short getShort(String key) {
        String val = getString(key);
        if (StringKit.isNotBlank(val)) {
            return Short.parseShort(val);
        }
        return null;
    }

    @Override
    public String toString() {
        return SampleJsonSerializer.serialize(this);
    }

}
