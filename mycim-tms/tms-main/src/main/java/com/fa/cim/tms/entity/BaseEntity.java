package com.fa.cim.tms.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/2/18        ********             miner               create file
 *
 * @author: Miner
 * @date: 2020/2/18 14:03
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    /**
     * Serial Version Uid
     */
    private static final long serialVersionUID = 4050860896851433100L;
    /**
     * The identity.
     */
    @Id
    @Column(name = "ID")
//    @GenericGenerator(name = "idGenerator", strategy = SnowflakeIDWorker.STRATEGY_REFERENCE)
//    @GeneratedValue(generator = "idGenerator")
    private String id;
}
