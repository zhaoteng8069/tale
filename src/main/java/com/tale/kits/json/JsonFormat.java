package com.tale.kits.json;

/**
 * @ClassName JsonFormat
 * @Desc TODO
 * @Author zhaoteng
 * @Date 2020/4/12 8:47
 * @Version 1.0
 **/
public @interface JsonFormat {

    String value();

    MappingType type() default MappingType.DATE_PATTEN;

}
