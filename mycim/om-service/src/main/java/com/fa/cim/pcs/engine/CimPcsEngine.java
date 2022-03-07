package com.fa.cim.pcs.engine;

import com.fa.cim.common.exception.ServiceException;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.OmCode;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.SpringContextUtil;
import com.fa.cim.dto.Infos;
import com.fa.cim.newcore.bo.person.CimPerson;
import com.fa.cim.newcore.bo.person.PersonManager;
import com.fa.cim.pcs.entity.ScriptGlobal;
import com.fa.cim.pcs.entity.utils.Date;
import com.fa.cim.pcs.entity.utils.System;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;


@Component
@Slf4j
public class CimPcsEngine {

    private PersonManager personManager;
    private ScriptEngine engine;
    private final ScriptEntityFactory scriptEntityFactory;

    /***
     * Languages supported by the script.
     */
    private static final String SCRIPT_LANGUAGE = "javascript";

    /***
     * Provides some basic Class for Script are as follows.
     */
    private static final String SCRIPT_FACTORY = "$Factory";
    private static final String SCRIPT_SYSTEM = "$System";
    private static final String SCRIPT_DATE = "$Date";
    private static final String SCRIPT_LOG = "Log";
    private static final String SCRIPT_PARAM_GLOBAL = "$Global";
    private static final String SCRIPT_LOT = "$Lot";
    private static final String SCRIPT_EQP = "$EQP";

    /***
     * Provides some basic Script Variable are as follows.
     */
    private static final String PARAM_LOT_ID = "$LotId";
    private static final String PARAM_EQP_ID = "$EquipmentId";
    private static final String PARAM_USER_ID = "$UserId";
    private static final String PARAM_TX_ID = "$TransactionId";

    @Autowired
    public CimPcsEngine(PersonManager personManager, ScriptEntityFactory scriptEntityFactory,
                        System system, Date date) {
        this.personManager = personManager;
        this.scriptEntityFactory = scriptEntityFactory;
        this.engine = new ScriptEngineManager().getEngineByName(SCRIPT_LANGUAGE);
        Bindings bindings = this.engine.getBindings(ScriptContext.GLOBAL_SCOPE);
        bindings.put(SCRIPT_FACTORY, scriptEntityFactory);
        bindings.put(SCRIPT_SYSTEM, system);
        bindings.put(SCRIPT_DATE, date);
        bindings.put(SCRIPT_LOG, log);
    }

    public void runScript(Infos.ObjCommon objCommon, ScriptParam param) {
        ObjectIdentifier userId = objCommon.getUser().getUserID();
        if (!ObjectIdentifier.isEmpty(userId) && CimStringUtils.isEmpty(ObjectIdentifier.fetchReferenceKey(userId))) {
            CimPerson user = personManager.findPersonNamed(ObjectIdentifier.fetchValue(userId));
            userId.setReferenceKey(user.getPrimaryKey());
        }
        ScriptThreadHolder.init(objCommon, param.getPhase(), param.getScriptName());
        Bindings localParams = createLocalParams(param);
        try {
            engine.eval(param.getScript(), localParams);
        } catch (ServiceException e) {
            log.error(e.getMessage());
            throw new ServiceException(new OmCode(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ServiceException(new OmCode(OmCode.ERROR_CODE, e.getMessage()));
        } finally {
            ScriptThreadHolder.clear();
        }
    }

    private Bindings createLocalParams(ScriptParam param) {
        Bindings bindings = engine.createBindings();
        String lotID = ObjectIdentifier.fetchValue(param.getLotId());
        String equipmentID = ObjectIdentifier.fetchValue(param.getEquipmentId());

        bindings.put(PARAM_LOT_ID, lotID);
        bindings.put(PARAM_EQP_ID, equipmentID);
        bindings.put(PARAM_USER_ID, ObjectIdentifier.fetchValue(ScriptThreadHolder.getUserID()));
        bindings.put(PARAM_TX_ID, ScriptThreadHolder.getObjCommon().getTransactionID());

        bindings.put(SCRIPT_PARAM_GLOBAL, SpringContextUtil.getSingletonBean(ScriptGlobal.class));
        bindings.put(SCRIPT_LOT, scriptEntityFactory.lot(lotID));
        bindings.put(SCRIPT_EQP, scriptEntityFactory.equipment(equipmentID));

        return bindings;
    }
}
