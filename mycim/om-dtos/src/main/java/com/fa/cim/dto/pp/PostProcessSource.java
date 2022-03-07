package com.fa.cim.dto.pp;

import com.fa.cim.common.support.ObjectIdentifier;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * this interface would be used for extract necessary informations from the input and out parameter
 *
 * @author Yuri
 */
public interface PostProcessSource {

    /**
     * extract lot IDs
     *
     * @return a list of extracted lot IDs
     */
    default List<ObjectIdentifier> lotIDs() {
        return Collections.emptyList();
    }

    /**
     * extract durable IDs
     *
     * @return a list of extracted durable IDs
     */
    default List<ObjectIdentifier> durableIDs() {
        return Collections.emptyList();
    }

    /**
     * extract equipment ID
     *
     * @return extracted equipment ID
     */
    default ObjectIdentifier equipmentID() {
        return ObjectIdentifier.emptyIdentifier();
    }

    /**
     * extract control job ID
     *
     * @return extracted control job ID
     */
    default ObjectIdentifier controlJobID() {
        return ObjectIdentifier.emptyIdentifier();
    }


    static PostProcessSource emptySource() {
        return EmptySource.INSTANCE;
    }

    enum EmptySource implements PostProcessSource {
        INSTANCE
    }
}
