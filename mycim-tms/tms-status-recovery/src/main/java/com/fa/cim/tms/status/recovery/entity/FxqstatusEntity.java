package com.fa.cim.tms.status.recovery.entity;

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
 * @date: 2020/10/22 17:20
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "OTQSTOCKERST")
@Data
public class FxqstatusEntity extends NonRuntimeEntity {
    private static final long serialVersionUID = 6096544409092215085L;

    @Column(name = "CREATE_TIME")
    private Timestamp timestamp;

    @Column(name = "STOCKER_ID")
    private String stockerID;

    @Column(name = "STOCKER_STATUS")
    private String stockerStatus;

}
