package com.github.foreignkey;


import com.github.foreignkey.exception.ConfigException;

/**
 * 外键约束行为
 *
 * @author madokast
 * @version 1.0
 */

public enum ConstraintAction {

    RESTRICT, CASCADE, SET_NULL, NO_ACTION, SET_DEFAULT;

    public static ConstraintAction DEFAULT_ACTION = NO_ACTION;

    public static ConstraintAction resolve(String action) {
        if (action == null) return DEFAULT_ACTION;
        if (action.equalsIgnoreCase("RESTRICT")) return RESTRICT;
        if (action.equalsIgnoreCase("CASCADE")) return CASCADE;
        if (action.equalsIgnoreCase("SET-NULL")) return SET_NULL;
        if (action.equalsIgnoreCase("NO-ACTION")) return NO_ACTION;
        if (action.equalsIgnoreCase("SET-DEFAULT")) return SET_DEFAULT;

        throw new ConfigException(action + " is not a validated constraint action");
    }
}
