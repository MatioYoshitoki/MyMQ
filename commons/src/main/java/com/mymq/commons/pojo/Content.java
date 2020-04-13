package com.mymq.commons.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Content implements Serializable {

    private String tag;
    private String topic;
    private String msg;
    private int type;
    private int msgType;
    private long msgID;

    public Content(String tag, String topic, String msg){
        this.tag = tag;
        this.topic = topic;
        this.msg = msg;
    }

    public Content(int type, int msgType){
        this.type = type;
        this.msgType = msgType;
    }

}
