package com.exemple.service.resource.common.util;

import org.apache.commons.lang3.StringUtils;

public final class StringHelper {

    private StringHelper() {

    }

    public static String join(String root, String element, char separator) {

        return root.isEmpty() ? element : StringUtils.join(new String[] { root, element }, separator);
    }

}
