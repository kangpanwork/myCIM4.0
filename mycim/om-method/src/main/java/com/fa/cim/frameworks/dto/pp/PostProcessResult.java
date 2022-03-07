package com.fa.cim.frameworks.dto.pp;

import com.fa.cim.dto.Infos;
import com.fa.cim.frameworks.pprocess.api.definition.PostProcessPlanProxy;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * the post process execute result
 *
 * @author Yuri
 */
public class PostProcessResult {

    /**
     * the register result
     */
    @Getter
    @Setter
    public static class Register {
        private String taskId;
        private List<PostProcessTask> tasks;
        private Infos.ObjCommon objCommon;
    }

    @Getter
    @Setter
    public static class Execute {
        private boolean successful = true;
    }

}
