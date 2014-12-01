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

package com.liferay.alloy.tools.model;

import com.liferay.alloy.util.ReservedAttributeUtil;
import com.liferay.alloy.util.StringUtil;
import com.liferay.alloy.util.TypeUtil;

import java.util.List;

import jodd.typeconverter.Convert;

import jodd.util.StringPool;

import org.apache.commons.lang.StringUtils;

import org.dom4j.Element;
public class Attribute extends BaseModel {

	public String getCapitalizedName() {
		return StringUtils.capitalize(getSafeName());
	}

	public Component getComponent() {
		return _component;
	}

	public String getConstantName() {
		return StringUtil.fromCamelCase(getSafeName(), '_').toUpperCase();
	}

	public String getDefaultValue() {
		return _defaultValue;
	}

	public String getGetterMethodPrefix() {
		String getterMethodPrefix = "get";

		if ("boolean".equals(_type)) {
			getterMethodPrefix = "is";
		}

		return getterMethodPrefix;
	}

	public String getJavaBeanPropertyName() {
		String javaBeanPropertyName = getSafeName();

		if ((javaBeanPropertyName.length() == 1) ||
			StringUtils.isAllLowerCase(
				getInferredNamePrefix(javaBeanPropertyName))) {

			javaBeanPropertyName = getCapitalizedName();
		}

		return javaBeanPropertyName;
	}

	public String getJavaSafeName() {
		return ReservedAttributeUtil.getJavaSafeName(this);
	}

	public String getJavaScriptType() {
		return _javaScriptType;
	}

	public String getJavaWrapperType() {
		String javaWrapperType = getType();
		javaWrapperType = TypeUtil.getInputJavaType(javaWrapperType, true);

		return TypeUtil.getJavaWrapperType(javaWrapperType);
	}

	public String getSafeName() {
		String safeName = getName();

		if ((getComponent() != null) && getComponent().isAlloyComponent()) {
			safeName = ReservedAttributeUtil.getSafeName(this);
		}

		if (safeName.indexOf(StringPool.COLON) > -1) {
			safeName = StringUtils.substringAfterLast(
				safeName, StringPool.COLON);
		}

		return safeName;
	}

	public String getType() {
		return _type;
	}

	public String getTypeSimpleClassName() {
		return getTypeSimpleClassName(_type);
	}

	public String getTypeSimpleClassName(String type) {
		if (TypeUtil.isPrimitiveType(type)) {
			return type;
		}
		else {
			try {
				return Class.forName(type).getSimpleName();
			}
			catch (ClassNotFoundException e) {
			}
		}

		return StringPool.EMPTY;
	}

	public void initialize(Element attributeElement, Component component) {
		setComponent(component);
		String description = attributeElement.elementText("description");
		setDescription(description);
		String name = attributeElement.elementText("name");
		setName(name);
		_defaultValue = attributeElement.elementText("defaultValue");
		_javaScriptType = Convert.toString(
			attributeElement.elementText("javaScriptType"), null);

		String defaultType = DEFAULT_TYPE;

		if (_javaScriptType != null) {
			defaultType = TypeUtil.getJavaScriptType(_javaScriptType);
			defaultType = TypeUtil.getInputJavaType(defaultType, true);
		}

		_type = Convert.toString(
			attributeElement.elementText("type"), defaultType);
		boolean generateJava = Convert.toBoolean(
			attributeElement.elementText("generateJava"), true);
		setGenerateJava(generateJava);

		_gettable = Convert.toBoolean(
			attributeElement.elementText("gettable"), true);
		_required = Convert.toBoolean(
			attributeElement.elementText("required"), false);
		_settable = Convert.toBoolean(
			attributeElement.elementText("settable"), true);
		_readOnly = Convert.toBoolean(
			attributeElement.elementText("readOnly"), false);
	}

	public boolean isEvent() {
		List<Event> events = _component.getEvents();

		return events.contains(this);
	}

	public boolean isGettable() {
		return _gettable;
	}

	public boolean isReadOnly() {
		return _readOnly;
	}

	public boolean isRequired() {
		return _required;
	}

	public boolean isSettable() {
		return _settable;
	}

	public void setComponent(Component component) {
		_component = component;
	}

	public void setDefaultValue(String defaultValue) {
		_defaultValue = defaultValue;
	}

	public void setGettable(boolean gettable) {
		_gettable = gettable;
	}

	public void setJavaScriptType(String javaScriptType) {
		_javaScriptType = javaScriptType;
	}

	public void setReadOnly(boolean readOnly) {
		_readOnly = readOnly;
	}

	public void setRequired(boolean required) {
		_required = required;
	}

	public void setSettable(boolean settable) {
		_settable = settable;
	}

	public void setType(String _type) {
		this._type = _type;
	}

	/**
	 * http://download.oracle.com/otn-pub/jcp/7224-javabeans-1.01-fr-spec-oth-JSpec/beans.101.pdf
	 * See section 8.8 (Capitalization of inferred names.)
	 */
	protected String getInferredNamePrefix(String name) {
		return name.substring(0, 2);
	}

	protected static final String DEFAULT_TYPE = "java.lang.Object";

	private Component _component;
	private String _defaultValue;
	private boolean _gettable = true;
	private String _javaScriptType;
	private boolean _readOnly;
	private boolean _required;
	private boolean _settable = true;
	private String _type;

}