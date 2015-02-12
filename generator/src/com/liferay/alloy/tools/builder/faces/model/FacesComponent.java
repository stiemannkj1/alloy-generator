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

import java.util.ArrayList;
import java.util.List;

import jodd.typeconverter.Convert;

import jodd.util.StringPool;

import org.dom4j.Element;
public class FacesComponent extends Component {

	public String getHandlerClass() {
		return _handlerClass;
	}

	public String getRendererParentClass() {
		if (_rendererParentClass.contains("<")) {
			return _rendererParentClass.substring(
				0, _rendererParentClass.indexOf("<"));
		}
		else {
			return _rendererParentClass;
		}
	}

	public String getSince() {
		return _since;
	}

	public String getUnqualifiedRendererParentClass() {
		return _rendererParentClass.substring(
			getRendererParentClass().lastIndexOf(StringPool.DOT) + 1);
	}

	public String getValidatorId() {
		return _validatorId;
	}

	public String getYuiClassName() {
		return StringUtil.toCamelCase(
			_yuiName, true, StringPool.DASH.charAt(0));
	}

	public String getYuiName() {
		return _yuiName;
	}

	public void initialize(
		Element facesComponentElement, String defaultPackage,
		String defaultYUIRendererParentClass, String defaultSince) {

		super.initialize(facesComponentElement, defaultPackage);

		_generateTaglibXML = Convert.toBoolean(
			facesComponentElement.attributeValue("generateTaglibXML"), true);
		_handlerClassOnly = Convert.toBoolean(
			facesComponentElement.attributeValue("handlerClassOnly"), false);
		_since = Convert.toString(
			facesComponentElement.attributeValue("since"), defaultSince);
		_styleable = Convert.toBoolean(
			facesComponentElement.attributeValue("styleable"), true);
		_handlerClass = Convert.toString(
			facesComponentElement.attributeValue("handlerClass"), null);
		_yui = Convert.toBoolean(
			facesComponentElement.attributeValue("yui"), false);
		_yuiName = Convert.toString(
			facesComponentElement.attributeValue("yuiName"), getName());
		_validatorId = Convert.toString(
			facesComponentElement.attributeValue("validatorId"), null);

		boolean generateJava = Convert.toBoolean(
			facesComponentElement.attributeValue("generateJava"), true);
		setGenerateJava(generateJava);

		String parentClass = Convert.toString(
			facesComponentElement.attributeValue("parentClass"),
			_DEFAULT_COMPONENT_PARENT_CLASS);
		setParentClass(parentClass);

		String defaultRendererParentClass = _DEFAULT_RENDERER_BASE_CLASS;

		if (isYui() && (defaultYUIRendererParentClass != null) &&
			(defaultYUIRendererParentClass.length() > 0)) {

			defaultRendererParentClass = defaultYUIRendererParentClass;
		}

		_rendererParentClass = Convert.toString(
			facesComponentElement.attributeValue("rendererParentClass"),
			defaultRendererParentClass);
	}

	public boolean isGenerateTaglibXML() {
		return _generateTaglibXML;
	}

	public boolean isHandlerClassOnly() {
		return _handlerClassOnly;
	}

	public boolean isStyleable() {
		return _styleable;
	}

	public boolean isYui() {
		return _yui;
	}

	public void setGenerateTaglibXML(boolean _generateTaglibXML) {
		this._generateTaglibXML = _generateTaglibXML;
	}

	public void setHandlerClass(String _handlerClass) {
		this._handlerClass = _handlerClass;
	}

	public void setHandlerClassOnly(boolean _handlerClassOnly) {
		this._handlerClassOnly = _handlerClassOnly;
	}

	public void setRendererParentClass(String rendererParentClass) {
		_rendererParentClass = rendererParentClass;
	}

	public void setSince(String _since) {
		this._since = _since;
	}

	public void setStyleable(boolean _styleable) {
		this._styleable = _styleable;
	}

	public void setValidatorId(String _validatorId) {
		this._validatorId = _validatorId;
	}

	public void setYui(boolean _yui) {
		this._yui = _yui;
	}

	@Override
	protected List<Attribute> getAttributesFromElements(
		List<Element> attributeElements) {

		List<Attribute> attributes = new ArrayList<Attribute>();

		for (Element attributeElement : attributeElements) {
			FacesAttribute facesAttribute = new FacesAttribute();
			facesAttribute.initialize(attributeElement, this);

			boolean readOnly = facesAttribute.isReadOnly();

			if (!readOnly) {
				attributes.add(facesAttribute);
			}
		}

		return attributes;
	}

	private static final String _DEFAULT_COMPONENT_PARENT_CLASS =
		"javax.faces.component.UIComponentBase";

	private static final String _DEFAULT_RENDERER_BASE_CLASS =
		"javax.faces.render.Renderer";

	private boolean _generateTaglibXML;
	private String _handlerClass;
	private boolean _handlerClassOnly;
	private String _rendererParentClass;
	private String _since;
	private boolean _styleable;
	private String _validatorId;
	private boolean _yui;
	private String _yuiName;

}