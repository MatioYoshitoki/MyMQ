package com.mymq.commons.util;

import cn.hutool.core.util.StrUtil;
import com.mymq.commons.pojo.Content;
import com.mymq.commons.protobuf.MyContentModule;

public class MessageConvertUtil {

    public static Content toRead(MyContentModule.Content sendContent){
        return new Content(sendContent.getTag(), sendContent.getTopic(), sendContent.getMsg(), sendContent.getType(), sendContent.getMsgType(), sendContent.getMsgID());
    }

    public static MyContentModule.Content toSend(Content readContent){
        MyContentModule.Content.Builder builder = MyContentModule.Content.newBuilder();
        builder.setMsgID(readContent.getMsgID()).setType(readContent.getType()).setMsgType(readContent.getMsgType());
        if (!StrUtil.isEmpty(readContent.getMsg())){
            builder.setMsg(readContent.getMsg());
        }
        if (!StrUtil.isEmpty(readContent.getTag())){
            builder.setTag(readContent.getTag());
        }
        if (!StrUtil.isEmpty(readContent.getTopic())){
            builder.setTopic(readContent.getTopic());
        }
        return builder.build();
    }

}
