package com.tale.kits;

import com.blade.reflectasm.MethodAccess;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static com.tale.kits.BladeKit.methodToFieldName;

/**
 * @ClassName BladeCache
 * @Desc TODO
 * @Author zhaoteng
 * @Date 2020/4/11 18:20
 * @Version 1.0
 **/
public class BladeCache {

    private static final Map<SerializedLambda, String> CACHE_LAMBDA_NAME       = new HashMap<>(8);
    private static final Map<Class, MethodAccess>      CLASS_METHOD_ACCESS_MAP = new HashMap<>(8);
    private static final Map<String, String>           PADDING_METHOD_STR      = new HashMap<>(6);

    static {
        PADDING_METHOD_STR.put("GET", StringKit.padRight("GET", 6));
        PADDING_METHOD_STR.put("POST", StringKit.padRight("POST", 6));
        PADDING_METHOD_STR.put("DELETE", StringKit.padRight("DELETE", 6));
        PADDING_METHOD_STR.put("PUT", StringKit.padRight("PUT", 6));
        PADDING_METHOD_STR.put("OPTIONS", StringKit.padRight("OPTIONS", 6));
        PADDING_METHOD_STR.put("HEAD", StringKit.padRight("HEAD", 6));
    }

    public static String getPaddingMethod(String method) {
        return PADDING_METHOD_STR.get(method);
    }

    public static final MethodAccess getMethodAccess(Class clazz) {
        return CLASS_METHOD_ACCESS_MAP.computeIfAbsent(clazz, MethodAccess::get);
    }

    public static String getLambdaFieldName(SerializedLambda serializedLambda) {
        String name = CACHE_LAMBDA_NAME.get(serializedLambda);
        if (null != name) {
            return name;
        }
        String className  = serializedLambda.getImplClass().replace("/", ".");
        String methodName = serializedLambda.getImplMethodName();
        String fieldName  = methodToFieldName(methodName);
        try {
            Field field = Class.forName(className).getDeclaredField(fieldName);
            name = field.getName();
            CACHE_LAMBDA_NAME.put(serializedLambda, name);
            return name;
        } catch (NoSuchFieldException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
