package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Outputs;
import com.fa.cim.entity.nonruntime.CimFimmTransDO;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.method.ITransMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/10/18        ********             Bear               create file
 *
 * @author: Bear
 * @date: 2018/10/18 11:27
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@Slf4j
@OmMethod
public class TransMethod  implements ITransMethod {

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @Override
    public Outputs.ObjTransAccessControlCheckOut transAccessControlCheckDR(Infos.ObjCommon objCommon) {
        Outputs.ObjTransAccessControlCheckOut objTransAccessControlCheckOut = new Outputs.ObjTransAccessControlCheckOut();
        // init
        objTransAccessControlCheckOut.setMachineRecipeState(false);
        objTransAccessControlCheckOut.setProductState(false);
        objTransAccessControlCheckOut.setQueryState(false);
        log.debug("the in-param transactionID: {}", objCommon.getTransactionID());
        CimFimmTransDO cimFimmTransExam = new CimFimmTransDO();
        cimFimmTransExam.setTxID(objCommon.getTransactionID());
        List<CimFimmTransDO> cimFimmTransDO = cimJpaRepository.findAll(Example.of(cimFimmTransExam));
        if (!CimArrayUtils.isEmpty(cimFimmTransDO)){
            CimFimmTransDO fimmTrans = cimFimmTransDO.get(0);
            if (null != fimmTrans) {
                objTransAccessControlCheckOut.setProductState(fimmTrans.getProductState());
                objTransAccessControlCheckOut.setMachineRecipeState(fimmTrans.getMachineRecipeState());
            }
        }
        return objTransAccessControlCheckOut;
    }
}