package com.fa.cim.method;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * description:
 * IMinQTimeMethod
 * change history:
 * date         defect#         person      comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 21-6-10       ********        Grant       create file
 *
 * @author: Grant
 * @date: 21-6-10 16:17
 * @copyright: 2021, FA Software (Chengdu) Co., Ltd. All Rights Reserved.
 */
public interface IMinQTimeMethod {

    /**
     * Lot 在 Trigger Operation 执行 Move Out/Pass Thru 等操作时, 检查是否对应有 Min Q-Time 限制, 并设置运行时数据
     * @param objCommon 通用参数
     * @param cassettes 晶舟集合
     * @version 0.1
     * @author Grant
     * @date 2021/6/10
     */
    void checkAndSetRestrictions(Infos.ObjCommon objCommon, List<Infos.StartCassette> cassettes);

    /**
     * Lot 在 Trigger Operation 执行 Move Out/Pass Thru 等操作时, 检查是否对应有 Min Q-Time 限制, 并设置运行时数据
     * @param objCommon 通用参数
     * @param lotID Lot的ID
     * @version 0.1
     * @author Grant
     * @date 2021/6/10
     */
    void checkAndSetRestrictions(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * 检查指定Lot是否有target operation的替换, 并执行替换动作
     * @param objCommon 通用参数
     * @param lotID Lot的ID
     * specificControlFlag 特定控制标识: true.表示当前operation可能已经变化 false.process operation没有变化
     *                            (由于Move Out的异步操作可能导致执行本方法逻辑的时候, process operation可能已经发生了变化)
     * @version 0.1
     * @author Grant
     * @date 2021/9/29
     */
    void checkTargetOpeReplace(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * 检查指定Lot是否有trigger operation的替换, 并执行替换动作
     * @param objCommon 通用参数
     * @param lotID Lot的ID
     * @version 0.1
     * @author Grant
     * @date 2021/9/29
     */
    void checkTriggerOpeReplace(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * Lot 在 Target Operation 执行 Lot Reserve/Move In 时,
     * 通过最小时间限制判断是否拒绝 Lot Reserve / Move In / Pass Thru 或继续执行
     * @param objCommon 通用参数
     * @param cassettes 晶舟集合
     * @version 0.1
     * @author Grant
     * @date 2021/6/16
     */
    void checkIsRejectByRestriction(Infos.ObjCommon objCommon, List<Infos.StartCassette> cassettes);

    /**
     * Lot 在 Target Operation 执行 Lot Reserve / Move In / Pass Thru 时,
     * 通过最小时间限制判断是否拒绝 Lot Reserve / Move In / Pass Thru 或继续执行
     * @param objCommon 通用参数
     * @param lotID Lot的ID
     * @version 0.1
     * @author Grant
     * @date 2021/7/14
     */
    void checkIsRejectByRestriction(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * Lot 执行 Split 分批操作时, 子批要继承母批的 Min Q-Time (继承的是运行时数据)
     * @param parentLotID 母批 Lot 的ID
     * @param childLotID 子批 Lot 的ID
     * @version 0.1
     * @author Grant
     * @date 2021/6/17
     */
    void lotSplit(ObjectIdentifier parentLotID, ObjectIdentifier childLotID);

    /**
     * Lot 执行 Merge 合批操作时对子/母Lot的Min Q-Time处理, 规则如下:
     * 1.子/母Lot都有Min Q-Time时, 系统根据子母批Min Q-Time剩余时间的长短, 选择继承剩余时间最长的Min Q-Time;
     * 2.当子Lot有Min Q-Time, 母Lot没有, Merge后, 母Lot继承子Lot的Min Q-Time
     * 3.当母Lot有Min Q-Time, 子Lot没有, Merge后, 母Lot的Min Q-Time保持不变
     * 4.子/母Lot都没有Min Q-Time时, 正常Merge
     * @param parentLotID 母批 Lot 的ID
     * @param childLotID 子批 Lot 的ID
     * @version 0.1
     * @author Grant
     * @date 2021/6/17
     */
    void lotMerge(ObjectIdentifier parentLotID, ObjectIdentifier childLotID);

    /**
     * 清除过期失效的Min Q-Time运行时数据
     * @version 0.1
     * @author Grant
     * @date 2021/6/20
     */
    void clearInvalid();

    /**
     * 检查指定Lot是否在Min Q-Time时间限制区域内
     * @param lotID 指定Lot的ID
     * @param mainProcessFlow current route(对应Lot的 MROUTE_PRF_RKEY 字段)
     * @param processFlowContextObj branch Route(对应Lot的 PRFCX_RKEY 字段)
     * @return 返回: true.在限制区域内; false.不在限制区域内
     * @version 0.1
     * @author Grant
     * @date 2021/6/20
     */
    boolean checkIsInRestriction(ObjectIdentifier lotID, String mainProcessFlow, String processFlowContextObj);

    /**
     * 查询指定Lot的所有正在运行的Min Q-Time限制数据
     * @param lotID 指定Lot的ID
     * @param mainProcessFlow 关联字段 MROUTE_PRF_RKEY
     * @return {@link Infos.LotQtimeInfo}
     * @version 0.1
     * @author Grant
     * @date 2021/6/20
     */
    List<Infos.LotQtimeInfo> getRestrictInProcessArea(ObjectIdentifier lotID, String mainProcessFlow);

    /**
     * 通过Lot获取对应有效的Min Q-Time运行时限制数据
     * @param objCommon 通用参数
     * @param lotID Lot的ID
     * @return {@link Outputs.QrestLotInfo} 返回有效的限制数据
     * @version 0.1
     * @author Grant
     * @date 2021/6/20
     */
    List<Outputs.QrestLotInfo> getRestrictionsByLot(Infos.ObjCommon objCommon, ObjectIdentifier lotID);

    /**
     * 通过条件查询有效的Min Q-Time运行时限制数据
     * @param objCommon
     * @param params
     * @return
     * @version 0.1
     * @author Grant
     * @date 2021/7/11
     */
    Page<Outputs.QrestLotInfo> getRestrictionsByPage(Infos.ObjCommon objCommon,
                                                     Infos.QtimePageListInqInfo params);

}
