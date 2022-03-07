package com.fa.cim.controller.parts;

import com.fa.cim.common.constant.TransactionIDEnum;
import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.utils.ThreadContextHolder;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.config.RetCodeConfigEx;
import com.fa.cim.controller.interfaces.parts.IPartsInqControl;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.middleware.standard.api.annotations.listener.CimMapping;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.service.access.IAccessInqService;
import com.fa.cim.service.parts.IBOMPartsInqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * <p>PartsInqControl .
 * change history:
 * date                      defect#             person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2020/4/21 11:22         ********              ZQI             create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2020/4/21 11:22
 * @copyright 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Slf4j
@Listenable
@RestController
@RequestMapping("/parts")
public class PartsInqController implements IPartsInqControl {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    private RetCodeConfigEx retCodeConfigEx;

    @Autowired
    private IAccessInqService accessInqService;

    @Autowired
    private IBOMPartsInqService partsInqService;


    @ResponseBody
    @PostMapping(value = "/bom_parts/inq")
    @Override
    @CimMapping(TransactionIDEnum.BOM_PARTS_INQ)
    public Response bomPartsInq(@RequestBody Params.BOMPartsDefinitionInqInParams bomPartsDefinitionInqInParams) {
        final String transactionID = TransactionIDEnum.BOM_PARTS_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Validations.check(null == bomPartsDefinitionInqInParams, retCodeConfig.getInvalidParameter());
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setProductIDList(Collections.singletonList(bomPartsDefinitionInqInParams.getProductID()));
        accessControlCheckInqParams.setLotIDLists(Collections.singletonList(bomPartsDefinitionInqInParams.getLotID()));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, bomPartsDefinitionInqInParams.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        Results.BOMPartsDefinitionInqResult bomPartsDefinitionInqResult = null;
        try {
            bomPartsDefinitionInqResult = partsInqService.sxBOMPartsDefinitionInq(objCommon, bomPartsDefinitionInqInParams);
        } catch (ServiceException e) {
            if (Validations.isEquals(e.getCode(), retCodeConfig.getBomNotDefined()) && e.getCode() == 1) {
                log.info("Reset RetCode");
            }
        }
        return Response.createSucc(transactionID, bomPartsDefinitionInqResult);
    }

    @ResponseBody
    @PostMapping(value = "/bom_parts_for_lot_list/inq")
    @Override
    @CimMapping(TransactionIDEnum.BOM_PARTS_FOR_LOT_LIST_INQ)
    public Response bomPartsForLotListInq(@RequestBody Params.BOMPartsLotListForProcessInqInParams bomPartsLotListForProcessInqInParams) {
        final String transactionID = TransactionIDEnum.BOM_PARTS_FOR_LOT_LIST_INQ.getValue();
        ThreadContextHolder.setTransactionId(transactionID);

        /*-----------------------------------------------------------------------*/
        /*   Pre Process                                                         */
        /*-----------------------------------------------------------------------*/
        Validations.check(null == bomPartsLotListForProcessInqInParams, retCodeConfig.getInvalidParameter());
        Params.AccessControlCheckInqParams accessControlCheckInqParams = new Params.AccessControlCheckInqParams();
        accessControlCheckInqParams.setProductIDList(Collections.singletonList(bomPartsLotListForProcessInqInParams.getProductID()));
        accessControlCheckInqParams.setLotIDLists(Collections.singletonList(bomPartsLotListForProcessInqInParams.getLotID()));
        Infos.ObjCommon objCommon = accessInqService.checkPrivilegeAndGetObjCommon(transactionID, bomPartsLotListForProcessInqInParams.getUser(), accessControlCheckInqParams);

        /*-----------------------------------------------------------------------*/
        /*   Main Process                                                        */
        /*-----------------------------------------------------------------------*/
        List<Infos.LotListAttributes> lotListAttributes = partsInqService.sxBOMPartsLotListForProcessInq(objCommon, bomPartsLotListForProcessInqInParams);

        return Response.createSucc(transactionID, lotListAttributes);
    }
}
