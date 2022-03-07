package com.fa.cim.controller.test;

import com.alibaba.fastjson.JSONObject;
import com.fa.cim.common.support.ObjectIdentifier;
import com.fa.cim.common.support.Response;
import com.fa.cim.common.support.User;
import com.fa.cim.common.utils.CimArrayUtils;
import com.fa.cim.common.utils.CimStringUtils;
import com.fa.cim.common.utils.SnowflakeIDWorker;
import com.fa.cim.controller.edc.EngineerDataCollectionInqController;
import com.fa.cim.dto.Infos;
import com.fa.cim.dto.Params;
import com.fa.cim.dto.Results;
import com.fa.cim.jpa.CimJpaRepository;
import com.fa.cim.middleware.standard.api.annotations.listener.Listenable;
import com.fa.cim.test.entity.FlowEapDO;
import com.fa.cim.test.entity.FlowLotDO;
import com.fa.cim.test.repository.FlowEapDao;
import com.fa.cim.test.repository.FlowLotDao;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * <p>StressTestController .
 * change history:
 * date                      defect#             person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2021/1/30 17:33         ********              ZQI             create file.
 *
 * @author ZQI
 * @date 2021/1/30 17:33
 * @copyright 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Slf4j
@RestController
@RequestMapping("/test")
@Listenable
public class StressTestController {

    @Autowired
    private FlowEapDao flowEapDao;

    @Autowired
    private FlowLotDao flowLotDao;

    @Autowired
    private EngineerDataCollectionInqController engineerDataCollectionInqController;

    @Autowired
    private CimJpaRepository cimJpaRepository;

    @PostMapping(value = "/init_data/req")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void initDBDataReq() {
        String processFlowID = "ST_Flow.01";
        String[] seqNoList = new String[]{
                "1000.100", "1000.200", "1000.300", "1000.400", "1000.500",
                "2000.100", "2000.200", "2000.300", "2000.400", "2000.500",
                "3000.100", "3000.200", "3000.300", "3000.400", "3000.500",
        };
        Random random = new Random(10000000);
        for (String seqNo : seqNoList) {
            String routeNo = seqNo.substring(0, seqNo.indexOf('.'));
            String stepNo = seqNo.substring(seqNo.indexOf('.') + 1);
            String firstRouteKey = routeNo.substring(0, 1);
            String firstStepKey = stepNo.substring(0, 1);
            for (int i = 1; i <= 10; i++) {
                String eqpID = String.format("ST%s%sEQP%02d", firstRouteKey, firstStepKey, i);
                FlowEapDO flowEapDO = new FlowEapDO();
                int randomInt = random.nextInt();
                String id = String.format("%05d", randomInt > 0 ? randomInt : -randomInt);
                flowEapDO.setId(String.format("SPFLOW_EAP.%s", id));
                flowEapDO.setEqpID(eqpID);
                flowEapDO.setFlowID(processFlowID);
                flowEapDO.setSequenceNumber(seqNo);
                flowEapDO.setIdleFlag(true);
                flowEapDao.save(flowEapDO);
            }
        }

        for (int i = 1; i <= 50; i++) {
            String lotID = String.format("NP0A%03d", i);
            FlowLotDO flowLotDO = new FlowLotDO();
            int randomInt = random.nextInt();
            String id = String.format("%05d", randomInt > 0 ? randomInt : -randomInt);
            flowLotDO.setId(String.format("SPFLOW_LOT.%s", id));
            flowLotDO.setLotID(lotID);
            flowLotDO.setIdleFlag(true);
            flowLotDO.setFlowID(processFlowID);
            flowLotDao.save(flowLotDO);
        }
    }


    @PostMapping(value = "/init_data_for_product/req")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Response initDBDataReqForProductForProcess(@RequestBody InitDataParameterIn param) {
        String productID = param.getProductID();
        String processFlowID = param.getProcessID();
        log.info("Product ID: {}", productID);
        log.info("Process ID: {}", processFlowID);

        // Get the OperationNo and EQP info by processID.
        String sql = "SELECT\n" +
                "    PF_PD.IDX_NO                      AS MAIN_SEQNO ,\n" +             // 0
                "    PF_POS.IDX_NO                     AS MODULE_SEQNO ,\n" +           // 1
                "    PF_PD.ROUTE_NO ||'.'|| POS.OPE_NO AS OPE_NO ,\n" +                 // 2
                "    MRCP.RECIPE_ID                     AS RECIPE_ID ,\n" +              // 3
                "    MRCP_EQP.EQP_ID                    AS EQP_ID\n" +                   // 4
                "FROM\n" +
                "    OMPRP MAINPD ,\n" +
                "    OMPRF MAIN_MODPF ,\n" +
                "    OMPRF_ROUTESEQ PF_PD ,\n" +
                "    OMPRP MODULEPD ,\n" +
                "    OMPRF MOUDLEPF ,\n" +
                "    OMPRF_PRSSSEQ PF_POS ,\n" +
                "    OMPRSS POS ,\n" +
                "    OMPRP STEPPD ,\n" +
                "    OMLRCP LRCP ,\n" +
                "    OMLRCP_DFLT LRCP_DSET ,\n" +
                "    OMRCP MRCP ,\n" +
                "    OMRCP_EQP MRCP_EQP\n" +
                "WHERE\n" +
                "    MAINPD.PRP_ID = ?1 \n" +
                "AND MAINPD.ACTIVE_MROUTE_PRF_RKEY = MAIN_MODPF.ID\n" +
                "AND MAIN_MODPF.ID = PF_PD.REFKEY\n" +
                "AND PF_PD.ROUTE_RKEY = MODULEPD.ID\n" +
                "AND MODULEPD.ACTIVE_PRF_RKEY = MOUDLEPF.ID\n" +
                "AND MOUDLEPF.ID = PF_POS.REFKEY\n" +
                "AND PF_POS.PRSS_RKEY = POS.ID\n" +
                "AND STEPPD.PRP_ID = POS.STEP_ID\n" +
                "AND STEPPD.LRCP_ID = LRCP.LRCP_ID\n" +
                "AND LRCP.ID = LRCP_DSET.REFKEY\n" +
                "AND LRCP_DSET.RECIPE_ID = MRCP.RECIPE_ID\n" +
                "AND MRCP_EQP.REFKEY = MRCP.ID\n" +
                "ORDER BY\n" +
                "    MAIN_SEQNO,\n" +
                "    MODULE_SEQNO,\n" +
                "    EQP_ID";

        log.info("Start init data.");
        log.info("");
        List<Object[]> stepNos = cimJpaRepository.query(sql, processFlowID);
        stepNos.forEach(data -> {
            log.info("------> FlowID: {} --- StepNO: {} --- Equipment: {} --- IdleFlag : true", processFlowID, data[2], data[4]);
            FlowEapDO flowEapDO = new FlowEapDO();
            String id = SnowflakeIDWorker.getInstance().generateId(FlowEapDO.class);
            flowEapDO.setId(id);
            flowEapDO.setFlowID(processFlowID);
            flowEapDO.setSequenceNumber(String.valueOf(data[2]));
            flowEapDO.setEqpID(String.valueOf(data[4]));
            flowEapDO.setIdleFlag(true);
            flowEapDao.save(flowEapDO);
        });

        sql = "SELECT\n" +
                "    PD.PRP_ID,\n" +            // 0
                "    LOT.LOT_ID\n" +           // 1
                "FROM\n" +
                "    OMPRODINFO PROD ,\n" +
                "    OMPRODINFO_PRP PROD_PD ,\n" +
                "    OMPRP PD ,\n" +
                "    OMLOT LOT\n" +
                "WHERE\n" +
                "    PROD.PROD_ID = ?1 \n" +
                "AND PROD.ID = PROD_PD.REFKEY\n" +
                "AND PD.PRP_ID = ?2 \n" +
                "AND PD.PRP_ID = PROD_PD.MAIN_PROCESS_ID\n" +
                "AND LOT.PROD_ID = PROD.PROD_ID\n" +
                "AND LOT.LOT_STATE = 'ACTIVE'\n" +
                "AND LOT.LOT_HOLD_STATE = 'NOTONHOLD'\n" +
                "AND LOT.LOT_PRODUCTION_STATE = 'INPRODUCTION'\n" +
                "AND LOT.ORIGINAL_LOT = '1'";

        List<Object[]> lotInfos = cimJpaRepository.query(sql, productID, processFlowID);
        log.info("");
        lotInfos.forEach(data -> {
            log.info("------> FlowID: {} --- Lot: {} --- IdleFlag : true", processFlowID, data[1]);
            FlowLotDO flowLotDO = new FlowLotDO();
            String id = SnowflakeIDWorker.getInstance().generateId(FlowLotDO.class);
            flowLotDO.setId(String.format("SPFLOW_LOT.%s", id));
            flowLotDO.setFlowID(processFlowID);
            flowLotDO.setLotID(String.valueOf(data[1]));
            flowLotDO.setIdleFlag(true);
            flowLotDao.save(flowLotDO);
        });
        log.info("");
        return Response.createSucc("0");
    }

    @PostMapping(value = "/get_idle_eap/req")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Response getIdleEqp(@RequestBody IdleEapParams param) {
        log.info("Process ID: {}", param.getFlowID());
        log.info("StepNO: {}", param.getSeqNo());
        log.info("Get idle eqp.");
        String sql = "SELECT\n" +
                "    SPFLOW_EAP.*\n" +
                "FROM\n" +
                "    SPFLOW_EAP,\n" +
                "    OMEQP\n" +
                "WHERE\n" +
                "    SPFLOW_EAP.FLOW_ID = ?1 \n" +
                "AND SPFLOW_EAP.SEQ_NO = ?2 \n" +
                "AND SPFLOW_EAP.IDLE_FLAG = '1' \n" +
                "AND OMEQP.E10_STATE_ID = 'SBY' \n" +
//                "AND FREQP.CUR_STATE_ID = 'WLOT'\n" +
                "AND OMEQP.EQP_ID = SPFLOW_EAP.EQP_ID \n" +
                "AND ROWNUM = '1' FOR UPDATE";

        FlowEapDO flowEapDO = cimJpaRepository.queryOne(sql, FlowEapDO.class, param.getFlowID(), param.getSeqNo());
        if (null == flowEapDO) {
            return Response.createSucc("0", null);
        }
        flowEapDO.setIdleFlag(false);
        flowEapDao.save(flowEapDO);
        return Response.createSucc("0", flowEapDO.getEqpID());
    }

    @PostMapping(value = "/make_eap_idle/req")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Response makeEapIdleReq(@RequestBody MakeEapIdleParams param) {
        log.info("Process ID: {} ,Step NO : {} ,EQP ID: {}", param.getFlowID(), param.getSeqNo(), param.getEqpID());
        log.info("Make eqp idle.");
        FlowEapDO flowEapDO;
        if (CimStringUtils.isEmpty(param.getSeqNo())) {
            String sql = "SELECT * FROM SPFLOW_EAP WHERE FLOW_ID = ?1 AND EQP_ID = ?2 FOR UPDATE";
            flowEapDO = cimJpaRepository.queryOne(sql, FlowEapDO.class,
                    param.getFlowID(),
                    param.getEqpID());
        } else {
            String sql = "SELECT * FROM SPFLOW_EAP WHERE FLOW_ID = ?1 AND SEQ_NO = ?2 AND EQP_ID = ?3 FOR UPDATE";
            flowEapDO = cimJpaRepository.queryOne(sql, FlowEapDO.class,
                    param.getFlowID(),
                    param.getSeqNo(),
                    param.getEqpID());
        }

        if (null != flowEapDO) {
            flowEapDO.setIdleFlag(true);
            flowEapDao.save(flowEapDO);
        }
        return Response.createSucc("0", flowEapDO);
    }

    @PostMapping(value = "/get_available_lot/req")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Response getAvailableLot(@RequestBody AvailableLotGetParams param) {
        log.info("Process ID: {}", param.getFlowID());
        log.info("Get the available lot.");
        String sql = "SELECT\n" +
                "    SPFLOW_LOT.*\n" +
                "FROM\n" +
                "    OMLOT,\n" +
                "    SPFLOW_LOT\n" +
                "WHERE\n" +
                "    OMLOT.MAIN_PROCESS_ID = ?1 \n" +
                "AND OMLOT.LOT_STATE = 'ACTIVE'\n" +
                // "AND OMLOT.LOT_HOLD_STATE = 'NOTONHOLD'\n" +
                "AND OMLOT.LOT_PRODUCTION_STATE = 'INPRODUCTION'\n" +
                "AND SPFLOW_LOT.LOT_ID = OMLOT.LOT_ID\n" +
                "AND SPFLOW_LOT.IDLE_FLAG = '1'\n" +
                "AND ROWNUM = '1' FOR UPDATE";
        FlowLotDO flowLotDO = cimJpaRepository.queryOne(sql, FlowLotDO.class, param.getFlowID());
        if (null == flowLotDO) {
            return Response.createSucc("0", null);
        }
        flowLotDO.setIdleFlag(false);
        flowLotDao.save(flowLotDO);
        return Response.createSucc("0", flowLotDO.getLotID());
    }

    /**
     * Gets one available lot for oms stress test with EAP.
     *
     * @param params params
     * @return an available
     */
    @PostMapping(value = "/get_one_lot_eap/inq")
    public Response getLotForEqp(@RequestBody AvailableLotForEapGetParams params) {
        log.info("EQP ID: {}", params.getEqpID());
        String sql = "SELECT\n" +
                "    B.LOT_ID\n" +
                "FROM\n" +
                "    OMLOT      A,\n" +
                "    SPFLOW_LOT B,\n" +
                "    OMLOT_EQP  C\n" +
                "WHERE\n" +
                "    A.LOT_ID = B.LOT_ID\n" +
                "AND A.LOT_STATE = 'ACTIVE'\n" +
                "AND A.LOT_PRODUCTION_ID = 'INPRODUCTION'\n" +
                "AND A.ID = C.REFKEY\n" +
                "AND C.EQP_ID = ?\n" +
                "AND ROWNUM = 1";
        final Object[] results = cimJpaRepository.queryOne(sql, params.getEqpID());
        if (null == results) {
            return Response.createSucc("0", null);
        }
        return Response.createSucc("0", results[0]);
    }

    @PostMapping(value = "/get_lots_has_edc/inq")
    public Response getLotsHasEDC() {
        String sql = "SELECT \n" +
                "    a.lot_id , \n" +
                "    a.ope_no \n" +
                "FROM \n" +
                "    omlot      a , \n" +
                "    spflow_lot b\n" +
                "WHERE\n" +
                "    a.lot_id = b.lot_id\n" +
                "AND a.ope_no IN ('2000.100', \n" +
                "                 '2000.500', \n" +
                "                 '3000.300', \n" +
                "                 '4000.100', \n" +
                "                 '4000.500')";

        final List<Object[]> results = cimJpaRepository.query(sql);
        if (null == results) {
            return Response.createSucc("0", null);
        }
        List<JSONObject> retVal = new ArrayList<>();
        for (Object[] result : results) {
            JSONObject jo = new JSONObject();
            jo.put("lotID", result[0]);
            jo.put("opeNo", result[1]);
            retVal.add(jo);
        }
        return Response.createSucc("0", retVal);
    }

    @PostMapping(value = "/make_lot_available/req")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Response makeLotAvailable(@RequestBody MakeLotAvailableParams param) {
        log.info("Process ID: {}", param.getFlowID());
        log.info("Lot ID: {}", param.getLotID());
        log.info("Make lot available.");
        String sql = "SELECT SPFLOW_LOT.* FROM SPFLOW_LOT WHERE FLOW_ID = ?1 AND LOT_ID = ?2 FOR UPDATE";
        FlowLotDO flowLotDO = cimJpaRepository.queryOne(sql, FlowLotDO.class, param.getFlowID(), param.getLotID());
        if (flowLotDO != null) {
            flowLotDO.setIdleFlag(true);
            flowLotDao.save(flowLotDO);
        }
        return Response.createSucc("0");
    }

    @PostMapping(value = "edc/spec_check_parameter_get/req")
    public Response specCheckParameterGetReq(@RequestBody Params.SpecCheckReqParams param) {
        Params.EDCDataItemWithTransitDataInqParams dataItemParam = new Params.EDCDataItemWithTransitDataInqParams();
        dataItemParam.setUser(param.getUser());
        dataItemParam.setControlJobID(param.getControlJobID());
        dataItemParam.setEquipmentID(param.getEquipmentID());

        Response response = engineerDataCollectionInqController.edcDataItemWithTransitDataInq(dataItemParam);
        Results.EDCDataItemWithTransitDataInqResult body = JSONObject.parseObject(JSONObject.toJSONString(response.getBody()), Results.EDCDataItemWithTransitDataInqResult.class);
        List<ObjectIdentifier> waferList = new ArrayList<>();
        if (null != body && CimArrayUtils.isNotEmpty(body.getStartCassetteList())) {
            for (Infos.StartCassette startCassette : body.getStartCassetteList()) {

                // get wafer list
                Infos.LotInCassette lotInCassette = startCassette.getLotInCassetteList().get(0);
                List<Infos.LotWafer> lotWaferList = lotInCassette.getLotWaferList();
                if (CimArrayUtils.isNotEmpty(lotWaferList)) {
                    for (Infos.LotWafer lotWafer : lotWaferList) {
                        waferList.add(lotWafer.getWaferID());
                    }
                }

                // get dc info
                List<Infos.DataCollectionInfo> dcDefList = lotInCassette.getStartRecipe().getDcDefList();
                if (CimArrayUtils.isNotEmpty(dcDefList)) {
                    Infos.DataCollectionInfo dataCollectionInfo = dcDefList.get(0);
                    List<Infos.DataCollectionItemInfo> dcItems = dataCollectionInfo.getDcItems();
                    if (CimArrayUtils.isNotEmpty(dcItems)) {
                        int size = CimArrayUtils.getSize(dcItems);
                        for (int i = 0; i < size; i++) {
                            Infos.DataCollectionItemInfo dataCollectionItemInfo = dcItems.get(i);
                            // set wafer id
                            dataCollectionItemInfo.setWaferID(waferList.get(i));

                            // set data value
                            if (CimStringUtils.equals("Integer", dataCollectionItemInfo.getDataType())) {
                                dataCollectionItemInfo.setDataValue("26");
                            } else if (CimStringUtils.equals("Float", dataCollectionItemInfo.getDataType())) {
                                dataCollectionItemInfo.setDataValue("26.0");
                            } else {
                                dataCollectionItemInfo.setDataValue("26.0");
                            }
                        }
                    }
                }
            }
        }
        return Response.createSucc(param.getUser().getFunctionID(), body);
    }


    // for stress test
    @Data
    public static class IdleEapParams {
        private User user;
        private String flowID;
        private String seqNo;
    }

    @Data
    public static class MakeEapIdleParams {
        private User user;
        private String flowID;
        private String eqpID;
        private String seqNo;
    }

    @Data
    public static class AvailableLotGetParams {
        private User user;
        private String flowID;
    }

    @Data
    public static class MakeLotAvailableParams {
        private String flowID;
        private String lotID;
    }

    @Data
    public static class InitDataParameterIn {
        private String productID;
        private String processID;
    }

    @Data
    public static class AvailableLotForEapGetParams {
        private User user;
        private String eqpID;
    }
}
