package com.fa.cim.pcs.entity;

import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimNumberUtils;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.pd.CimProcessOperation;
import com.fa.cim.newcore.bo.recipe.CimLogicalRecipe;
import com.fa.cim.newcore.bo.recipe.CimMachineRecipe;
import com.fa.cim.newcore.dto.pd.ProcessDTO;
import com.fa.cim.pcs.annotations.PcsEntity;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * <p>ScriptOperation .
 *
 * @author ZQI
 * @version 1.0
 * @date 2019/12/24 16:11
 * @copyright 2019, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@PcsEntity
public class ScriptOperation extends ScriptEntity<CimProcessOperation> {

    ScriptOperation(CimProcessOperation bizObject) {
        super(bizObject);
    }

    /**
     * Gets the operation number for PO.
     *
     * @return operation number
     * @version 1.0
     * @author ZQI
     * @date 2019/12/24 16:19
     */
    public String operationNum() {
        return bizObject.getOperationNumber();
    }

    /**
     * Gets the step identifier for PO.
     *
     * @return step id
     * @version 1.0
     * @author ZQI
     * @date 2020/6/8 12:44
     */
    public String stepId() {
        return bizObject.getProcessDefinition().getIdentifier();
    }

    /**
     * Gets the route identifier for PO.
     *
     * @return route identifier
     * @version 1.0
     * @author ZQI
     * @date 2019/12/24 16:18
     */
    public String routeId() {
        return bizObject.getModuleProcessDefinition().getIdentifier();
    }

    /**
     * Gets the process identifier for PO.
     *
     * @return process identifier
     * @version 1.0
     * @author ZQI
     * @date 2020/6/8 11:00
     */
    public String processId() {
        return bizObject.getMainProcessDefinition().getIdentifier();
    }

    /**
     * Gets the operation start time.
     *
     * @return move in time
     * @version 1.0
     * @author ZQI
     * @date 2019/12/24 16:21
     */
    public Timestamp moveInTime() {
        return bizObject.getActualStartTimeStamp();
    }

    /**
     * Gets the operation completed time.
     *
     * @return move out time
     * @version 1.0
     * @author ZQI
     * @date 2019/12/24 16:30
     */
    public Timestamp moveOutTime() {
        return bizObject.getActualCompTimeStamp();
    }

    /**
     * Gets the used reticles for PO.
     *
     * @return array of {@link ScriptReticle}
     * @version 1.0
     * @author ZQI
     * @date 2019/12/24 17:02
     */
    public ScriptReticle[] usedReticles() {
        List<ProcessDTO.StartReticleInfo> assignedReticles = bizObject.getAssignedReticles();
        ScriptReticle[] reticles = new ScriptReticle[CimArrayUtils.getSize(assignedReticles)];
        Optional.ofNullable(assignedReticles).ifPresent(list -> {
            int index = 0;
            for (ProcessDTO.StartReticleInfo reticle : list) {
                reticles[index++] = factory.reticle(ObjectIdentifier.fetchValue(reticle.getReticleID()));
            }
        });
        return reticles;
    }

    /**
     * Gets a used reticle for PO.
     *
     * @return a used {@link ScriptReticle}
     * @version 1.0
     * @author ZQI
     * @date 2019/12/24 17:02
     */
    public ScriptReticle usedReticle() {
        return Optional.ofNullable(usedReticles()).map(data -> data[0]).orElse(null);
    }

    /**
     * Gets the used fixtures for PO.
     *
     * @return array of {@link ScriptFixture}
     * @todo continue development if the fixture function is need in the future.
     * @version 1.0
     * @author ZQI
     * @date 2019/12/24 17:17
     */
    public ScriptFixture[] usedFixtures() {
        return null;
    }

    /**
     * Gets a used fixture for PO.
     *
     * @return a used {@link ScriptFixture}
     * @version 1.0
     * @author ZQI
     * @date 2019/12/25 9:53
     */
    public ScriptFixture usedFixture() {
        return Optional.ofNullable(usedFixtures()).map(data -> data[0]).orElse(null);
    }

    /**
     * Gets the used machine recipe for PO.
     *
     * @return {@link ScriptMachineRecipe}
     * @version 1.0
     * @author ZQI
     * @date 2019/12/24 17:54
     */
    public ScriptMachineRecipe usedRecipe() {
        CimMachineRecipe machineRecipe = bizObject.getAssignedMachineRecipe();
        return factory.generateScriptEntity(ScriptMachineRecipe.class, machineRecipe);
    }

    /**
     * Gets the used logic recipe for PO.
     *
     * @return {@link ScriptLogicRecipe}
     * @version 1.0
     * @author ZQI
     * @date 2019/12/24 17:56
     */
    public ScriptLogicRecipe usedLogicalRecipe() {
        CimLogicalRecipe logicalRecipe = bizObject.getAssignedLogicalRecipe();
        return factory.generateScriptEntity(ScriptLogicRecipe.class, logicalRecipe);
    }

    /**
     * Gets the used equipment for PO.
     *
     * @return {@link ScriptEquipment}
     * @version 1.0
     * @author ZQI
     * @date 2019/12/24 17:59
     */
    public ScriptEquipment usedEquipment() {
        CimMachine machine = bizObject.getAssignedMachine();
        return factory.generateScriptEntity(ScriptEquipment.class, machine);
    }

    /**
     * Gets the pass count for PO.
     *
     * @return pass count
     * @version 1.0
     * @author ZQI
     * @date 2019/12/24 18:23
     */
    public int passCount() {
        return CimNumberUtils.intValue(bizObject.getPassCount());
    }

}
