package io.fireboom.utils;

import cn.hutool.core.util.StrUtil;
import io.fireboom.server.generated.ConfigurationVariable;

public class Variable {

    public static String loadString(ConfigurationVariable variable) {
        if (null == variable) {
            return StrUtil.EMPTY;
        }

        switch (variable.getKind()) {
            case STATIC_CONFIGURATION_VARIABLE:
                return variable.getStaticVariableContent();
            case PLACEHOLDER_CONFIGURATION_VARIABLE:
                return variable.getPlaceholderVariableName();
            case ENV_CONFIGURATION_VARIABLE:
                String value = System.getenv(variable.getEnvironmentVariableName());
                return StrUtil.firstNonEmpty(value, variable.getEnvironmentVariableDefaultValue());
            default:
                return StrUtil.EMPTY;
        }
    }
}
