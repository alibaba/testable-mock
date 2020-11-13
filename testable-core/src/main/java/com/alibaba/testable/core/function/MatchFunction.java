package com.alibaba.testable.core.function;

/**
 * @author flin
 */
public interface MatchFunction {

    /**
     * Judge whether real argument value match exception
     *
     * @param value real argument value when mock method invoked
     * @return match result
     */
    boolean check(Object value);

}
