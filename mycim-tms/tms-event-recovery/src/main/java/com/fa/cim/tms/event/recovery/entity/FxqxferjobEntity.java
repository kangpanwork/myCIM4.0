package com.fa.cim.tms.event.recovery.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * description:
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/10/22                               Neyo                create file
 *
 * @author: Neyo
 * @date: 2020/10/22 17:17
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "OTQXFEREVENT")
@Data
public class FxqxferjobEntity extends NonRuntimeEntity {

    private static final long serialVersionUID = 2834625062393545858L;

    @Column(name = "OPE_CATEGORY")
    private String opeCategory;

    @Column(name = "CARRIER_ID")
    private String carrierID;

    @Column(name = "JOB_ID")
    private String jobID;

    @Column(name = "CARRIER_JOB_ID")
    private String carrierJobID;

    @Column(name = "TRANSPORT_TYPE")
    private String transportType;

    @Column(name = "ZONE_TYPE")
    private String zoneType;

    @Column(name = "N2PURGE_FLAG", length = 10)
    private Integer n2PurgeFlag;

    @Column(name = "ORIG_MACHINE_ID")
    private String fromMachineID;

    @Column(name = "ORIG_PORT_ID")
    private String fromPortID;

    @Column(name = "TO_STOCKER_GROUP")
    private String toStockerGroup;

    @Column(name = "DEST_MACHINE_ID")
    private String toMachineID;

    @Column(name = "DEST_PORT_ID")
    private String toPortID;

    @Column(name = "EXT_START_TIME")
    private String expectedStrtTime;

    @Column(name = "EXT_END_TIME")
    private String expectedEndTime;

    @Column(name = "EST_START_TIME")
    private String estimateStrtTime;

    @Column(name = "EST_END_TIME")
    private String estimateEndTime;

    @Column(name = "MANDATORY_FLAG", length = 10)
    private Integer mandatoryFlag;

    @Column(name = "PRIORITY")
    private String priority;

    @Column(name = "JOB_STATUS")
    private String jobStatus;

    @Column(name = "CARRIER_JOB_STATUS")
    private String carrierJobStatus;

    @Column(name = "REPORT_TIME")
    private Timestamp timestamp;

    @Column(name = "TRX_USER_ID")
    private String claimUserID;
}
