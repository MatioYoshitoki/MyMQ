package com.mymq.commons.util;

import cn.hutool.core.lang.Snowflake;

public class SnowFlakeUtil {
    static Snowflake snowflake = new Snowflake(0, 1);

    public static long next(){
        return snowflake.nextId();
    }

}
