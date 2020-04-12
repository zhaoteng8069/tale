package com.tale.kits;

import com.tale.kits.json.Ason;
import com.tale.kits.json.DefaultJsonSupport;
import com.tale.kits.json.JsonSupport;

import java.lang.reflect.Type;

/**
 * @ClassName JsonKit
 * @Desc TODO
 * @Author zhaoteng
 * @Date 2020/4/12 8:40
 * @Version 1.0
 **/
public class JsonKit {

    private static final DefaultJsonSupport defaultJsonSupport = new DefaultJsonSupport();

    private static JsonSupport jsonSupport = new DefaultJsonSupport();

    public static void jsonSupprt(JsonSupport jsonSupport) {
        JsonKit.jsonSupport = jsonSupport;
    }

    public static String toString(Object object) {
        return jsonSupport.toString(object);
    }

    public static <T> T formJson(String json, Type type) {
        return jsonSupport.formJson(json, type);
    }

    public static Ason<?, ?> toAson(String value) {
        return defaultJsonSupport.toAson(value);
    }
}
