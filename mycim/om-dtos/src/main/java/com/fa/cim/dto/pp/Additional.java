package com.fa.cim.dto.pp;

import com.fa.cim.common.support.ObjectIdentifier;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Additional {
    private final List<ObjectIdentifier> entityID;

    public Additional() {
        this.entityID = new ArrayList<>();
    }
}
