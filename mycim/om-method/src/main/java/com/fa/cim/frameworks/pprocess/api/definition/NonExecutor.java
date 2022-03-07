package com.fa.cim.frameworks.pprocess.api.definition;

import com.fa.cim.frameworks.dto.pp.PostProcessTask;
import com.fa.cim.frameworks.dto.pp.mode.AvailablePhase;
import com.fa.cim.frameworks.pprocess.api.annotations.PostProcessTaskHandler;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessExecutor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>NonExecutor .
 * change history:
 * date               defect            person            comments
 * -------------------------------------------------------------------------------------------------------------------
 * 2021/6/10 16:50    ********          ZQI               create file.
 *
 * @author ZQI
 * @version 1.0
 * @date 2021/6/10 16:50
 * @copyright 2021, FA Software (Shanghai) Co., Ltd. All Rights Reserved.
 * @since JDK1.8
 */
@Slf4j
@PostProcessTaskHandler(available = AvailablePhase.ALL)
public class NonExecutor implements PostProcessExecutor {

    @Override
    public PostProcessTask.Result doExecute(PostProcessTask.Param param) {
        log.info("PostProcess Executor: no action...");
        return PostProcessTask.success();
    }
}
