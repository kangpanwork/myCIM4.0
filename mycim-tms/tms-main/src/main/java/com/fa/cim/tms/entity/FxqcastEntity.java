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
 * @date: 2020/10/22 17:16
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "OTQCARRIER")
@IdPrefix(value = "OTQCARRIER")
@Data
public class FxqcastEntity extends NonRuntimeEntity {
    private static final long serialVersionUID = 8861978582298128531L;

    @Column(name = "CREATE_TIME")
    private Timestamp timestamp;

    @Column(name = "CARRIER_ID")
    private String carrierID;

    @Column(name = "MCS_JOB_ID")
    private String jobID;

    @Column(name = "CARRIER_JOB_ID")
    private String carrierJobID;

    @Column(name = "EVENT_TYPE")
    private String eventType;

    @Column(name = "EVENT_STATUS")
    private String eventStatus;

    @Column(name = "MACHINE_ID")
    private String machineID;

    @Column(name = "PORT_ID")
    private String portID;

    @Column(name = "TRANSFER_STATUS")
    private String transferStatus;

}
