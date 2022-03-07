package com.fa.cim.rtms;

import com.fa.cim.common.support.User;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2021/8/24        ********              Decade               create file
 * * @author: Decade
 *
 * @date: 2021/8/24 16:42
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Data
public class ReticleRTMSMoveInAndReserveReqParams {

    private List<String> reticles;

    private int waferCount;

    private boolean hasReserve;

    private String lotId;

    private User user;

    private String productId;

    private String lotType;

    private String processId;

    private String opeNo;

    private int opePassCount;

    private String equipmentId;

    private Timestamp trxTime;

    private String opeCategory;


}