package com.fa.cim.pcs.engine;

import com.fa.cim.common.utils.SpringContextUtil;
import com.fa.cim.newcore.functions.ExtendableBO;
import com.fa.cim.newcore.bo.abstractgroup.CimPropertyBase;
import com.fa.cim.newcore.bo.code.CimCategory;
import com.fa.cim.newcore.bo.code.CodeManager;
import com.fa.cim.newcore.bo.durable.CimCassette;
import com.fa.cim.newcore.bo.durable.CimProcessDurable;
import com.fa.cim.newcore.bo.durable.CimReticlePod;
import com.fa.cim.newcore.bo.durable.DurableManager;
import com.fa.cim.newcore.bo.machine.CimMachine;
import com.fa.cim.newcore.bo.machine.CimStorageMachine;
import com.fa.cim.newcore.bo.machine.MachineManager;
import com.fa.cim.newcore.bo.person.CimPerson;
import com.fa.cim.newcore.bo.person.CimUserGroup;
import com.fa.cim.newcore.bo.person.PersonManager;
import com.fa.cim.newcore.bo.prodspec.CimProductSpecification;
import com.fa.cim.newcore.bo.prodspec.CimTechnology;
import com.fa.cim.newcore.bo.prodspec.ProductSpecificationManager;
import com.fa.cim.newcore.bo.product.CimLot;
import com.fa.cim.newcore.bo.product.CimWafer;
import com.fa.cim.newcore.bo.product.ProductManager;
import com.fa.cim.newcore.bo.recipe.CimLogicalRecipe;
import com.fa.cim.newcore.bo.recipe.CimMachineRecipe;
import com.fa.cim.newcore.bo.recipe.RecipeManager;
import com.fa.cim.newcore.standard.drblmngm.Cassette;
import com.fa.cim.newcore.standard.drblmngm.ProcessDurable;
import com.fa.cim.pcs.attribute.property.ScriptProperty;
import com.fa.cim.pcs.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScriptEntityFactory {

    private ProductManager productManager;
    private MachineManager machineManager;
    private ProductSpecificationManager productSpecificationManager;
    private final DurableManager durableManager;
    private final RecipeManager recipeManager;
    private final CodeManager codeManager;
    private final PersonManager personManager;

    @Autowired
    public ScriptEntityFactory(ProductManager productManager,
                               MachineManager machineManager,
                               ProductSpecificationManager productSpecificationManager,
                               DurableManager durableManager,
                               RecipeManager recipeManager, CodeManager codeManager, PersonManager personManager) {
        this.productManager = productManager;
        this.machineManager = machineManager;
        this.productSpecificationManager = productSpecificationManager;
        this.durableManager = durableManager;
        this.recipeManager = recipeManager;
        this.codeManager = codeManager;
        this.personManager = personManager;
    }

    /**
     * Generate a instance of ScriptEntity from Spring BeanFactory.
     *
     * @param <B>    BO object, must be extends {@link ExtendableBO}
     * @param <S>    subclass of {@link ScriptEntity}
     * @param tClass class type that need to generate
     * @param bo     bo business instance
     * @return instance of {@link ScriptEntity}
     * @version 1.0
     * @author ZQI
     * @date 2019/12/25 16:48
     */
    public <B extends ExtendableBO, S extends ScriptEntity<B>> S generateScriptEntity(Class<S> tClass, B bo) {
        assert null != tClass;
        return null == bo ? null : SpringContextUtil.getBean(tClass, bo);
    }

    public <B extends CimPropertyBase, P extends ScriptProperty<B>> P generateScriptProperty(Class<P> tClass, B bo) {
        assert null != tClass;
        return null == bo ? null : SpringContextUtil.getBean(tClass, bo);
    }

    /**
     * Convert a {@link CimLot} to {@link ScriptLot},and return it.
     *
     * @param lotId lot id
     * @return {@link ScriptLot}
     * @version 1.0
     * @author ZQI
     * @date 2019/12/24 17:26
     */
    public ScriptLot lot(String lotId) {
        CimLot lot = productManager.findLotNamed(lotId);
        return generateScriptEntity(ScriptLot.class, lot);
    }

    /**
     * Convert a {@link CimMachine} to {@link ScriptEquipment},and return it.
     *
     * @param equipmentId equip id
     * @return {@link ScriptEquipment}
     * @version 1.0
     * @author ZQI
     * @date 2019/12/24 17:27
     */
    public ScriptEquipment equipment(String equipmentId) {
        CimMachine machine = machineManager.findMachineNamed(equipmentId);
        return generateScriptEntity(ScriptEquipment.class, machine);
    }

    /**
     * Convert a {@link CimProductSpecification} to {@link ScriptProduct},and return it.
     *
     * @param productId product id
     * @return {@link ScriptProduct}
     * @version 1.0
     * @author ZQI
     * @date 2019/12/24 17:28
     */
    public ScriptProduct product(String productId) {
        CimProductSpecification prodSpec = productSpecificationManager.findProductSpecificationNamed(productId);
        return generateScriptEntity(ScriptProduct.class, prodSpec);
    }

    /**
     * Convert a {@link CimWafer} to {@link ScriptWafer},and return it.
     *
     * @param waferId wafer id
     * @return {@link ScriptWafer}
     * @version 1.0
     * @author ZQI
     * @date 2019/12/24 17:29
     */
    public ScriptWafer wafer(String waferId) {
        CimWafer wafer = productManager.findWaferNamed(waferId);
        return generateScriptEntity(ScriptWafer.class, wafer);
    }

    /**
     * Convert a {@link ProcessDurable} to {@link ScriptFixture},and return it.
     *
     * @param fixtureId fixture id
     * @return {@link ScriptFixture}
     * @version 1.0
     * @author ZQI
     * @date 2019/12/24 17:29
     */
    public ScriptFixture fixture(String fixtureId) {
        ProcessDurable durable = durableManager.findProcessDurableNamed(fixtureId);
        return generateScriptEntity(ScriptFixture.class, (CimProcessDurable) durable);
    }

    /**
     * Convert a {@link ProcessDurable} to {@link ScriptReticle},and return it.
     *
     * @param reticleId reticle id
     * @return {@link ScriptReticle}
     * @version 1.0
     * @author ZQI
     * @date 2019/12/24 17:30
     */
    public ScriptReticle reticle(String reticleId) {
        ProcessDurable durable = durableManager.findProcessDurableNamed(reticleId);
        return generateScriptEntity(ScriptReticle.class, (CimProcessDurable) durable);
    }

    /**
     * Convert a {@link CimReticlePod} to {@link ScriptReticlePod},and return it.
     *
     * @param reticlePodId reticle pod id
     * @return {@link ScriptReticlePod}
     * @version 1.0
     * @author ZQI
     * @date 2019/12/24 17:37
     */
    public ScriptReticlePod reticlePod(String reticlePodId) {
        CimReticlePod reticlePod = durableManager.findReticlePodNamed(reticlePodId);
        return generateScriptEntity(ScriptReticlePod.class, reticlePod);
    }

    /**
     * Convert a {@link CimMachineRecipe} to {@link ScriptMachineRecipe},and return it.
     *
     * @param recipeId machine recipe id
     * @return {@link ScriptMachineRecipe}
     * @version 1.0
     * @author ZQI
     * @date 2019/12/24 17:31
     */
    public ScriptMachineRecipe recipe(String recipeId) {
        CimMachineRecipe recipe = recipeManager.findMachineRecipeNamed(recipeId);
        return generateScriptEntity(ScriptMachineRecipe.class, recipe);
    }

    /**
     * Convert a {@link CimLogicalRecipe} to {@link ScriptLogicRecipe},and return it.
     *
     * @param logicRecipeId logic recipe id
     * @return {@link ScriptLogicRecipe}
     * @version 1.0
     * @author ZQI
     * @date 2019/12/24 17:31
     */
    public ScriptLogicRecipe logicRecipe(String logicRecipeId) {
        CimLogicalRecipe recipe = recipeManager.findLogicalRecipeNamed(logicRecipeId);
        return generateScriptEntity(ScriptLogicRecipe.class, recipe);
    }

    /**
     * Convert a {@link Cassette} to {@link ScriptCarrier},and return it.
     *
     * @param carrierId carrier id
     * @return {@link ScriptCarrier}
     * @version 1.0
     * @author ZQI
     * @date 2019/12/24 17:33
     */
    public ScriptCarrier carrier(String carrierId) {
        Cassette cassette = durableManager.findCassetteNamed(carrierId);
        return generateScriptEntity(ScriptCarrier.class, (CimCassette) cassette);
    }

    /**
     * Convert a {@link CimCategory} to {@link ScriptCategory},and return it.
     *
     * @param categoryId category id
     * @return {@link ScriptCategory}
     * @version 1.0
     * @author ZQI
     * @date 2019/12/24 17:42
     */
    public ScriptCategory category(String categoryId) {
        CimCategory category = codeManager.findCategoryNamed(categoryId);
        return generateScriptEntity(ScriptCategory.class, category);
    }

    /**
     * Convert a {@link CimStorageMachine} to {@link ScriptStocker},and return it.
     *
     * @param stockerId stocker id
     * @return {@link ScriptStocker}
     * @version 1.0
     * @author ZQI
     * @date 2019/12/24 17:46
     */
    public ScriptStocker stocker(String stockerId) {
        CimStorageMachine storageMachine = machineManager.findStorageMachineNamed(stockerId);
        return generateScriptEntity(ScriptStocker.class, storageMachine);
    }

    /**
     * Convert a {@link CimTechnology} to {@link ScriptTechnology},and return it.
     *
     * @param technologyId technology id
     * @return {@link ScriptTechnology}
     * @version 1.0
     * @author ZQI
     * @date 2019/12/24 17:48
     */
    public ScriptTechnology technology(String technologyId) {
        CimTechnology technology = productSpecificationManager.findTechnologyNamed(technologyId);
        return generateScriptEntity(ScriptTechnology.class, technology);
    }

    /**
     * Convert a {@link CimPerson} to {@link ScriptUser},and return it.
     *
     * @param userId user id
     * @return {@link ScriptUser}
     * @version 1.0
     * @author ZQI
     * @date 2019/12/24 17:50
     */
    public ScriptUser user(String userId) {
        CimPerson person = personManager.findPersonNamed(userId);
        return generateScriptEntity(ScriptUser.class, person);
    }

    /**
     * Convert a {@link CimUserGroup} to {@link ScriptUserGroup},and return it.
     *
     * @param userGroupId user group id
     * @return {@link ScriptUserGroup}
     * @version 1.0
     * @author ZQI
     * @date 2019/12/24 17:50
     */
    public ScriptUserGroup userGroup(String userGroupId) {
        CimUserGroup userGroup = personManager.findUserGroupNamed(userGroupId);
        return generateScriptEntity(ScriptUserGroup.class, userGroup);
    }
}
