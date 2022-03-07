package com.fa.cim.pcs.engine;

import com.fa.cim.common.support.ObjectIdentifier;
import lombok.Data;

@Data
public class ScriptParam {

    private ObjectIdentifier lotId;
    private ObjectIdentifier equipmentId;
    private String phase;
    private String scriptName;
    private String script;

}
