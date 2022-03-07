package com.fa.cim.method.impl;

import com.fa.cim.annotaion.OmMethod;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimDateUtils;
import com.fa.cim.common.utils.Validations;
import com.fa.cim.config.RetCodeConfig;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Results;
import com.fa.cim.method.IFactoryNoteMethod;
import com.fa.cim.newcore.bo.factory.CimFactoryNote;
import com.fa.cim.newcore.bo.factory.MESFactoryManager;
import com.fa.cim.newcore.bo.person.CimPerson;
import com.fa.cim.newcore.factory.BaseCoreFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2018/11/29       ********              Nyx             create file
 *
 * @author: Nyx
 * @date: 2018/11/29 10:19
 * @copyright: 2018, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@OmMethod
@Slf4j
public class FactoryNoteMethod  implements IFactoryNoteMethod {

    @Autowired
    private RetCodeConfig retCodeConfig;

    @Autowired
    @Qualifier("MESFactoryManagerCore")
    private MESFactoryManager mesFactoryManager;

    @Autowired
    private BaseCoreFactory baseCoreFactory;

    @Override
    public Results.EboardInfoInqResult factoryNoteFillInTxPLQ001DR(Infos.ObjCommon objCommon) {
        Results.EboardInfoInqResult out = new Results.EboardInfoInqResult();

        List<com.fa.cim.newcore.bo.factory.CimFactoryNote> factoryNotes = mesFactoryManager.allFactoryNotes();
        if (!CimArrayUtils.isEmpty(factoryNotes)) {
            for (com.fa.cim.newcore.bo.factory.CimFactoryNote factoryNoteBo : factoryNotes) {
                Results.EboardInfoInqResult eboardInfoInqResult = new Results.EboardInfoInqResult();
                eboardInfoInqResult.setReportUserID(new ObjectIdentifier(factoryNoteBo.getPersonID()));
                eboardInfoInqResult.setReportTimeStamp(CimDateUtils.convertToSpecString(factoryNoteBo.getLastUpdateTimeStamp()));
                eboardInfoInqResult.setNoticeTitle(factoryNoteBo.getTitle());
                eboardInfoInqResult.setNoticeDescription(factoryNoteBo.getContents());
                out = eboardInfoInqResult;
            }
        }
        return out;
    }

    @Override
    public void factoryNoteMake(Infos.ObjCommon objCommon, String noticeTitle, String noticeDescription) {

        CimPerson aPerson = baseCoreFactory.getBO(CimPerson.class, objCommon.getUser().getUserID());
        Validations.check(aPerson == null, retCodeConfig.getNotFoundPerson());

        List<CimFactoryNote> noteList = mesFactoryManager.allFactoryNotes();
        if (CimArrayUtils.isEmpty(noteList)){
            log.info("note list is null");
        }
        for (CimFactoryNote factoryNote : noteList) {
            mesFactoryManager.removeFactoryNotes(factoryNote);
        }
        log.info("FW allFactoryNotes is OK");
        log.info("FW removeFactoryNote is OK");

        CimFactoryNote aFactoryNote = mesFactoryManager.createFactoryNote(objCommon.getTimeStamp().getReportTimeStamp(), aPerson);
        log.info("FW createFactoryNote is OK");
        Validations.check(aFactoryNote == null, retCodeConfig.getNotFoundFactorynote());

        aFactoryNote.setTitle(noticeTitle);
        aFactoryNote.setContents(noticeDescription);
        aFactoryNote.setLastUpdateTimeStamp(objCommon.getTimeStamp().getReportTimeStamp());
        aFactoryNote.makeTypeString();
        log.info("FW makeTypeString is OK");
    }
}
