package com.fa.cim.post.controller;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.pp.PostProcessSource;
import com.fa.cim.frameworks.pprocess.api.annotations.EnablePostProcess;
import org.springframework.stereotype.Controller;

@Controller
public class SimControllerForPostProcessTest {

    @EnablePostProcess
    public Response simNonLotController(NonLotParam nonLotParam) {
        return null;
    }

    public Response simLotController(LotParam lotParam) {
        return null;
    }

    public static class LotParam implements PostProcessSource {

    }

    public static class NonLotParam implements PostProcessSource {

    }

}
