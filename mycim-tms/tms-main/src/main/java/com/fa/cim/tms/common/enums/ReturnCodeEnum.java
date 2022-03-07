package com.fa.cim.tms.common.enums;

/**
 * description:
 * This file use to define transaction ID by Enum
 * refer to: superpos/src/spxm/source/posxm/message/xmrc.h
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/3/20        ********            Miner        create file
 *
 * @author: Miner
 * @date: 2020/3/20 10:18
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public enum ReturnCodeEnum {
    RC_ERROR(2),
    RC_OK(0),

    RC_LOT_NOT_FOUND_MM(1450),
    RC_TCS_NO_RESPONSE(2104),
    RC_INVALID_LOT_PROCSTAT(933),
    RC_CANNOT_LOCK_OBJECT(324),
    RC_MTSC_UNKNOWN_JOBID(109),

    RC_LOCKED_BY_ANOTHER(4122),
    RC_INVALID_TYPE(4093),

    RC_A_XM(4000),
    //-----MCS ------------
    RC_MCS_NO_RESPONSE(4001),
    RC_MCS_SERVER_NOT_AVAILABLE(4002),
    RC_AMHS03_NO_RESPONSE(4003),
    RC_AMHS04_NO_RESPONSE(4004),
    RC_AMHS05_NO_RESPONSE(4005),
    RC_AMHS06_NO_RESPONSE(4006),
    RC_AMHS07_NO_RESPONSE(4007),
    RC_AMHS08_NO_RESPONSE(4008),
    RC_AMHS09_NO_RESPONSE(4009),
    RC_AMHS10_NO_RESPONSE(4010),
    RC_AMHS11_NO_RESPONSE(4011),
    RC_AMHS12_NO_RESPONSE(4012),
    RC_AMHS13_NO_RESPONSE(4013),
    RC_AMHS14_NO_RESPONSE(4014),
    RC_AMHS15_NO_RESPONSE(4015),
    RC_AMHS16_NO_RESPONSE(4016),


    RC_ABNORMAL_STOCK(4017),


    RC_D_XM(4040),
    RC_DEST_FULL(4041),

    RC_DEST_INTRA_NOT_AVAIL(4042),

    RC_DEST_NOT_AVAIL(4043),
    RC_DEST_ROUTE_NOT_DEF(4044),
    RC_DEST_STATION_NOT_AVAIL(4045),
    RC_DIFFERRENT_JOBID(4046),
    RC_DUPL_JOBID(4047),
    RC_DUPLICATE(4048),
    RC_DISPTCH_ERR(4049),


    RC_J_XM(4100),
    RC_JOB_NOT_FOUND(4101),

    RC_M_XM(4130),
    RC_MM_NO_RESPONSE(4131),
    RC_MISMATCH_TYPE(4132),
    RC_MM_SERVER_NOT_AVAILABLE(4133),
    RC_MISMATCH_INOUT_LENGTH(4134),
    RC_INVALID_STATUS_CHANGE(4135),
    RC_MM_CAST_TX_NO_SEND(4136),
    RC_MM_STAT_TX_NO_SEND(4137),


    RC_N_XM(4140),
    RC_NG(4141),
    RC_NOT_FOUND(4142),
    RC_NOT_FOUND_RETICLE(4143),
    RC_NOT_FOUND_STOCKER(4144),
    RC_NEED_SINGLE_DATA(4145),

    RC_P_XM(4160),
    RC_Q_XM(4170),
    RC_R_XM(4180),
    RC_RECORD_NOT_FOUND(4181),
    RC_RELAY_ST_NOT_AVAIL(4183),
    RC_ROUTE_NOT_AVAIL(4184),
    RC_REPORT_FAIL(4185),
    RC_S_XM(4190),
    RC_SYSTEM_ERROR(4191),
    RC_SOURCE_INTRA_NOT_AVAIL(4192),
    RC_SOURCE_NOT_AVAIL(4193),
    RC_SOURCE_STATION_NOT_AVAIL(4194),

    RC_T_XM(4200),
    RC_TX_TIMEOUT(4201),

    RC_U_XM(4210),
    RC_UNDEFINED_STOCKER_TYPE(4211),
    RC_UNKNOWN_CARRIER(4212),
    RC_UNKNOWN_DEST(4213),
    RC_UNKNOWN_JOBID(4214),
    RC_UNKNOWN_RETURN_CODE(4215),


    RC_V_XM(4220),

    RC_W_XM(4230),

    RC_X_XM(4240),

    RC_Y_XM(4250),

    RC_Z_XM(4260),

    RC_NOT_FOUND_PERSON(1466),//"not found person"

    /* The following was generated from xmmsg.src. */
    MSG_RECORD_NOT_FOUND(162),
    ;


    /* The following was generated from xmmsg.src. */

    private int value;

    ReturnCodeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }


}


