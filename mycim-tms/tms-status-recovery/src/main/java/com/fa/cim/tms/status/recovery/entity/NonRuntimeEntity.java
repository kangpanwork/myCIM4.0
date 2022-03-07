package com.fa.cim.tms.status.recovery.entity;


import javax.persistence.MappedSuperclass;

/**
 * description:
 * <p>NonRuntimeEntity .</p>
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2019/4/10        ********             PlayBoy               create file
 *
 * @author: PlayBoy
 * @date: 2019/4/10 09:58
 * @copyright: 2019, FA Software (Chengdu) Co., Ltd. All Rights Reserved.
 */
@MappedSuperclass
public class NonRuntimeEntity extends BaseEntity {
    private static final long serialVersionUID = -4078245682552242562L;
}
