/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.alloy.util;

import com.liferay.alloy.util.converter.NumberTypeConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import jodd.typeconverter.Convert;
import jodd.typeconverter.TypeConversionException;
import jodd.typeconverter.TypeConverterManager;

import jodd.util.StringPool;

import org.apache.commons.lang.StringUtils;

/**
 * @author Eduardo Lundgren
 * @author Bruno Basto
 */
public class DefaultValueUtil {

	public static String getDefaultValue(String className, String value) {
		String defaultValue = StringPool.EMPTY;

		_registerConverters();

		if (className.equals(ArrayList.class.getName()) ||
			className.equals(HashMap.class.getName()) ||
			className.equals(Object.class.getName()) ||
			className.equals(String.class.getName())) {

			if (!isValidStringValue(value)) {
				return defaultValue;
			}

			if (_EMPTY_STRINGS.contains(value)) {
				value = StringPool.EMPTY;
			}
			else if (className.equals(ArrayList.class.getName()) &&
					 !value.trim().startsWith(StringPool.LEFT_BRACKET)) {

				value = "[]";
			}
			else if (className.equals(HashMap.class.getName()) &&
					 !value.trim().startsWith(StringPool.LEFT_BRACE)) {

				value = "{}";
			}

			defaultValue = StringUtil.unquote(value);
		}
		else if (className.equals(boolean.class.getName()) ||
				 className.equals(Boolean.class.getName())) {

			if (StringUtil.isEmpty(value)) {
				defaultValue = String.valueOf(false);
			}
			else {
				try {
					defaultValue = String.valueOf(
						Convert.toBoolean(value.toLowerCase()));
				}
				catch (TypeConversionException tce) {
					defaultValue = String.valueOf(false);
				}
			}
		}
		else if (className.equals(int.class.getName()) ||
				 className.equals(Integer.class.getName())) {

			if (_INFINITY.contains(value)) {
				value = String.valueOf(Integer.MAX_VALUE);
			}

			defaultValue = String.valueOf(Convert.toInteger(value));
		}
		else if (className.equals(double.class.getName()) ||
				 className.equals(Double.class.getName())) {

			if (_INFINITY.contains(value)) {
				value = String.valueOf(Double.MAX_VALUE);
			}

			defaultValue = String.valueOf(Convert.toDouble(value));
		}
		else if (className.equals(float.class.getName()) ||
				 className.equals(Float.class.getName())) {

			if (_INFINITY.contains(value)) {
				value = String.valueOf(Float.MAX_VALUE);
			}

			defaultValue = String.valueOf(Convert.toFloat(value));
		}
		else if (className.equals(long.class.getName()) ||
				 className.equals(Long.class.getName())) {

			if (_INFINITY.contains(value)) {
				value = String.valueOf(Long.MAX_VALUE);
			}

			defaultValue = String.valueOf(Convert.toLong(value));
		}
		else if (className.equals(short.class.getName()) ||
				 className.equals(Short.class.getName())) {

			if (_INFINITY.contains(value)) {
				value = String.valueOf(Short.MAX_VALUE);
			}

			defaultValue = String.valueOf(Convert.toShort(value));
		}
		else if (className.equals(Number.class.getName())) {
			if (_INFINITY.contains(value)) {
				value = String.valueOf(Integer.MAX_VALUE);
			}

			defaultValue = String.valueOf(
				TypeConverterManager.convertType(value, Number.class));
		}

		return defaultValue;
	}

	public static boolean isValidStringValue(String value) {
		value = StringUtil.trimDown(Convert.toString(value, StringPool.EMPTY));

		if (StringUtil.isBlank(value)) {
			return false;
		}

		if (StringUtils.isAlpha(value) ||
			(!StringUtils.containsIgnoreCase(value, _GENERATED) &&
			 !StringUtils.isAlpha(value.substring(0, 1)) &&
			 !StringUtils.endsWith(value, StringPool.DOT))) {

			return true;
		}

		return false;
	}

	private static void _registerConverters() {
		TypeConverterManager.register(Number.class, new NumberTypeConverter());
	}

	private static final List<String> _EMPTY_STRINGS = Arrays.asList(
		"", "''", "\"\"", "(empty)", "empty", "EMPTY_STR", "undefined",
		"WidgetStdMod.BODY", "HTMLTextNode");

	private static final String _GENERATED = "generated";

	private static final List<String> _INFINITY = Arrays.asList(
		"infinity", "Infinity", "INFINITY");

}