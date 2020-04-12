package com.tale.kits.json;

import java.lang.reflect.Type;

public interface JsonSupport {

    String toString(Object data);

    <T> T formJson(String json, Type cls);

}
