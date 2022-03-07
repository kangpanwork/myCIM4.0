package com.fa.cim.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import com.fa.cim.common.support.Response;
import com.fa.cim.dto.LotSamplingRuleCheckParam;
import com.fa.cim.sampling.AdvancedWaferSamplingResultParam;

import feign.hystrix.FallbackFactory;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@FeignClient(value = "oms-sampling-service",
        fallbackFactory = ISamplingFeign.ISamplingFeignService.class)
public interface ISamplingFeign {

    /**
     * description:  lot sampling check .skip / keep
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/6/24 0024 10:44                        YJ                Create
     *
     * @author YJ
     * @date 2021/6/24 0024 10:44
     * @param lotSamplingRuleCheckParam - lot sampling 执行check时请求参数。
     * @return result ---> skip / keep
     */
    @PostMapping(value = "/sampling/ls/lot_sampling_rule_check/req")
    Response lotSamplingCheck(LotSamplingRuleCheckParam lotSamplingRuleCheckParam);

    /**
     * description:  wafer sampling 进行wafer 加工抽样。
     * change history:
     * date             defect             person             comments
     * ---------------------------------------------------------------------------------------------------------------------
     * 2021/6/24 0024 10:44                        YJ                Create
     *
     * @author YJ
     * @date 2021/6/24 0024 10:44
     * @param advancedWaferSamplingResultParam - 请求抽样参数
     * @return result ---> 进行加工的wafer
     */
    @PostMapping(value = "/sampling/wsa/advanced_wafer_sampling/inq")
    Response advancedWaferSamplingCompile(AdvancedWaferSamplingResultParam advancedWaferSamplingResultParam);


    @Slf4j
    @AllArgsConstructor
    @NoArgsConstructor
    @Service
    class ISamplingFeignService implements ISamplingFeign, FallbackFactory<ISamplingFeign> {
        /**
         * throwable exception
         */
        private Throwable throwable;

        @Override
        public Response lotSamplingCheck(LotSamplingRuleCheckParam lotSamplingRuleCheckParam) {
            log.error("lotSampling .... check error : ", throwable);
            return Response.createError("LS20006", throwable.getMessage());
        }

        @Override
        public Response advancedWaferSamplingCompile(AdvancedWaferSamplingResultParam advancedWaferSamplingResultParam) {
            log.error("lotSampling .... check error : ", throwable);
            return Response.createError("SWSAQ003", throwable.getMessage());
        }


        @Override
        public ISamplingFeign create(Throwable throwable) {
            return new ISamplingFeignService(throwable);
        }
    }
}
