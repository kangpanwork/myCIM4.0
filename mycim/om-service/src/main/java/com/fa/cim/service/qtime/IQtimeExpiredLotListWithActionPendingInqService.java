package com.fa.cim.service.qtime;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐
 * 2021/9/2         ********             ZH                 create file
 *
 * @author: ZH
 * @date: 2021/9/2 9:52 上午
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved
 */
public interface IQtimeExpiredLotListWithActionPendingInqService {

    List<ObjectIdentifier> sxQtimeExpiredLotListWithActionPendingInq(Infos.ObjCommon strObjCommonIn, Params.QtimeExpiredLotListWithActionPendingInqInParm strQtimeExpiredLotListWithActionPendingInqInParm);

}