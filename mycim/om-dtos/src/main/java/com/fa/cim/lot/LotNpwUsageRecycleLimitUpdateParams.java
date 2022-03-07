package com.fa.cim.lot;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.User;
import lombok.Data;

@Data
public class LotNpwUsageRecycleLimitUpdateParams {

    private User user;

    private ObjectIdentifier lotID;

    private int usageCountLimit;

    private int recycleCountLimit;

}
