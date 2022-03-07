package com.fa.cim.locktest;

import com.fa.cim.MycimApplication;
import com.fa.cim.common.utils.RequestJsonUtils;
import com.fa.cim.common.utils.SnowflakeIDWorker;
import com.fa.cim.controller.equipment.EquipmentController;
import com.fa.cim.controller.lot.LotController;
import com.fa.cim.dto.Params;
import com.fa.cim.entity.nonruntime.postprocess.pprocess.CimPostProcessPatternDefinitionDO;
import com.fa.cim.entity.nonruntime.postprocess.pprocess.CimPostProcessTransactionDefinitionDO;
import com.fa.cim.repository.standard.pprocess.PostProcessPatternDefinitionDao;
import com.fa.cim.service.equipment.impl.EquipmentProcessOperationImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * description:
 * <p>
 * change history:
 * date             defect#             person             comments
 * ---------------------------------------------------------------------------------------------------------------------
 * 2020/3/18          ********            Nyx                create file
 *
 * @author: Nyx
 * @date: 2020/3/18 13:27
 * @copyright: 2020, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MycimApplication.class)
@WebAppConfiguration
@Slf4j
public class LockTest {

    @Autowired
    private LockInfoTest lockInfoTest;

    @Autowired
    private LotController lotController;

    @Autowired
    private EquipmentController equipmentController;

    @Test
    public void test03() {
        String skipJson = "{\n" +
                "    \"currentOperationNumber\": \"9000.400\",\n" +
                "    \"currentRouteID\": {\n" +
                "        \"referenceKey\": \"OMPRP.454252511148705216\",\n" +
                "        \"value\": \"T-PROCESS.01\"\n" +
                "    },\n" +
                "    \"locateDirection\": false,\n" +
                "    \"lotID\": {\n" +
                "        \"referenceKey\": \"OMLOT.457119858893063616\",\n" +
                "        \"value\": \"NP000384.000\"\n" +
                "    },\n" +
                "    \"operationID\": {\n" +
                "        \"referenceKey\": \"OMPRP.454251440435168704\",\n" +
                "        \"value\": \"T-DUMMY.01\"\n" +
                "    },\n" +
                "    \"operationNumber\": \"9000.350\",\n" +
                "    \"processRef\": {\n" +
                "        \"mainProcessFlow\": \"OMPRF.454252521131148737\",\n" +
                "        \"moduleNumber\": \"9000\",\n" +
                "        \"modulePOS\": \"OMPRSS.457893081356240320\",\n" +
                "        \"moduleProcessFlow\": \"OMPRF.457893081012307392\",\n" +
                "        \"processFlow\": \"OMPRF.454252512205669824\",\n" +
                "        \"processOperationSpecification\": \"OMPRSS.457929334395045312\",\n" +
                "        \"siInfo\": null\n" +
                "    },\n" +
                "    \"routeID\": {\n" +
                "        \"referenceKey\": \"OMPRP.454252511148705216\",\n" +
                "        \"value\": \"T-PROCESS.01\"\n" +
                "    },\n" +
                "    \"seqno\": -1,\n" +
                "    \"sequenceNumber\": 0,\n" +
                "    \"user\": {\n" +
                "        \"userID\": {\n" +
                "            \"value\": \"DEV\",\n" +
                "            \"referenceKey\": \"\"\n" +
                "        },\n" +
                "        \"password\": \"96e79218965eb72c92a549dd5a330112\",\n" +
                "        \"functionID\": \"OLOTW003\"\n" +
                "    }\n" +
                "}";
        Params.SkipReqParams skipReqParams = RequestJsonUtils.toJavaObject(skipJson.getBytes(), Params.SkipReqParams.class);

        String loadJson = "{\n" +
                "    \"cassetteID\": {\n" +
                "        \"value\": \"T0009\",\n" +
                "        \"referenceKey\": \"\"\n" +
                "    },\n" +
                "    \"equipmentID\": {\n" +
                "        \"referenceKey\": \"OMEQP.453581270490351040\",\n" +
                "        \"value\": \"T-CVD01\"\n" +
                "    },\n" +
                "    \"portID\": {\n" +
                "        \"value\": \"P1\",\n" +
                "        \"referenceKey\": \"\"\n" +
                "    },\n" +
                "    \"opeMemo\": \"\",\n" +
                "    \"loadPurposeType\": \"Process Lot\",\n" +
                "    \"user\": {\n" +
                "        \"userID\": {\n" +
                "            \"value\": \"DEV\",\n" +
                "            \"referenceKey\": \"\"\n" +
                "        },\n" +
                "        \"password\": \"96e79218965eb72c92a549dd5a330112\",\n" +
                "        \"functionID\": \"OEQPR001\"\n" +
                "    }\n" +
                "}";
        Params.loadOrUnloadLotRptParams loadReqParam = RequestJsonUtils.toJavaObject(loadJson.getBytes(), Params.loadOrUnloadLotRptParams.class);

        String moveInJson = "{\n" +
                "    \"controlJobID\": null,\n" +
                "    \"equipmentID\": {\n" +
                "        \"referenceKey\": \"OMEQP.453581270490351040\",\n" +
                "        \"value\": \"T-CVD01\"\n" +
                "    },\n" +
                "    \"portGroupID\": \"PG1\",\n" +
                "    \"processJobLevelCtrl\": false,\n" +
                "    \"startCassetteList\": [\n" +
                "        {\n" +
                "            \"cassetteID\": {\n" +
                "                \"referenceKey\": \"OMCARRIER.457111242991535552\",\n" +
                "                \"value\": \"T0009\"\n" +
                "            },\n" +
                "            \"loadPortID\": {\n" +
                "                \"referenceKey\": \"OMPORT.453581271375349184\",\n" +
                "                \"value\": \"P1\"\n" +
                "            },\n" +
                "            \"loadPurposeType\": \"Process Lot\",\n" +
                "            \"loadSequenceNumber\": 1,\n" +
                "            \"lotInCassetteList\": [\n" +
                "                {\n" +
                "                    \"lotID\": {\n" +
                "                        \"referenceKey\": \"OMLOT.457119858893063616\",\n" +
                "                        \"value\": \"NP000384.000\"\n" +
                "                    },\n" +
                "                    \"lotType\": \"Production\",\n" +
                "                    \"lotWaferList\": [\n" +
                "                        {\n" +
                "                            \"aliasName\": \"\",\n" +
                "                            \"controlWaferFlag\": false,\n" +
                "                            \"parameterUpdateFlag\": false,\n" +
                "                            \"processJobExecFlag\": true,\n" +
                "                            \"processJobStatus\": \"\",\n" +
                "                            \"slotNumber\": 1,\n" +
                "                            \"startRecipeParameterList\": [],\n" +
                "                            \"waferID\": {\n" +
                "                                \"referenceKey\": \"OMWAFER.457119856468755904\",\n" +
                "                                \"value\": \"NP000384.000.25\"\n" +
                "                            }\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"aliasName\": \"\",\n" +
                "                            \"controlWaferFlag\": false,\n" +
                "                            \"parameterUpdateFlag\": false,\n" +
                "                            \"processJobExecFlag\": true,\n" +
                "                            \"processJobStatus\": \"\",\n" +
                "                            \"slotNumber\": 2,\n" +
                "                            \"startRecipeParameterList\": [],\n" +
                "                            \"waferID\": {\n" +
                "                                \"referenceKey\": \"OMWAFER.457119855969633728\",\n" +
                "                                \"value\": \"NP000384.000.24\"\n" +
                "                            }\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"aliasName\": \"\",\n" +
                "                            \"controlWaferFlag\": false,\n" +
                "                            \"parameterUpdateFlag\": false,\n" +
                "                            \"processJobExecFlag\": true,\n" +
                "                            \"processJobStatus\": \"\",\n" +
                "                            \"slotNumber\": 3,\n" +
                "                            \"startRecipeParameterList\": [],\n" +
                "                            \"waferID\": {\n" +
                "                                \"referenceKey\": \"OMWAFER.457119855441151424\",\n" +
                "                                \"value\": \"NP000384.000.23\"\n" +
                "                            }\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"aliasName\": \"\",\n" +
                "                            \"controlWaferFlag\": false,\n" +
                "                            \"parameterUpdateFlag\": false,\n" +
                "                            \"processJobExecFlag\": true,\n" +
                "                            \"processJobStatus\": \"\",\n" +
                "                            \"slotNumber\": 4,\n" +
                "                            \"startRecipeParameterList\": [],\n" +
                "                            \"waferID\": {\n" +
                "                                \"referenceKey\": \"OMWAFER.457119854925252032\",\n" +
                "                                \"value\": \"NP000384.000.22\"\n" +
                "                            }\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"aliasName\": \"\",\n" +
                "                            \"controlWaferFlag\": false,\n" +
                "                            \"parameterUpdateFlag\": false,\n" +
                "                            \"processJobExecFlag\": true,\n" +
                "                            \"processJobStatus\": \"\",\n" +
                "                            \"slotNumber\": 5,\n" +
                "                            \"startRecipeParameterList\": [],\n" +
                "                            \"waferID\": {\n" +
                "                                \"referenceKey\": \"OMWAFER.457119854413546944\",\n" +
                "                                \"value\": \"NP000384.000.21\"\n" +
                "                            }\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"aliasName\": \"\",\n" +
                "                            \"controlWaferFlag\": false,\n" +
                "                            \"parameterUpdateFlag\": false,\n" +
                "                            \"processJobExecFlag\": true,\n" +
                "                            \"processJobStatus\": \"\",\n" +
                "                            \"slotNumber\": 6,\n" +
                "                            \"startRecipeParameterList\": [],\n" +
                "                            \"waferID\": {\n" +
                "                                \"referenceKey\": \"OMWAFER.457119853910230464\",\n" +
                "                                \"value\": \"NP000384.000.20\"\n" +
                "                            }\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"aliasName\": \"\",\n" +
                "                            \"controlWaferFlag\": false,\n" +
                "                            \"parameterUpdateFlag\": false,\n" +
                "                            \"processJobExecFlag\": true,\n" +
                "                            \"processJobStatus\": \"\",\n" +
                "                            \"slotNumber\": 7,\n" +
                "                            \"startRecipeParameterList\": [],\n" +
                "                            \"waferID\": {\n" +
                "                                \"referenceKey\": \"OMWAFER.457119853385942464\",\n" +
                "                                \"value\": \"NP000384.000.19\"\n" +
                "                            }\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"aliasName\": \"\",\n" +
                "                            \"controlWaferFlag\": false,\n" +
                "                            \"parameterUpdateFlag\": false,\n" +
                "                            \"processJobExecFlag\": true,\n" +
                "                            \"processJobStatus\": \"\",\n" +
                "                            \"slotNumber\": 8,\n" +
                "                            \"startRecipeParameterList\": [],\n" +
                "                            \"waferID\": {\n" +
                "                                \"referenceKey\": \"OMWAFER.457119852870043072\",\n" +
                "                                \"value\": \"NP000384.000.18\"\n" +
                "                            }\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"aliasName\": \"\",\n" +
                "                            \"controlWaferFlag\": false,\n" +
                "                            \"parameterUpdateFlag\": false,\n" +
                "                            \"processJobExecFlag\": true,\n" +
                "                            \"processJobStatus\": \"\",\n" +
                "                            \"slotNumber\": 9,\n" +
                "                            \"startRecipeParameterList\": [],\n" +
                "                            \"waferID\": {\n" +
                "                                \"referenceKey\": \"OMWAFER.457119852358337984\",\n" +
                "                                \"value\": \"NP000384.000.17\"\n" +
                "                            }\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"aliasName\": \"\",\n" +
                "                            \"controlWaferFlag\": false,\n" +
                "                            \"parameterUpdateFlag\": false,\n" +
                "                            \"processJobExecFlag\": true,\n" +
                "                            \"processJobStatus\": \"\",\n" +
                "                            \"slotNumber\": 10,\n" +
                "                            \"startRecipeParameterList\": [],\n" +
                "                            \"waferID\": {\n" +
                "                                \"referenceKey\": \"OMWAFER.457119851846632896\",\n" +
                "                                \"value\": \"NP000384.000.16\"\n" +
                "                            }\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"aliasName\": \"\",\n" +
                "                            \"controlWaferFlag\": false,\n" +
                "                            \"parameterUpdateFlag\": false,\n" +
                "                            \"processJobExecFlag\": true,\n" +
                "                            \"processJobStatus\": \"\",\n" +
                "                            \"slotNumber\": 11,\n" +
                "                            \"startRecipeParameterList\": [],\n" +
                "                            \"waferID\": {\n" +
                "                                \"referenceKey\": \"OMWAFER.457119851313956288\",\n" +
                "                                \"value\": \"NP000384.000.15\"\n" +
                "                            }\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"aliasName\": \"\",\n" +
                "                            \"controlWaferFlag\": false,\n" +
                "                            \"parameterUpdateFlag\": false,\n" +
                "                            \"processJobExecFlag\": true,\n" +
                "                            \"processJobStatus\": \"\",\n" +
                "                            \"slotNumber\": 12,\n" +
                "                            \"startRecipeParameterList\": [],\n" +
                "                            \"waferID\": {\n" +
                "                                \"referenceKey\": \"OMWAFER.457119850810639808\",\n" +
                "                                \"value\": \"NP000384.000.14\"\n" +
                "                            }\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"aliasName\": \"\",\n" +
                "                            \"controlWaferFlag\": false,\n" +
                "                            \"parameterUpdateFlag\": false,\n" +
                "                            \"processJobExecFlag\": true,\n" +
                "                            \"processJobStatus\": \"\",\n" +
                "                            \"slotNumber\": 13,\n" +
                "                            \"startRecipeParameterList\": [],\n" +
                "                            \"waferID\": {\n" +
                "                                \"referenceKey\": \"OMWAFER.457119850286351808\",\n" +
                "                                \"value\": \"NP000384.000.13\"\n" +
                "                            }\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"aliasName\": \"\",\n" +
                "                            \"controlWaferFlag\": false,\n" +
                "                            \"parameterUpdateFlag\": false,\n" +
                "                            \"processJobExecFlag\": true,\n" +
                "                            \"processJobStatus\": \"\",\n" +
                "                            \"slotNumber\": 14,\n" +
                "                            \"startRecipeParameterList\": [],\n" +
                "                            \"waferID\": {\n" +
                "                                \"referenceKey\": \"OMWAFER.457119849762063808\",\n" +
                "                                \"value\": \"NP000384.000.12\"\n" +
                "                            }\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"aliasName\": \"\",\n" +
                "                            \"controlWaferFlag\": false,\n" +
                "                            \"parameterUpdateFlag\": false,\n" +
                "                            \"processJobExecFlag\": true,\n" +
                "                            \"processJobStatus\": \"\",\n" +
                "                            \"slotNumber\": 15,\n" +
                "                            \"startRecipeParameterList\": [],\n" +
                "                            \"waferID\": {\n" +
                "                                \"referenceKey\": \"OMWAFER.457119849267135936\",\n" +
                "                                \"value\": \"NP000384.000.11\"\n" +
                "                            }\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"aliasName\": \"\",\n" +
                "                            \"controlWaferFlag\": false,\n" +
                "                            \"parameterUpdateFlag\": false,\n" +
                "                            \"processJobExecFlag\": true,\n" +
                "                            \"processJobStatus\": \"\",\n" +
                "                            \"slotNumber\": 16,\n" +
                "                            \"startRecipeParameterList\": [],\n" +
                "                            \"waferID\": {\n" +
                "                                \"referenceKey\": \"OMWAFER.457119848747042240\",\n" +
                "                                \"value\": \"NP000384.000.10\"\n" +
                "                            }\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"aliasName\": \"\",\n" +
                "                            \"controlWaferFlag\": false,\n" +
                "                            \"parameterUpdateFlag\": false,\n" +
                "                            \"processJobExecFlag\": true,\n" +
                "                            \"processJobStatus\": \"\",\n" +
                "                            \"slotNumber\": 17,\n" +
                "                            \"startRecipeParameterList\": [],\n" +
                "                            \"waferID\": {\n" +
                "                                \"referenceKey\": \"OMWAFER.457119848277280192\",\n" +
                "                                \"value\": \"NP000384.000.09\"\n" +
                "                            }\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"aliasName\": \"\",\n" +
                "                            \"controlWaferFlag\": false,\n" +
                "                            \"parameterUpdateFlag\": false,\n" +
                "                            \"processJobExecFlag\": true,\n" +
                "                            \"processJobStatus\": \"\",\n" +
                "                            \"slotNumber\": 18,\n" +
                "                            \"startRecipeParameterList\": [],\n" +
                "                            \"waferID\": {\n" +
                "                                \"referenceKey\": \"OMWAFER.457119847799129536\",\n" +
                "                                \"value\": \"NP000384.000.08\"\n" +
                "                            }\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"aliasName\": \"\",\n" +
                "                            \"controlWaferFlag\": false,\n" +
                "                            \"parameterUpdateFlag\": false,\n" +
                "                            \"processJobExecFlag\": true,\n" +
                "                            \"processJobStatus\": \"\",\n" +
                "                            \"slotNumber\": 19,\n" +
                "                            \"startRecipeParameterList\": [],\n" +
                "                            \"waferID\": {\n" +
                "                                \"referenceKey\": \"OMWAFER.457119847325173184\",\n" +
                "                                \"value\": \"NP000384.000.07\"\n" +
                "                            }\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"aliasName\": \"\",\n" +
                "                            \"controlWaferFlag\": false,\n" +
                "                            \"parameterUpdateFlag\": false,\n" +
                "                            \"processJobExecFlag\": true,\n" +
                "                            \"processJobStatus\": \"\",\n" +
                "                            \"slotNumber\": 20,\n" +
                "                            \"startRecipeParameterList\": [],\n" +
                "                            \"waferID\": {\n" +
                "                                \"referenceKey\": \"OMWAFER.457119846834439616\",\n" +
                "                                \"value\": \"NP000384.000.06\"\n" +
                "                            }\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"aliasName\": \"\",\n" +
                "                            \"controlWaferFlag\": false,\n" +
                "                            \"parameterUpdateFlag\": false,\n" +
                "                            \"processJobExecFlag\": true,\n" +
                "                            \"processJobStatus\": \"\",\n" +
                "                            \"slotNumber\": 21,\n" +
                "                            \"startRecipeParameterList\": [],\n" +
                "                            \"waferID\": {\n" +
                "                                \"referenceKey\": \"OMWAFER.457119846356288960\",\n" +
                "                                \"value\": \"NP000384.000.05\"\n" +
                "                            }\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"aliasName\": \"\",\n" +
                "                            \"controlWaferFlag\": false,\n" +
                "                            \"parameterUpdateFlag\": false,\n" +
                "                            \"processJobExecFlag\": true,\n" +
                "                            \"processJobStatus\": \"\",\n" +
                "                            \"slotNumber\": 22,\n" +
                "                            \"startRecipeParameterList\": [],\n" +
                "                            \"waferID\": {\n" +
                "                                \"referenceKey\": \"OMWAFER.457119845852972480\",\n" +
                "                                \"value\": \"NP000384.000.04\"\n" +
                "                            }\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"aliasName\": \"\",\n" +
                "                            \"controlWaferFlag\": false,\n" +
                "                            \"parameterUpdateFlag\": false,\n" +
                "                            \"processJobExecFlag\": true,\n" +
                "                            \"processJobStatus\": \"\",\n" +
                "                            \"slotNumber\": 23,\n" +
                "                            \"startRecipeParameterList\": [],\n" +
                "                            \"waferID\": {\n" +
                "                                \"referenceKey\": \"OMWAFER.457119845353850304\",\n" +
                "                                \"value\": \"NP000384.000.03\"\n" +
                "                            }\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"aliasName\": \"\",\n" +
                "                            \"controlWaferFlag\": false,\n" +
                "                            \"parameterUpdateFlag\": false,\n" +
                "                            \"processJobExecFlag\": true,\n" +
                "                            \"processJobStatus\": \"\",\n" +
                "                            \"slotNumber\": 24,\n" +
                "                            \"startRecipeParameterList\": [],\n" +
                "                            \"waferID\": {\n" +
                "                                \"referenceKey\": \"OMWAFER.457119844863116736\",\n" +
                "                                \"value\": \"NP000384.000.02\"\n" +
                "                            }\n" +
                "                        },\n" +
                "                        {\n" +
                "                            \"aliasName\": \"\",\n" +
                "                            \"controlWaferFlag\": false,\n" +
                "                            \"parameterUpdateFlag\": false,\n" +
                "                            \"processJobExecFlag\": true,\n" +
                "                            \"processJobStatus\": \"\",\n" +
                "                            \"slotNumber\": 25,\n" +
                "                            \"startRecipeParameterList\": [],\n" +
                "                            \"waferID\": {\n" +
                "                                \"referenceKey\": \"OMWAFER.457119844284302784\",\n" +
                "                                \"value\": \"NP000384.000.01\"\n" +
                "                            }\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"monitorLotFlag\": false,\n" +
                "                    \"moveInFlag\": true,\n" +
                "                    \"productGroupID\": null,\n" +
                "                    \"productID\": {\n" +
                "                        \"referenceKey\": \"OMPRODINFO.454258162386142656\",\n" +
                "                        \"value\": \"T-PRD01.01\"\n" +
                "                    },\n" +
                "                    \"recipeParameterChangeType\": \"RecipeParmChangeByLot\",\n" +
                "                    \"startOperationInfo\": {\n" +
                "                        \"maskLevel\": \"\",\n" +
                "                        \"operationID\": {\n" +
                "                            \"referenceKey\": \"OMPRP.454251440435168704\",\n" +
                "                            \"value\": \"T-DUMMY.01\"\n" +
                "                        },\n" +
                "                        \"operationNumber\": \"9000.350\",\n" +
                "                        \"passCount\": 2,\n" +
                "                        \"processFlowID\": {\n" +
                "                            \"referenceKey\": \"OMPRP.454252511148705216\",\n" +
                "                            \"value\": \"T-PROCESS.01\"\n" +
                "                        }\n" +
                "                    },\n" +
                "                    \"startRecipe\": {\n" +
                "                        \"dataCollectionFlag\": false,\n" +
                "                        \"dcDefList\": [],\n" +
                "                        \"logicalRecipeID\": {\n" +
                "                            \"referenceKey\": \"OMLRCP.454247039003854272\",\n" +
                "                            \"value\": \"T-CVD01.01\"\n" +
                "                        },\n" +
                "                        \"machineRecipeID\": {\n" +
                "                            \"referenceKey\": \"OMRCP.453597638572967360\",\n" +
                "                            \"value\": \"T.T-CVD01.01\"\n" +
                "                        },\n" +
                "                        \"physicalRecipeID\": \"T-CVD01\",\n" +
                "                        \"startFixtureList\": [],\n" +
                "                        \"startReticleList\": []\n" +
                "                    },\n" +
                "                    \"subLotType\": \"Production\",\n" +
                "                    \"technologyID\": null\n" +
                "                }\n" +
                "            ],\n" +
                "            \"unloadPortID\": {\n" +
                "                \"referenceKey\": \"OMPORT.453581271375349184\",\n" +
                "                \"value\": \"P1\"\n" +
                "            }\n" +
                "        }\n" +
                "    ],\n" +
                "    \"processJobPauseFlag\": false,\n" +
                "    \"user\": {\n" +
                "        \"userID\": {\n" +
                "            \"value\": \"DEV\",\n" +
                "            \"referenceKey\": \"\"\n" +
                "        },\n" +
                "        \"password\": \"96e79218965eb72c92a549dd5a330112\",\n" +
                "        \"functionID\": \"OEQPW005\"\n" +
                "    }\n" +
                "}";
        Params.MoveInReqParams moveInReqParams = RequestJsonUtils.toJavaObject(moveInJson.getBytes(), Params.MoveInReqParams.class);
        lotController.skipReq(skipReqParams);
        equipmentController.carrierLoadingRpt(loadReqParam);
        equipmentController.moveInReq(moveInReqParams);
    }

    @Test
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Rollback
    public void test01() {
        int threadCount = 10;
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                log.info("----------------------------objectLockForEquipmentResource start .................." + Thread.currentThread().getName());
                lockInfoTest.objectLockForEquipmentResourceTest();
                log.info("----------------------------objectLockForEquipmentResource end .................." + Thread.currentThread().getName());
                countDownLatch.countDown();
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
        executorService.shutdown();
    }

    @Autowired
    private PostProcessPatternDefinitionDao patternDefinitionDao;

    @Test
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Rollback(false)
    public void test02() {
        List<CimPostProcessPatternDefinitionDO> list = new ArrayList<>(13);
        for (int i = 0; i < 13; i++) {
            CimPostProcessPatternDefinitionDO data = new CimPostProcessPatternDefinitionDO();
            data.setId(SnowflakeIDWorker.getInstance().generateId(CimPostProcessTransactionDefinitionDO.class));
            data.setPatternId("undefined");
            list.add(data);
        }
        patternDefinitionDao.saveAll(list);
    }
}