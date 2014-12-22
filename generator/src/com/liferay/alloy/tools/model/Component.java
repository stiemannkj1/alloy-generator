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

import com.liferay.alloy.util.PropsUtil;
import com.liferay.alloy.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

import jodd.typeconverter.Convert;

import jodd.util.StringPool;

import org.dom4j.Element;
public class Component extends BaseModel {

	public List<Event> getAfterEvents() {
		List<Event> afterEvents = new ArrayList<Event>();

		for (Event event : getEvents()) {
			if (event.isAfter()) {
				afterEvents.add(event);
			}
		}

		return afterEvents;
	}

	public String getAttributeNamespace() {
		StringBuilder sb = new StringBuilder();

		sb.append(_package);
		sb.append(StringPool.COLON);
		sb.append(getUncamelizedName("-"));
		sb.append(StringPool.COLON);

		return sb.toString().toLowerCase();
	}

	public List<Attribute> getAttributes() {
		return _attributes;
	}

	public List<Attribute> getAttributesAndEvents() {
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();

		attributes.addAll(_attributes);
		attributes.addAll(_events);

		return attributes;
	}

	public String[] getAuthors() {
		return _authors;
	}

	public String getCamelizedName() {
		return StringUtil.toCamelCase(
			getName(), true, StringPool.DASH.charAt(0));
	}

	public List<Event> getEvents() {
		return _events;
	}

	public String[] getExtendsTags() {
		return _extendsTags;
	}

	public String getInterface() {
		return _componentInterface;
	}

	public String getModule() {
		return _module;
	}

	public String getModuleString() {
		if (_module == null) {
			return "null";
		}
		else {
			return StringPool.QUOTE.concat(_module).concat(StringPool.QUOTE);
		}
	}

	public List<Event> getOnEvents() {
		List<Event> onEvents = new ArrayList<Event>();

		for (Event event : getEvents()) {
			if (event.isOn()) {
				onEvents.add(event);
			}
		}

		return onEvents;
	}

	public String getPackage() {
		return _package;
	}

	public String getParentClass() {
		return _parentClass;
	}

	public String getSafeName() {
		return StringUtil.replace(getName(), StringPool.DOT, StringPool.EMPTY);
	}

	public String getUncamelizedName() {
		return getUncamelizedName("");
	}

	public String getUncamelizedName(String delimiter) {
		final String PLACEHOLDER = "$";
		String name = getName().replace(".", PLACEHOLDER);
		name = StringUtil.fromCamelCase(name, PLACEHOLDER.charAt(0));

		return name.replace(PLACEHOLDER, delimiter);
	}

	public String getUncapitalizedName() {
		return StringUtil.uncapitalize(getCamelizedName());
	}

	public String getUnqualifiedParentClass() {
		return _parentClass.substring(
			_parentClass.lastIndexOf(StringPool.DOT) + 1);
	}

	public void initialize(Element componentElement, String defaultPackage) {
		String name = componentElement.attributeValue("name");
		setName(name);

		Element descriptionElement = componentElement.element("description");

		String description = StringPool.EMPTY;

		if (descriptionElement != null) {
			description = Convert.toString(descriptionElement.getText());
		}

		setDescription(description);

		Element authorsElement = componentElement.element("authors");

		if (authorsElement != null) {
			List<String> authors = new ArrayList<String>();
			List<Element> authorElementsList = authorsElement.elements(
				"author");

			for (Element authorElement : authorElementsList) {
				authors.add(authorElement.getText());
			}

			_authors = authors.toArray(new String[authors.size()]);
		}

		if (_authors == null) {
			_authors = _DEFAULT_AUTHORS;
		}

		_alloyComponent = Convert.toBoolean(
			componentElement.attributeValue("alloyComponent"), true);
		_bodyContent = Convert.toBoolean(
			componentElement.attributeValue("bodyContent"), false);
		_componentInterface = Convert.toString(
			componentElement.attributeValue("componentInterface"), null);
		String extendsTagsString = Convert.toString(
			componentElement.attributeValue("extendsTags"), null);

		if (extendsTagsString == null) {
			_extendsTags = null;
		}
		else {
			_extendsTags = extendsTagsString.split(StringPool.SPACE);
		}

		_module = Convert.toString(
			componentElement.attributeValue("module"), null);
		_package = Convert.toString(
			componentElement.attributeValue("package"), defaultPackage);
		_parentClass = Convert.toString(
			componentElement.attributeValue("parentClass"), null);
		boolean generateJava = Convert.toBoolean(
			componentElement.attributeValue("generateJava"), true);
		setGenerateJava(generateJava);

		Element attributesElement = componentElement.element("attributes");
		_attributes = new ArrayList<Attribute>();

		if (attributesElement != null) {
			List<Element> attributeElementsList = attributesElement.elements(
				"attribute");
			_attributes.addAll(
				getAttributesFromElements(attributeElementsList));
		}

		Element eventsElement = componentElement.element("events");
		_events = new ArrayList<Event>();

		if (eventsElement != null) {
			List<Element> eventElementsList = eventsElement.elements("event");
			_events.addAll(getEventsFromElements(eventElementsList));
		}
	}

	public boolean isAlloyComponent() {
		return _alloyComponent;
	}

	public boolean isBodyContent() {
		return _bodyContent;
	}

	public boolean isChildClassOf(String className) {
		try {
			String parentClassName = getParentClass();

			if (StringUtil.isNotEmpty(parentClassName)) {
				Class<?> parentClass = Class.forName(parentClassName);

				Class<?> clazz = Class.forName(className);

				return clazz.isAssignableFrom(parentClass);
			}
		}
		catch (Exception e) {
		}

		return false;
	}

	public void setAlloyComponent(boolean alloyComponent) {
		_alloyComponent = alloyComponent;
	}

	public void setAttributes(List<Attribute> attributes) {
		_attributes = attributes;

		for (Attribute attribute : attributes) {
			attribute.setComponent(this);
		}
	}

	public void setAuthors(String[] authors) {
		_authors = authors;
	}

	public void setBodyContent(boolean bodyContent) {
		_bodyContent = bodyContent;
	}

	public void setEvents(List<Event> events) {
		_events = events;

		for (Attribute event : events) {
			event.setComponent(this);
		}
	}

	public void setExtends(String _extends) {
		this._extends = _extends;
	}

	public void setExtendsTags(String[] _extendsTags) {
		this._extendsTags = _extendsTags;
	}

	public void setInterface(String componentInterface) {
		_componentInterface = componentInterface;
	}

	public void setModule(String type) {
		_module = type;
	}

	public void setPackage(String componentPackage) {
		_package = componentPackage;
	}

	public void setParentClass(String parentClass) {
		_parentClass = parentClass;
	}

	protected List<Attribute> getAttributesFromElements(
		List<Element> attributeElements) {

		List<Attribute> attributes = new ArrayList<Attribute>();

		for (Element attributeElement : attributeElements) {
			Attribute attribute = new Attribute();
			attribute.initialize(attributeElement, this);
			attributes.add(attribute);
		}

		return attributes;
	}

	protected List<Event> getEventsFromElements(List<Element> eventElements) {
		List<Event> events = new ArrayList<Event>();

		for (Element eventElement : eventElements) {
			Attribute attribute = new Attribute();
			attribute.initialize(eventElement, this);

			Event afterEvent = new Event(attribute, true);
			events.add(afterEvent);

			Event onEvent = new Event(attribute, false);
			events.add(onEvent);
		}

		return events;
	}

	private static final String[] _DEFAULT_AUTHORS = PropsUtil.getStringArray(
				"builder.authors");

	private boolean _alloyComponent;
	private List<Attribute> _attributes;
	private String[] _authors;
	private boolean _bodyContent;
	private String _componentInterface;
	private List<Event> _events;
	private String _extends;
	private String[] _extendsTags;
	private String _module;
	private String _package;
	private String _parentClass;

}