package com.fa.cim.controller.qtime;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.method.IQTimeMethod;
import com.fa.cim.method.IUtilsComp;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.qtime.IQtimeExpiredLotListWithActionPendingInqService;
import com.fa.cim.service.qtime.IQtimeListInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * description: 提供给qTime-Sentinel调用
 * <p>
 * change history:
 * date             defect#             person             comments
 * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐
 * 2021/9/3         ********             ZH                 create file
 *
 * @author: ZH
 * @date: 2021/9/3 3:03 下午
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved
 */
@RestController
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
@Slf4j
@RequestMapping("/qteap")
@Listenable
public class QtimeExpiredLotListWithActionPendingInqController {

    @Autowired
    private IUtilsComp utilsComp;

    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private IQtimeListInqService qtimeListInqService;

    @Autowired
    private IQTimeMethod qTimeMethod;

    @Autowired
    private IQtimeExpiredLotListWithActionPendingInqService qtimeExpiredLotListWithActionPendingInqService;

    @RequestMapping(value = "/qtime_expired_lot_list_with_action_pending/inq", method = RequestMethod.POST)
    public Response qtimeExpiredLotListWithActionPendingInq(@RequestBody Params.QtimeExpiredLotListWithActionPendingInqInParm strQtimeExpiredLotListWithActionPendingInqInParm) {
        String txId = TransactionIDEnum.EXPIRED_QREST_TIME_LOT_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(txId);
        User user = strQtimeExpiredLotListWithActionPendingInqInParm.getUser();
        Infos.ObjCommon objCommon = utilsComp.setObjCommon(TransactionIDEnum.EXPIRED_QREST_TIME_LOT_LIST_INQ, user);
        //step2 -  call txAccessControlCheckInq(...)
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessInqService.sxAccessControlCheckInq(objCommon, accessControlCheckInqParams);

        // step3 - txQtimeExpiredLotListWithActionPendingInq
        List<ObjectIdentifier> rc = qtimeExpiredLotListWithActionPendingInqService.sxQtimeExpiredLotListWithActionPendingInq(objCommon, strQtimeExpiredLotListWithActionPendingInqInParm);

        return Response.createSucc(txId, rc);
    }


}