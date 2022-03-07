package com.fa.cim.tms.entity;

import com.fa.cim.common.annotation.IdPrefix;
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
 * @date: 2020/10/22 17:18
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "OTXFERREQ")
@IdPrefix(value = "OTXFERREQ")
@Data
public class FxtrnreqEntity extends NonRuntimeEntity {

    private static final long serialVersionUID = 5885308522431584097L;

    @Column(name = "CARRIER_ID", length = 64)
    private String carrierID;

    @Column(name = "JOB_ID", length = 64)
    private String jobID;

    @Column(name = "CARRIER_JOB_ID", length = 64)
    private String carrierJobID;

    @Column(name = "TRANSPORT_TYPE", length = 64)
    private String transportType;

    @Column(name = "ZONE_TYPE", length = 12)
    private String zoneType;

    @Column(name = "N2PURGE_FLAG", length = 10)
    private Integer n2PurgeFlag;

    @Column(name = "ORIG_MACHINE_ID", length = 64)
    private String fromMachineID;

    @Column(name = "ORIG_PORT_ID", length = 64)
    private String fromPortID;

    @Column(name = "TO_STOCKER_GROUP", length = 64)
    private String toStockerGroup;

    @Column(name = "DEST_MACHINE_ID", length = 64)
    private String toMachineID;

    @Column(name = "DEST_PORT_ID", length = 64)
    private String toPortID;

    @Column(name = "EXP_START_TIME", length = 14)
    private String expectedStrtTime;

    @Column(name = "EXP_END_TIME", length = 14)
    private String expectedEndTime;

    @Column(name = "EST_START_TIME", length = 14)
    private String estimateStrtTime;

    @Column(name = "EST_END_TIME", length = 14)
    private String estimateEndTime;

    @Column(name = "MANDATORY_FLAG", length = 10)
    private Integer mandatoryFlag;

    @Column(name = "PRIORITY", length = 4)
    private String priority;

    @Column(name = "JOB_STATUS", length = 4)
    private String jobStatus;

    @Column(name = "CARRIER_JOB_STATUS", length = 4)
    private String carrierJobStatus;

    @Column(name = "REPORT_time")
    private Timestamp timestamp;
}
