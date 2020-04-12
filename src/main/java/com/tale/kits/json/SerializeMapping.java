package com.tale.kits.json;

import lombok.Builder;
import lombok.Data;

/**
 * @ClassName SerializeMapping
 * @Desc TODO
 * @Author zhaoteng
 * @Date 2020/4/12 8:42
 * @Version 1.0
 **/
@Data
@Builder
public class SerializeMapping {
    @Builder.Default
    private String datePatten     = "yyyy-MM-dd";
    @Builder.Default
    private int    bigDecimalKeep = 2;

    private static final SerializeMapping instance = SerializeMapping.builder().build();

    public static SerializeMapping defaultMapping() {
        return instance;
    }
}
