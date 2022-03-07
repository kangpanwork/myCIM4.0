package com.fa.cim.controller;

import com.fa.cim.dto.Params;
import com.fa.cim.dto.Response;
import com.fa.cim.service.LotOperationEventRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.sql.DataSourceDefinition;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @author Ho
 * @date 2019/2/25 11:02:35
 */
@RestController
public class LotOperationEventRecordController {

    @Autowired
    private LotOperationEventRecordService lotOperationEventRecordService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param params
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/3/1 16:51
     */
    @PostMapping("/create_lot_operation_start/event_record")
    public Response createLotOperationStartEventRecord(@RequestBody Params.CreateLotOperationStartEventRecord params) {
        return lotOperationEventRecordService.createLotOperationStartEventRecord(params.getLotOperationStartEventRecord(),params.getUserDataSets());
    }

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param params
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/3/1 16:52
     */
    @PostMapping("create_lot_operation_complete/event_record")
    public Response createLotOperationCompleteEventRecord(Params.CreateLotOperationCompleteEventRecord params) {
        return lotOperationEventRecordService.createLotOperationCompleteEventRecord(params.getLotOperationCompleteEventRecord(),params.getUserDataSets());
    }

}
