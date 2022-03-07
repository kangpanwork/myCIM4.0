package com.fa.cim.service;

import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.fa.cim.utils.BaseUtils.*;

/**
 * description:
 * <p></p>
 * change history:
 * date             defect             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 *
 * @exception
 * @author Ho
 * @date 2019/5/11 15:12
 */
@Transactional(rollbackFor = Exception.class)
@Repository
public class CollectedDataEventRecordService {

    @Autowired
    private CollectedDataHistoryService collectedDataHistoryService;

    /**
     * description:
     * <p></p>
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     *
     * @param collectedDataEventRecord
     * @param userDataSets
     * @return com.fa.cim.dto.Response
     * @exception
     * @author Ho
     * @date 2019/5/11 15:20
     */
    public Response createCollectedDataEventRecord(Infos.CollectedDataEventRecord collectedDataEventRecord, List<Infos.UserDataSet> userDataSets) {
        Response iRc=returnOK();

        Params.Param<Boolean> setFlag=new Params.Param<>(false);
        iRc = collectedDataHistoryService.createFHCDATAHS_DATA( collectedDataEventRecord, userDataSets, setFlag );
        if (!isOk(iRc )) {
            return( iRc );
        }

        if( isTrue(setFlag.getValue()) ) {
            iRc = collectedDataHistoryService.createFHCDATAHS( collectedDataEventRecord, userDataSets );
            if ( !isOk(iRc) ) {
                return( iRc );
            }
        }


        if( isTrue(setFlag.getValue()) ) {

            iRc = collectedDataHistoryService.createFHCDATAHS_LOT( collectedDataEventRecord, userDataSets );
            if (!isOk(iRc ) ) {
                return( iRc );
            }
        }

        if( isTrue(setFlag.getValue()) ) {
            iRc = collectedDataHistoryService.createFHCDATAHS_SPEC( collectedDataEventRecord, userDataSets );
            if (!isOk(iRc ) ) {
                return( iRc );
            }
        }
        if ( isTrue(setFlag.getValue()) ) {
            iRc = collectedDataHistoryService.createFHCDATAHS_ACTIONS( collectedDataEventRecord, userDataSets );
            if ( !isOk(iRc ) ) {
                return iRc;
            }
        }

        return returnOK();
    }

}
