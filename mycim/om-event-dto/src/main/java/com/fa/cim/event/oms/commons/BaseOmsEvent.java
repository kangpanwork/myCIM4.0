package com.fa.cim.event.oms.commons;

import com.fa.cim.event.oms.BaseOmsEventData;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public abstract class BaseOmsEvent extends BaseOmsEventData {

    private static final long serialVersionUID = -698887609438897365L;

    private List<CustomerDefiniedData> customerDefiniedData;

}
