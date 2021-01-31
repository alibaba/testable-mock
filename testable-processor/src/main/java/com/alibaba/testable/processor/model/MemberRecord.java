package com.alibaba.testable.processor.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author flin
 */
public class MemberRecord {

    /**
     * Record private and final fields
     */
    public final List<String> privateOrFinalFields = new ArrayList<String>();
    /**
     * Record non-private fields
     */
    public final List<String> nonPrivateNorFinalFields = new ArrayList<String>();
    /**
     * Record private methods and possible parameter counts (negative number means large or equals)
     */
    public final Map<String, List<Integer>> privateMethods = new HashMap<String, List<Integer>>();
    /**
     * Record non-private methods and possible parameter counts (negative number means large or equals)
     */
    public final Map<String, List<Integer>> nonPrivateMethods = new HashMap<String, List<Integer>>();

}
