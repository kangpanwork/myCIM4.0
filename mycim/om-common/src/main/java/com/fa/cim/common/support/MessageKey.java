package com.fa.cim.common.support;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@EqualsAndHashCode
@Data
@NoArgsConstructor
public class MessageKey {

    private Long timestamp;
    private UUID uuid;
    private String transactionID;

    @Override
    public String toString () {
        return JSON.toJSONString(this);
    }

}
