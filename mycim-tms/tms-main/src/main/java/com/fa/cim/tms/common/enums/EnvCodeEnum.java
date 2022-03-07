package com.fa.cim.tms.common.enums;

/**
 * description:
 * <p>
 * 配置值参考solite项目
 * This file use to define transaction ID by Enum
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/3/20        ********            Miner         create file
 *
 * @author: Miner
 * @date: 2018/3/20 10:18
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public enum EnvCodeEnum {

    /****TransportJobStatusReport***/
    OM_TM03_STATUS_FLAG("OM_TM03_STATUS_FLAG"),
    OM_TM03_ERROR_OUT("OM_TM03_ERROR_OUT"),
    OM_TM03_MO_REPORT_FLAG("OM_TM03_MO_REPORT_FLAG"),
    OM_GET_CARRIER_FLAG("OM_GET_CARRIER_FLAG"),
    OM_REROUTE_EQP_XER("OM_REROUTE_EQP_XER"),
    OM_QUE_SWITCH("OM_QUE_SWITCH"),
    OM_TM06_ERROR_OUT("OM_TM06_ERROR_OUT"),
    OM_TM06_ERROR_RETURN("OM_TM06_ERROR_RETURN"),
    OM_RETICLE_LOGIC_AVAILABLE("OM_RETICLE_LOGIC_AVAILABLE"),
    OM_TM06_SEND_FROM_PORTID("OM_TM06_SEND_FROM_PORTID"),
    OM_DELIVERY_REQ_ON("OM_DELIVERY_REQ_ON"),

    //Specify ON or OFF. If this is “ON”, TMS will not fill in “FromMachineID” and
    // “FromPortID” field of Transport Job Create Request(S1) message when current
    // carrier location is stocker. Otherwise, TMS may fill in the field. Default is OFF.
    OM_OM01_NO_FROM_MACHINEID_FOR_STK("OM_OM01_NO_FROM_MACHINEID_FOR_STK"),


    //删除类型CJ 为carrirJob
    OM_TM06_JOB_DELETE("OM_TM06_JOB_DELETE"),
    OM_RETICLE_POD_ONLY("OM_RETICLE_POD_ONLY"),
    OM_QUE_UPDATE_BY_EVENT("OM_QUE_UPDATE_BY_EVENT"),
    OM_FORCE_DELETE_RC("OM_FORCE_DELETE_RC"),
    OM_IGNORE_DELETE_RC("OM_IGNORE_DELETE_RC"),
    OM_INVENTORY_FLAG("OM_INVENTORY_FLAG"),
    OM_QUE_UPDATE("OM_QUE_UPDATE"),
    OM_TM02_MO_REPORT_FLAG("OM_TM02_MO_REPORT_FLAG"),
    OM_TM02_MO_REPORT_CARRIER_STATUS("OM_TM02_MO_REPORT_CARRIER_STATUS"),
    OM_INVENTORY_TM_FLAG("OM_INVENTORY_TM_FLAG"),
    OM_INVENTORY_RTM_FLAG("OM_INVENTORY_RTM_FLAG"),
    //add
    OM_XFERJOB_STATUS_RPT_FLAG("OM_XFERJOB_STATUS_RPT_FLAG"),
    OM_QUE_XFERJOB_SWITCH("OM_QUE_XFERJOB_SWITCH"),
    OM_TM05_FORCE_KEEP("OM_TM05_FORCE_KEEP"),

    //RTMS add
    OM_MO_RESOURCE_TYPE("OM_MO_RESOURCE_TYPE"),
    OM_ABNORMALSTOCK_MANUALPORTID("OM_ABNORMALSTOCK_MANUALPORTID");

    private String value;

    EnvCodeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}


