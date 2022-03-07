package com.fa.cim.pcs.annotations;

import com.fa.cim.common.constant.BizConstant;

import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PcsAPI {

    Scope[] value();

    enum Scope {
        PRE_1(BizConstant.SP_BRSCRIPT_PRE1),
        PRE_2(BizConstant.SP_BRSCRIPT_PRE2),
        POST(BizConstant.SP_BRSCRIPT_POST)
        ;
        private String value;

        Scope(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

}
