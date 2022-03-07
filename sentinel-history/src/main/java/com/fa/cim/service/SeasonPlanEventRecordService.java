package com.fa.cim.service;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Response;
import com.fa.cim.method.TableMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.fa.cim.Constant.SPConstant.*;
import static com.fa.cim.utils.BaseUtils.*;
import static com.fa.cim.utils.StringUtils.variableStrCmp;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @exception
 * @author Ho
 * @date 2019/6/30 15:06
 */
@Slf4j
@Repository
@Transactional(rollbackFor = Exception.class)
public class SeasonPlanEventRecordService {

    @Autowired
    private TableMethod tableMethod;

    @Autowired
    private SeasonPlanHistoryService seasonPlanHistoryService;

    @Autowired
    private LotOperationHistoryService lotOperationHistoryService;


    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
      * @param event
     * @return void
     * @exception
     * @author ho
     * @date 2020/7/31 12:58
     */
    public void createSeasonPlanEventRecord(String event) {
        // insert OHSEASON
        String id = seasonPlanHistoryService.insertOHSEASON(event);

        // insert OHSEASON_CHAMBER
        seasonPlanHistoryService.insertOHSEASONCHAMBER(event,id);

        // insert OHSEASON_PARAM
        seasonPlanHistoryService.insertOHSEASONPARAM(event,id);

        // insert OHSEASON_PRODRECIPE
        seasonPlanHistoryService.insertOHSEASONPRODRECIPE(event,id);

        // insert OHSEASON_PRODUCT
        seasonPlanHistoryService.insertOHSEASONPRODUCT(event,id);

        // inset OHSEASON_UDATA
        seasonPlanHistoryService.insertOHSEASONUDATA(event,id);
    }
}
