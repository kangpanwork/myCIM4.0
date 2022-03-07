package com.fa.cim.service.fam.impl;

import com.fa.cim.annotaion.OmService;
import com.fa.cim.dto.Params;
import com.fa.cim.service.fam.IFAMService;
import lombok.extern.slf4j.Slf4j;

/**
 * description:
 * <p>
 * change history:
 * date defect# person comments
 * ‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐‐
 * 2021/4/25 ******** zh create file
 *
 * @author: zh
 * @date: 2021/4/25 12:53 下午
 * @copyright: 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmService
public class FAMServieImpl implements IFAMService {

//    @Autowired
//    private CustomizeSqlSupport customizeSupport;

    @Override
    public Boolean sxSortJobHistoryStatusChangeReq(Params.SortJobHistoryParams sortJobHistoryParams) {
//        List<Object[]> query = customizeSupport.query(
//                "SELECT\n" +
//                        "\tf.ID\n" +
//                        "FROM\n" +
//                        "\tFHSORTJOBHS f,\n" +
//                        "\tFHSORTJOBHS_COMPONENT fc\n" +
//                        "WHERE\n" +
//                        "\tfc.SORTER_JOB_ID = f.SORTER_JOB_ID\n" +
//                        "\tAND f.SORTER_JOB_STATUS = 'Completed'\n" +
//                        "\tAND f.SORTER_JOB_CATEGORY = ?\n" +
//                        "\tAND fc.DEST_CAST_ID = ?\n" +
//                        "\tAND fc.SRC_CAST_ID = ?",sortJobHistoryParams.getSortJobCategory(),sortJobHistoryParams.getDestCastID(),sortJobHistoryParams.getSrcCastID());
//
//        if (ArrayUtils.isEmpty(query)) return false;
//        for (Object[] objects : query) {
//            customizeSupport.save(
//                    "UPDATE FHSORTJOBHS \n" +
//                    "SET SORTER_JOB_CATEGORY =? \n" +
//                    "WHERE\n" +
//                    "\tID = ?","LotStartComplete",objects[0]);
//        }
        return true;
    }
}