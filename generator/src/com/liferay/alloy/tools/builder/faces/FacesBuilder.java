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

package com.liferay.alloy.tools.builder.faces;

import com.liferay.alloy.tools.builder.base.BaseBuilder;
import com.liferay.alloy.tools.builder.faces.model.FacesComponent;
import com.liferay.alloy.tools.model.Attribute;
import com.liferay.alloy.tools.model.Component;
import com.liferay.alloy.tools.model.Event;
import com.liferay.alloy.util.PropsUtil;

import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.dom4j.Document;
import org.dom4j.Element;
public class FacesBuilder extends BaseBuilder {

	public static void main(String[] args) throws Exception {
		FacesBuilder facesBuilder = new FacesBuilder();
		facesBuilder.build();
	}

	@Override
	public void build() throws Exception {
		List<Component> components = getAllComponents();
		Iterator<Component> iterator = components.iterator();

		Map<String, Object> context = new HashMap<String, Object>();
		context.put("copyrightYear", getCopyrightYear());
		context.put("namespace", _NAMESPACE);
		context.put("packagePath", _COMPONENTS_PACKAGE);

		while (iterator.hasNext()) {
			FacesComponent facesComponent = (FacesComponent)iterator.next();

			if (facesComponent.isGenerateJava()) {
				context.put("component", facesComponent);
				_buildComponentFile(facesComponent, context);
				_buildComponentFile(facesComponent, context, "Base");

				if (facesComponent.isGenerateRenderer()) {
					_buildComponentFile(facesComponent, context, "Renderer");
					_buildComponentFile(facesComponent, context, "RendererBase");
				}

				context.remove("component");
			}

			if (!facesComponent.isGenerateTaglibXML()) {
				iterator.remove();
			}
		}

		_buildTaglibXMLFile(components, context);

		System.out.println(
			"Finished looping over " + components.size() + " components.");
	}

	@Override
	protected List<Component> getComponents(Document doc) throws Exception {
		Element root = doc.getRootElement();

		Map<String, Component> facesComponentsMap =
			new HashMap<String, Component>();

		String defaultYUIRendererParentClass = root.attributeValue(
			"defaultYUIRendererParentClass");
		String defaultSince = root.attributeValue("defaultSince");
		List<Element> allComponentNodes = root.elements("component");

		for (Element node : allComponentNodes) {
			FacesComponent facesComponent = new FacesComponent();
			facesComponent.initialize(
				node, _COMPONENTS_PACKAGE, defaultYUIRendererParentClass,
				defaultSince);
			facesComponentsMap.put(facesComponent.getName(), facesComponent);
		}

		List<Component> facesComponents = new ArrayList<Component>(
			facesComponentsMap.values());

		for (Component facesComponent : facesComponents) {
			recursivelyAddExtensionAttributesAndEvents(
				facesComponent, facesComponentsMap);
		}

		return facesComponents;
	}

	protected void recursivelyAddExtensionAttributesAndEvents(
		Component component, Map<String, Component> facesComponentsMap) {

		String[] extensionNames = component.getExtendsTags();

		if (extensionNames != null) {
			for (String extensionName : extensionNames) {
				Component extensionComponent = facesComponentsMap.get(
					extensionName);

				try {
					recursivelyAddExtensionAttributesAndEvents(
						extensionComponent, facesComponentsMap);
				}
				catch (NullPointerException e) {
					throw new NoSuchElementException(
						component.getName() + " extends non-existent tag " +
						extensionName);
				}

				List<Attribute> extensionAttributes =
					extensionComponent.getAttributesAndEvents();

				if (extensionAttributes.size() > 0) {
					List<Attribute> attributes = component.getAttributes();
					List<Event> events = component.getEvents();

					for (Attribute extensionAttribute : extensionAttributes) {
						if (extensionAttribute instanceof Event) {
							Event event = (Event)extensionAttribute;

							if (!events.contains(event)) {
								events.add(event);
							}
						}
						else {
							if (!attributes.contains(extensionAttribute)) {
								attributes.add(extensionAttribute);
							}
						}
					}
				}
			}
		}
	}

	@Override
	protected String processTemplate(String name, Map<String, Object> context)
		throws Exception {

		return super.processTemplate(_TEMPLATES_DIR.concat(name), context);
	}

	private void _buildComponentFile(
			FacesComponent facesComponent, Map<String, Object> context)
		throws Exception {

		_buildComponentFile(facesComponent, context, "");
	}

	private void _buildComponentFile(
			FacesComponent facesComponent, Map<String, Object> context,
			String suffix)
		throws Exception {

		String templateFilePath = "Component" + suffix + ".ftl";
		String componentContent = processTemplate(templateFilePath, context);

		StringBuilder sb = new StringBuilder(8);

		if (suffix.contains("Renderer")) {
			sb.append(_COMPONENT_IMPL_DIR);
		}
		else {
			sb.append(_COMPONENT_API_DIR);
		}

		sb.append("/");
		sb.append(facesComponent.getUncamelizedName());
		sb.append("/");

		if (suffix.contains("Renderer")) {
			sb.append("internal/");
		}

		sb.append(facesComponent.getCamelizedName());
		sb.append(suffix);
		sb.append(".java");

		File componentFile = new File(sb.toString());
		writeFile(componentFile, componentContent, suffix.contains("Base"));
	}

	private void _buildTaglibXMLFile(
			List<Component> components, Map<String, Object> context)
		throws Exception {

		context.put("components", components);
		context.put("version", PropsUtil.getString("builder.faces.version"));

		for (Document doc : getComponentDefinitionDocs()) {
			Element root = doc.getRootElement();

			Element descriptionElement = root.element("description");
			String description = null;

			if (descriptionElement != null) {
				description = descriptionElement.getTextTrim();
			}

			context.put("description", description);

			Element extensionElement = root.element("extension");

			if (extensionElement != null) {
				List<Element> extensionElements = extensionElement.elements();
				context.put("extensionElements", extensionElements);
			}

			String taglibXMLContent = processTemplate(
				"taglib.xml.ftl", context);

			StringBuilder sb = new StringBuilder(4);
			sb.append(_TAGLIB_XML_OUTPUT_DIR);
			sb.append("/");
			sb.append(_NAMESPACE);
			sb.append(".taglib.xml");

			File taglibXMLFile = new File(sb.toString());
			writeFile(taglibXMLFile, taglibXMLContent, true);
		}
	}

	private static final String _BASE_API_OUTPUT_DIR = PropsUtil.getString(
		"builder.faces.api.output.dir");

	private static final String _BASE_IMPL_OUTPUT_DIR = PropsUtil.getString(
		"builder.faces.impl.output.dir");

	private static final String _NAMESPACE = PropsUtil.getString(
		"builder.faces.taglib.xml.namespace", "alloy");

	private static final String _COMPONENTS_PACKAGE =
		"com.liferay.faces." + _NAMESPACE + ".component";

	private static final String _COMPONENT_API_DIR =
		_BASE_API_OUTPUT_DIR + "/" + _COMPONENTS_PACKAGE.replaceAll("\\.", "/");

	private static final String _COMPONENT_IMPL_DIR =
		_BASE_IMPL_OUTPUT_DIR + "/" + _COMPONENTS_PACKAGE.replaceAll("\\.", "/");

	private static final String _TAGLIB_XML_OUTPUT_DIR = PropsUtil.getString(
		"builder.faces.taglib.xml.output.dir");

	private static final String _TEMPLATES_DIR =
		"com/liferay/alloy/tools/builder/templates/faces/";

}