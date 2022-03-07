package com.fa.cim.service.qtime;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;

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
 * @date: 2021/9/2 9:53 上午
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved
 */
public interface IQtimeListInqService {

    List<Outputs.QrestLotInfo> sxQtimeListInq(Infos.ObjCommon objCommon, Infos.QtimeListInqInfo qtimeListInqInfo);

}