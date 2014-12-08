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

package com.liferay.alloy.tools.builder.base;

import com.liferay.alloy.tools.model.Component;
import com.liferay.alloy.util.FreeMarkerUtil;
import com.liferay.alloy.util.PropsUtil;
import com.liferay.alloy.util.StringUtil;
import com.liferay.alloy.util.xml.SAXReaderUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jodd.io.FileUtil;

import jodd.typeconverter.Convert;

import jodd.util.StringPool;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;

import org.xml.sax.InputSource;

/**
 * @author Eduardo Lundgren
 * @author Bruno Basto
 */
public abstract class BaseBuilder {

	public abstract void build() throws Exception;

	public List<Document> getComponentDefinitionDocs() {
		if (_componentsDefinitionDocs == null) {
			_componentsDefinitionDocs = new ArrayList<Document>();

			for (String componentsXML : getComponentDefinitionsList()) {
				File componentsFile = new File(componentsXML);

				if (componentsFile.exists()) {
					try {
						Document document = SAXReaderUtil.read(componentsFile);

						_componentsDefinitionDocs.add(
							_getExtendedDocument(document));
					}
					catch (DocumentException e) {
						e.printStackTrace();
					}
				}
				else {
					System.out.println(
						"Couldn't load components definitions from '" +
						componentsFile.getAbsolutePath() + "'.");
				}
			}
		}

		return _componentsDefinitionDocs;
	}

	public List<String> getComponentDefinitionsList() {
		if (_componentsDefinitionList == null) {
			String componentsDefinitionList = PropsUtil.getString(
				"builder.components.definitions");

			_componentsDefinitionList = Arrays.asList(
				componentsDefinitionList.split(StringPool.COMMA));
		}

		return _componentsDefinitionList;
	}

	public String getCopyrightYear() {
		if (_copyrightYear == null) {
			_copyrightYear = System.getProperty("builder.copyright.year");

			if (StringUtil.isBlank(_copyrightYear)) {
				Calendar calendar = Calendar.getInstance();

				_copyrightYear = String.valueOf(calendar.get(Calendar.YEAR));
			}
		}

		return _copyrightYear;
	}

	public abstract String getTemplatesDir();

	protected List<Component> getAllComponents() throws Exception {
		DocumentFactory factory = SAXReaderUtil.getDocumentFactory();

		Document doc = factory.createDocument();

		String taglibsXML = "<components></components>";

		Document taglibsDoc = SAXReaderUtil.read(
			new InputSource(
				new ByteArrayInputStream(taglibsXML.getBytes("utf-8"))));

		Element root = taglibsDoc.getRootElement();

		for (Document currentDoc : getComponentDefinitionDocs()) {
			currentDoc = _getExtendedDocument(currentDoc);

			Element currentRoot = currentDoc.getRootElement();

			String defaultPackage = currentRoot.attributeValue("short-name");
			List<Element> extComponentNodes = currentRoot.elements("component");

			for (Element extComponent : extComponentNodes) {
				String extComponentPackage = Convert.toString(
					extComponent.attributeValue("package"), defaultPackage);

				extComponent.addAttribute("package", extComponentPackage);
			}

			Element authors = currentRoot.element("author");

			List<Element> components = currentRoot.elements("component");

			for (Element component : components) {
				Element copy = component.createCopy();
				Element componentAuthors = copy.element("authors");

				if ((authors != null) && (componentAuthors == null)) {
					copy.add(authors.createCopy());
				}

				root.add(copy);
			}

			List<org.dom4j.Attribute> attributes = currentRoot.attributes();

			for (org.dom4j.Attribute attribute : attributes) {
				root.addAttribute(attribute.getName(), attribute.getValue());
			}
		}

		doc.add(root.createCopy());

		return getComponents(doc);
	}

	protected Element getComponentNode(Document doc, String name) {
		List<Element> components = doc.getRootElement().elements(_COMPONENT);

		for (Element component : components) {
			if (component.attributeValue("name").equals(name)) {
				return component;
			}
		}

		return null;
	}

	protected abstract List<Component> getComponents(Document doc)
		throws Exception;

	protected Document getComponentsDocByShortName(String name) {
		for (Document doc : getComponentDefinitionDocs()) {
			Element root = doc.getRootElement();

			if (root.attributeValue("short-name").equals(name)) {
				return doc;
			}
		}

		return null;
	}

	protected Map<String, Object> getDefaultTemplateContext() {
		Map<String, Object> context = new HashMap<String, Object>();

		context.put("copyrightYear", getCopyrightYear());

		return context;
	}

	protected Element getElementByName(List<Element> elements, String name) {
		for (Element element : elements) {
			if (name.equals(element.elementText("name"))) {
				return element;
			}
		}

		return null;
	}

	protected Map<String, Object> getTemplateContext(Component component) {
		Map<String, Object> context = getDefaultTemplateContext();

		context.put("component", component);
		context.put("namespace", component.getAttributeNamespace());

		return context;
	}

	protected Document mergeXMLAttributes(Document doc1, Document doc2) {
		Element doc2Root = doc2.getRootElement();
		Element doc1Root = doc1.getRootElement();
		Element docRoot = doc2Root.createCopy();

		docRoot.clearContent();

		if (doc1Root != null) {
			Iterator<Object> attributesIterator = doc1Root.attributeIterator();

			while (attributesIterator.hasNext()) {
				org.dom4j.Attribute attribute =
					(org.dom4j.Attribute)attributesIterator.next();

				if (attribute.getName().equals("extends")) {
					continue;
				}

				docRoot.addAttribute(attribute.getName(), attribute.getValue());
			}

			Element descriptionElement = doc1Root.element("description");

			if (descriptionElement != null) {
				docRoot.add(descriptionElement.createCopy());
			}
		}

		DocumentFactory factory = SAXReaderUtil.getDocumentFactory();

		Document doc = factory.createDocument();
		doc.setRootElement(docRoot);

		List<Element> doc2Components = doc2Root.elements(_COMPONENT);

		for (Element doc2Component : doc2Components) {
			Element component = doc2Component.createCopy();

			String name = doc2Component.attributeValue("name");

			Element doc1Component = getComponentNode(doc1, name);

			if (doc1Component != null) {
				Element doc1ComponentDescriptionElement = doc1Component.element(
					"description");

				if (doc1ComponentDescriptionElement != null) {
					Element descriptionElement = component.element(
						"description");

					if (descriptionElement != null) {
						component.remove(descriptionElement);
					}

					component.add(doc1ComponentDescriptionElement.createCopy());
				}

				Iterator<Object> attributesIterator =
					doc1Component.attributeIterator();

				while (attributesIterator.hasNext()) {
					org.dom4j.Attribute attribute =
						(org.dom4j.Attribute)attributesIterator.next();

					component.addAttribute(
						attribute.getName(), attribute.getValue());
				}

				Element doc1AttributesNode = doc1Component.element(_ATTRIBUTES);

				Element attributesNode = component.element(_ATTRIBUTES);

				if ((doc1AttributesNode != null) && (attributesNode != null)) {
					List<Element> doc1Attributes = doc1AttributesNode.elements(
						_ATTRIBUTE);

					List<Element> attributes = attributesNode.elements(
						_ATTRIBUTE);

					for (Element doc1Attribute : doc1Attributes) {
						Element attribute = getElementByName(
							attributes, doc1Attribute.elementText("name"));

						if (attribute != null) {
							attributesNode.remove(attribute);
						}

						attributesNode.add(doc1Attribute.createCopy());
					}
				}

				Element doc1EventsNode = doc1Component.element(_EVENTS);

				Element eventsNode = component.element(_EVENTS);

				if ((doc1EventsNode != null) && (eventsNode != null)) {
					List<Element> doc1Events = doc1EventsNode.elements(_EVENT);

					List<Element> events = eventsNode.elements(_EVENT);

					for (Element doc1Event : doc1Events) {
						Element event = getElementByName(
							events, doc1Event.elementText("name"));

						if (event != null) {
							eventsNode.add(event);
						}

						eventsNode.add(doc1Event.createCopy());
					}
				}
			}

			doc.getRootElement().add(component);
		}

		if (doc1Root != null) {
			List<Element> doc1Components = doc1Root.elements(_COMPONENT);

			for (Element doc1Component : doc1Components) {
				Element component = doc1Component.createCopy();

				String name = doc1Component.attributeValue("name");

				Element doc2Component = getComponentNode(doc2, name);

				if (doc2Component == null) {
					doc.getRootElement().add(component);
				}
			}
		}

		return doc;
	}

	protected String processTemplate(String name, Map<String, Object> context)
		throws Exception {

		return StringUtil.replace(
			FreeMarkerUtil.process(name, context), "\r", StringPool.EMPTY);
	}

	protected void writeFile(File file, String content) {
		writeFile(file, content, true);
	}

	protected void writeFile(File file, String content, boolean overwrite) {
		try {
			file.getParentFile().mkdirs();

			if (overwrite || !file.exists()) {
				String oldContent = StringPool.EMPTY;

				if (file.exists()) {
					oldContent = FileUtil.readString(file);
				}

				if (!file.exists() || !content.equals(oldContent)) {
					System.out.println("Writing " + file);

					FileUtil.writeString(file, content);
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Document _getExtendedDocument(Document document) {
		String parentXMLPath = document.getRootElement().attributeValue(
			"extends");

		File parentXML = null;

		if (StringUtil.isNotBlank(parentXMLPath)) {
			parentXML = new File(parentXMLPath);

			if (!parentXML.exists()) {
				parentXMLPath = PropsUtil.getString(parentXMLPath);
				parentXML = new File(parentXMLPath);
			}

			if (parentXML.exists()) {
				try {
					Document parentDoc = SAXReaderUtil.read(parentXML);

					Element extensionElement =
						document.getRootElement().element("extension");

					document = mergeXMLAttributes(document, parentDoc);

					if (extensionElement != null) {
						document.getRootElement().add(
							extensionElement.createCopy());
					}
				}
				catch (DocumentException e) {
					e.printStackTrace();
				}
			}
			else {
				System.out.println(
					"Could not extend from: " + parentXMLPath +
					". File does not exist.");
			}
		}

		return document;
	}

	private static final String _ATTRIBUTE = "attribute";

	private static final String _ATTRIBUTES = "attributes";

	private static final String _COMPONENT = "component";

	private static final String _EVENT = "event";

	private static final String _EVENTS = "events";

	private List<Document> _componentsDefinitionDocs;
	private List<String> _componentsDefinitionList;
	private String _copyrightYear;

}