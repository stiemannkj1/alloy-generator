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

package com.liferay.alloy.tools.builder.faces.model;

import com.liferay.alloy.tools.model.Attribute;
import com.liferay.alloy.tools.model.Component;
import com.liferay.alloy.util.StringUtil;
import com.liferay.alloy.util.TypeUtil;

import jodd.typeconverter.Convert;

import jodd.util.StringPool;

import org.dom4j.Element;
public class FacesAttribute extends Attribute {

	@Override
	public String getJavaWrapperType() {
		String javaWrapperType = TypeUtil.removeJavaPrefix(getType());

		if (javaWrapperType.equals("int")) {
			javaWrapperType = "Integer";
		}
		else if (!javaWrapperType.contains(StringPool.DOT) &&
				 !javaWrapperType.contains("[]")) {

			javaWrapperType = StringUtil.capitalize(javaWrapperType);
		}

		return javaWrapperType;
	}

	public String getMethodSignature() {
		return _methodSignature;
	}

	public String getUnprefixedType() {
		return TypeUtil.removeJavaPrefix(getType());
	}

	public String getYuiConstantName() {
		return StringUtil.fromCamelCase(_yuiName, '_').toUpperCase();
	}

	public String getYuiName() {
		return _yuiName;
	}

	public String getYuiType() {
		return _yuiType;
	}

	@Override
	public void initialize(Element facesAttributeElement, Component component) {
		super.initialize(facesAttributeElement, component);

		String defaultValue = Convert.toString(
			facesAttributeElement.elementText("defaultValue"), "null");

		setDefaultValue(defaultValue);

		String type = Convert.toString(
			facesAttributeElement.elementText("type"), DEFAULT_TYPE);
		_methodSignature = facesAttributeElement.elementText(
			"method-signature");

		if (_methodSignature != null && type.equals(DEFAULT_TYPE)) {
			type = "javax.el.MethodExpression";
		}

		setType(type);

		_inherited = Convert.toBoolean(
			facesAttributeElement.elementText("inherited"), false);
		_methodSignature = facesAttributeElement.elementText(
			"method-signature");
		_override = Convert.toBoolean(
			facesAttributeElement.elementText("override"), false);
		_yui = Convert.toBoolean(
			facesAttributeElement.elementText("yui"), false);
		_yuiName = Convert.toString(
			facesAttributeElement.elementText("yuiName"), getName());
		_yuiType = Convert.toString(
			facesAttributeElement.elementText("yuiType"), getJavaWrapperType());
	}

	public boolean isInherited() {
		return _inherited;
	}

	public boolean isOverride() {
		return _override;
	}

	public boolean isYui() {
		return _yui;
	}

	public void setInherited(boolean _inherited) {
		this._inherited = _inherited;
	}

	public void setMethodSignature(String methodSignature) {
		_methodSignature = methodSignature;
	}

	public void setOverride(boolean _override) {
		this._override = _override;
	}

	public void setYui(boolean _yui) {
		this._yui = _yui;
	}

	public void setYuiName(String _yuiName) {
		this._yuiName = _yuiName;
	}

	public void setYuiType(String _yuiType) {
		this._yuiType = _yuiType;
	}

	private boolean _inherited;
	private String _methodSignature;
	private boolean _override;
	private boolean _yui;
	private String _yuiName;
	private String _yuiType;

}