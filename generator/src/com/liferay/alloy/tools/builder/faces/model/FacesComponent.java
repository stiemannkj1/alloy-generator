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

import java.util.ArrayList;
import java.util.List;

import jodd.util.StringPool;

import com.liferay.alloy.tools.model.Attribute;
import com.liferay.alloy.tools.model.Component;
import com.liferay.alloy.util.PropsUtil;
import com.liferay.alloy.util.StringUtil;
import jodd.typeconverter.Convert;
import org.dom4j.Element;

public class FacesComponent extends Component {

	public String getRendererBaseClass() {

		String rendererBaseClass = getRendererParentClass();

		if (isYui()) {
			rendererBaseClass = _getDefaultAlloyRendererBaseClass();
		}

		return rendererBaseClass;
	}

	public String getRendererParentClass() {
		return _rendererParentClass;
	}

	public String getUnqualifiedRendererBaseClass() {
		return getRendererBaseClass().substring(
			getRendererBaseClass().lastIndexOf(StringPool.DOT) + 1);
	}

	public String getUnqualifiedRendererParentClass() {
		return getRendererParentClass().substring(
			getRendererParentClass().lastIndexOf(StringPool.DOT) + 1);
	}

	@Override
	public void initialize(Element facesComponentElement,
			String defaultPackage) {
		super.initialize(facesComponentElement, defaultPackage);

		_generateTaglibXML = Convert.toBoolean(
			facesComponentElement.attributeValue("generateTaglibXML"), true);
		_styleable = Convert.toBoolean(
			facesComponentElement.attributeValue("styleable"), true);
		_tagHandlerClass = Convert.toString(
			facesComponentElement.attributeValue("tagHandlerClass"), null);
		_yui = Convert.toBoolean(
			facesComponentElement.attributeValue("yui"), false);
		_yuiName = Convert.toString(
			facesComponentElement.attributeValue("yuiName"), getName());

		boolean generateJava = Convert.toBoolean(
			facesComponentElement.attributeValue("generateJava"), true);
		setGenerateJava(generateJava);

		String parentClass = Convert.toString(
			facesComponentElement.attributeValue("parentClass"),
			_COMPONENT_DEFAULT_PARENT_CLASS);
		setParentClass(parentClass);

		String defaultRendererParentClass = _DEFAULT_RENDERER_BASE_CLASS;

		if (isYui() && _DEFAULT_ALLOY_RENDERER_PARENT_CLASS != null
				&& _DEFAULT_ALLOY_RENDERER_PARENT_CLASS.length() > 0) {
			defaultRendererParentClass = _DEFAULT_ALLOY_RENDERER_PARENT_CLASS;
		}

		_rendererParentClass = Convert.toString(
			facesComponentElement.attributeValue("rendererParentClass"),
			defaultRendererParentClass);
	}

	public boolean isStyleable() {
		return _styleable;
	}

	public void setRendererParentClass(String rendererParentClass) {
		_rendererParentClass = rendererParentClass;
	}

	public void setStyleable(boolean _styleable) {
		this._styleable = _styleable;
	}

	@Override
	protected List<Attribute> getAttributesFromElements(List<Element> attributeElements) {

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

	private String _getDefaultAlloyRendererBaseClass() {
		StringBuilder sb = new StringBuilder(4);

		sb.append(getPackage());
		sb.append(StringPool.DOT);
		sb.append(getCamelizedName());
		sb.append("RendererBase");

		return sb.toString();
	}

	private static final String _COMPONENT_DEFAULT_PARENT_CLASS =
		PropsUtil.getString("builder.faces.component.default.parent.class");

	private static final String _DEFAULT_ALLOY_RENDERER_PARENT_CLASS =
		PropsUtil.getString("builder.faces.default.alloy.renderer.parent.class");

	private static final String _DEFAULT_RENDERER_BASE_CLASS =
		"javax.faces.render.Renderer";

	public boolean isGenerateTaglibXML() {
		return _generateTaglibXML;
	}

	public void setGenerateTaglibXML(boolean _generateTaglibXML) {
		this._generateTaglibXML = _generateTaglibXML;
	}

	public String getYuiName() {
		return _yuiName;
	}

	public String getYuiClassName() {
		return StringUtil.toCamelCase(
			_yuiName, true, StringPool.DASH.charAt(0));
	}

	public boolean isYui() {
		return _yui;
	}

	public void setYui(boolean _yui) {
		this._yui = _yui;
	}

	public String getTagHandlerClass() {
		return _tagHandlerClass;
	}

	public void setTagHandlerClass(String _tagHandlerClass) {
		this._tagHandlerClass = _tagHandlerClass;
	}

	private boolean _generateTaglibXML;
	private boolean _styleable;
	private String _tagHandlerClass;
	private String _yuiName;
	private String _rendererParentClass;
	private boolean _yui;
}
