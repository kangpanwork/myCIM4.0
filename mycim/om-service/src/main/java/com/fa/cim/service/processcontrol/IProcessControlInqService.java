package com.fa.cim.service.processcontrol;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.jpa.SearchCondition;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/9/8        ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2020/9/8 17:16
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
public interface IProcessControlInqService {

    Page<Infos.FutureHoldListAttributes> sxFutureHoldListInq(Infos.ObjCommon objCommon, Infos.FutureHoldSearchKey futureHoldSearchKey, Integer count, SearchCondition searchCondition) ;

    List<Infos.LotCtrlStatus> sxNPWUsageStateSelectionInq(Infos.ObjCommon objCommon);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2018/11/19                          Wind
     * @param objCommon
     * @param qtimeDefinitionSelectionInqParam
     * @return RetCode<Results.QtimeDefinitionSelectionInqResult>
     * @author Wind
     * @date 2018/11/19 16:24
     */
    List<Infos.QrestTimeInfo> sxQtimeDefinitionSelectionInq(Infos.ObjCommon objCommon, Params.QtimeDefinitionSelectionInqParam qtimeDefinitionSelectionInqParam);

    /**
     * description:
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * @author Jerry
     * @date 2018/11/13 9:30
     * @param objCommon
     * @param qtimeListInqInfo -
     * @return com.fa.cim.dto.RetCode<Results.QtimeListInqResult>
     */
    public List<Outputs.QrestLotInfo> sxQtimeListInq(Infos.ObjCommon objCommon, Infos.QtimeListInqInfo qtimeListInqInfo);

    /**
    * description:  search qtimelist for searchcondition
    * change history:
    * date             defect             person             comments
    * ---------------------------------------------------------------------------------------------------------------------
    * 2021/6/3 13:56                     Aoki               Create
    *
    * @author Aoki
    * @date 2021/6/3 13:56
    * @param
    * @return java.util.List<com.fa.cim.dto.Outputs.QrestLotInfo>
    *
    */
    public Page<Outputs.QrestLotInfo> sxQtimeListInq(Infos.ObjCommon objCommon, Infos.QtimePageListInqInfo qtimePageListInqInfo);

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param objCommon
     * @param strLotFuturePctrlListInqInParm
     * @return com.fa.cim.dto.RetCode<Results.LotFuturePctrlListInqResult>
     * @author Ho
     * @date 2018/12/4 14:31:14
     */
    Results.LotFuturePctrlListInqResult sxLotFuturePctrlListInq(Infos.ObjCommon objCommon, Infos.LotFuturePctrlListInqInParm strLotFuturePctrlListInqInParm);

    List<ObjectIdentifier> sxQtimeExpiredLotListWithActionPendingInq(Infos.ObjCommon strObjCommonIn, Params.QtimeExpiredLotListWithActionPendingInqInParm strQtimeExpiredLotListWithActionPendingInqInParm);

    List<Infos.ProcHoldListAttributes> sxProcessHoldListInq(Infos.ObjCommon objCommon, Params.ProcessHoldListInqParams params);

    List<Infos.FutureReworkInfo> sxFutureReworkListInq(Infos.ObjCommon objCommon, ObjectIdentifier lotID) ;
}