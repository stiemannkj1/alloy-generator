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

package com.liferay.alloy.tools.builder.taglib;

import com.liferay.alloy.tools.builder.base.BaseBuilder;
import com.liferay.alloy.tools.builder.taglib.model.TagComponent;
import com.liferay.alloy.tools.model.Component;
import com.liferay.alloy.util.PropsUtil;
import com.liferay.alloy.util.xml.SAXReaderUtil;
import com.liferay.alloy.util.xml.xpath.AlloyGeneratorNamespaceContext;

import java.io.ByteArrayInputStream;
import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jodd.io.FileUtil;

import jodd.typeconverter.Convert;

import jodd.util.StringPool;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;

import org.jaxen.NamespaceContext;
public class TagBuilder extends BaseBuilder {

	public static void main(String[] args) throws Exception {
		String componentsXML = PropsUtil.getString(
			"builder.taglibs.components.definitions");
		String docrootDir = PropsUtil.getString("builder.taglibs.docroot.dir");
		String javaDir = PropsUtil.getString("builder.taglibs.java.dir");
		String javaPackage = PropsUtil.getString(
			"builder.taglibs.java.package");
		String jspCommonInitPath = PropsUtil.getString(
			"builder.taglibs.jsp.common.init.path");
		String jspDir = PropsUtil.getString("builder.taglibs.jsp.dir");
		String tldDir = PropsUtil.getString("builder.taglibs.tld.dir");

		new TagBuilder(
			componentsXML, javaDir, docrootDir, javaPackage, jspDir,
			jspCommonInitPath, tldDir);
	}

	public TagBuilder(
			String componentsDefinitionList, String javaDir, String docrootDir,
			String javaPackage, String jspDir, String jspCommonInitPath,
			String tldDir)
		throws Exception {

		_docrootDir = docrootDir;
		_javaDir = javaDir;
		_javaPackage = javaPackage;
		_jspCommonInitPath = jspCommonInitPath;
		_jspDir = jspDir;
		_tldDir = tldDir;

		_tplCommonInitJsp = _TEMPLATES_DIR + "common_init_jsp.ftl";
		_tplInitJsp = _TEMPLATES_DIR + "init_jsp.ftl";
		_tplJsp = _TEMPLATES_DIR + "jsp.ftl";
		_tplStartJsp = _TEMPLATES_DIR + "start_jsp.ftl";
		_tplTag = _TEMPLATES_DIR + "tag.ftl";
		_tplTagBase = _TEMPLATES_DIR + "tag_base.ftl";
		_tplTld = _TEMPLATES_DIR + "tld.ftl";

		build();
	}

	@Override
	public void build() throws Exception {
		List<Component> components = getAllComponents();

		for (Component component : components) {
			TagComponent tagComponent = (TagComponent)component;
			Map<String, Object> context = getTemplateContext(tagComponent);

			_createBaseTag(tagComponent, context);

			if (tagComponent.isWriteJSP()) {
				_createPageJSP(tagComponent, context);
			}

			_createTag(tagComponent, context);
		}

		_createCommonInitJSP();
		_createTld();

		System.out.println(
			"Finished looping over " + components.size() + " components.");
	}

	@Override
	protected List<Component> getComponents(Document doc) throws Exception {
		Element root = doc.getRootElement();

		List<Component> components = new ArrayList<Component>();

		String defaultPackage = root.attributeValue("short-name");
		List<Element> allComponentNodes = root.elements("component");

		for (Element node : allComponentNodes) {
			TagComponent tagComponent = new TagComponent();
			tagComponent.initialize(node, defaultPackage);
			components.add(tagComponent);
		}

		return components;
	}

	@Override
	protected Map<String, Object> getDefaultTemplateContext() {
		Map<String, Object> context = super.getDefaultTemplateContext();

		context.put("jspCommonInitPath", _jspCommonInitPath);
		context.put("jspDir", _jspDir);
		context.put("packagePath", _javaPackage);

		return context;
	}

	protected String getJavaOutputBaseDir(TagComponent tagComponent) {
		StringBuilder sb = new StringBuilder();

		sb.append(getJavaOutputDir(tagComponent));
		sb.append(_BASE);
		sb.append(StringPool.SLASH);

		return sb.toString();
	}

	protected String getJavaOutputDir(TagComponent tagComponent) {
		StringBuilder sb = new StringBuilder();

		sb.append(_javaDir);
		sb.append(tagComponent.getPackage());
		sb.append(StringPool.SLASH);

		return sb.toString();
	}

	protected String getJspDir(TagComponent tagComponent) {
		StringBuilder sb = new StringBuilder();

		sb.append(_jspDir);
		sb.append(tagComponent.getPackage());
		sb.append(StringPool.SLASH);

		return sb.toString();
	}

	protected String getJspOutputDir(TagComponent tagComponent) {
		StringBuilder sb = new StringBuilder();

		sb.append(_docrootDir);
		sb.append(StringPool.SLASH);
		sb.append(_jspDir);
		sb.append(tagComponent.getPackage());
		sb.append(StringPool.SLASH);

		return sb.toString();
	}

	protected Map<String, Object> getTemplateContext(
		TagComponent tagComponent) {

		Map<String, Object> context = super.getTemplateContext(tagComponent);

		String jspRelativePath = getJspDir(tagComponent).concat(
			tagComponent.getUncamelizedName(StringPool.UNDERSCORE));

		context.put("jspRelativePath", jspRelativePath);

		return context;
	}

	protected Document mergeTlds(Document sourceDoc, Document targetDoc) {
		Element targetRoot = targetDoc.getRootElement();

		DocumentFactory factory = SAXReaderUtil.getDocumentFactory();

		XPath xpathTags = factory.createXPath("//tld:tag");

		Map<String, String> namespaceContextMap = new HashMap<String, String>();

		namespaceContextMap.put(_TLD_XPATH_PREFIX, _TLD_XPATH_URI);

		NamespaceContext namespaceContext = new AlloyGeneratorNamespaceContext(
			namespaceContextMap);

		xpathTags.setNamespaceContext(namespaceContext);

		List<Node> sources = xpathTags.selectNodes(sourceDoc);

		for (Node source : sources) {
			Element sourceElement = (Element)source;

			String sourceName = sourceElement.elementText("name");

			String xpathTagValue = "//tld:tag[tld:name='" + sourceName + "']";

			XPath xpathTag = factory.createXPath(xpathTagValue);

			xpathTag.setNamespaceContext(namespaceContext);

			List<Node> targets = xpathTag.selectNodes(targetDoc);

			if (targets.size() > 0) {
				Element targetElement = (Element)targets.get(0);

				XPath xpathAttributes = factory.createXPath(
					xpathTagValue + "//tld:attribute");

				Map<String, String> namespaces = new HashMap<String, String>();

				namespaces.put("tld", StringPool.EMPTY);

				xpathAttributes.setNamespaceURIs(namespaces);

				List<Node> sourceAttributes = xpathAttributes.selectNodes(
					source);

				for (Node sourceAttribute : sourceAttributes) {
					Element sourceAttributeElement = (Element)sourceAttribute;

					String attributeName = sourceAttributeElement.elementText(
						"name");

					String xpathAttributeValue = "//tld:attribute[tld:name='" +
						attributeName + "']";

					XPath xpathAttribute = factory.createXPath(
						xpathTagValue + xpathAttributeValue);

					xpathAttribute.setNamespaceContext(namespaceContext);

					Node targetAttribute = xpathAttribute.selectSingleNode(
						targetElement);

					if (targetAttribute != null) {
						targetAttribute.detach();
					}

					targetElement.add(sourceAttributeElement.createCopy());
				}

				Element dynamicAttrElement = targetElement.element(
					"dynamic-attributes");

				if (dynamicAttrElement != null) {
					targetElement.add(dynamicAttrElement.detach());
				}
			}
			else {
				targetRoot.add(sourceElement.createCopy());
			}
		}

		return targetDoc;
	}

	private void _createBaseTag(
			TagComponent tagComponent, Map<String, Object> context)
		throws Exception {

		StringBuilder sb = new StringBuilder();

		sb.append(getJavaOutputBaseDir(tagComponent));
		sb.append(_BASE_CLASS_PREFIX);
		sb.append(tagComponent.getClassName());
		sb.append(_CLASS_SUFFIX);

		String content = processTemplate(_tplTagBase, context);

		File tagFile = new File(sb.toString());

		writeFile(tagFile, content);
	}

	private void _createCommonInitJSP() throws Exception {
		Map<String, Object> context = getDefaultTemplateContext();

		String contentCommonInitJsp = processTemplate(
			_tplCommonInitJsp, context);

		StringBuilder sb = new StringBuilder();

		sb.append(_docrootDir);
		sb.append(StringPool.SLASH);
		sb.append(_jspCommonInitPath);

		File commonInitFile = new File(sb.toString());

		writeFile(commonInitFile, contentCommonInitJsp, false);
	}

	private void _createPageJSP(
			TagComponent tagComponent, Map<String, Object> context)
		throws Exception {

		String pathName = tagComponent.getUncamelizedName(
			StringPool.UNDERSCORE);
		String path = getJspOutputDir(tagComponent).concat(pathName);

		String contentJsp = processTemplate(_tplJsp, context);
		String contentInitJsp = processTemplate(_tplInitJsp, context);

		File initFile = new File(path.concat(_INIT_PAGE));
		File initExtFile = new File(path.concat(_INIT_EXT_PAGE));

		writeFile(initFile, contentInitJsp);
		writeFile(initExtFile, StringPool.EMPTY, false);

		if (tagComponent.isBodyContent()) {
			String contentStart = processTemplate(_tplStartJsp, context);

			File startFile = new File(path.concat(_START_PAGE));

			writeFile(startFile, contentStart, false);
		}
		else {
			File pageFile = new File(path.concat(_PAGE));

			writeFile(pageFile, contentJsp, false);
		}
	}

	private void _createTag(
			TagComponent tagComponent, Map<String, Object> context)
		throws Exception {

		StringBuilder sb = new StringBuilder();

		sb.append(getJavaOutputDir(tagComponent));
		sb.append(tagComponent.getClassName());
		sb.append(_CLASS_SUFFIX);

		String content = processTemplate(_tplTag, context);

		File tagFile = new File(sb.toString());

		writeFile(tagFile, content, false);
	}

	private void _createTld() throws Exception {
		Map<String, Object> context = getDefaultTemplateContext();

		for (Document doc : getComponentDefinitionDocs()) {
			Element root = doc.getRootElement();

			String shortName = Convert.toString(
				root.attributeValue("short-name"), _DEFAULT_TAGLIB_SHORT_NAME);
			String version = Convert.toString(
				root.attributeValue("tlib-version"), _DEFAULT_TAGLIB_VERSION);
			String uri = Convert.toString(
				root.attributeValue("uri"), _DEFAULT_TAGLIB_URI);

			boolean isAlloyComponent = shortName.equals(
				_DEFAULT_ATTRIBUTE_NAMESPACE);

			context.put("alloyComponent", isAlloyComponent);
			context.put("components", getComponents(doc));
			context.put("shortName", shortName);
			context.put("uri", uri);
			context.put("version", version);

			String tldFilePath = _tldDir.concat(
				shortName).concat(_TLD_EXTENSION);

			File tldFile = new File(tldFilePath);

			String content = processTemplate(_tplTld, context);

			Document source = SAXReaderUtil.read(
				new ByteArrayInputStream(content.getBytes()));

			if (tldFile.exists()) {
				Document target = SAXReaderUtil.read(
					new ByteArrayInputStream(FileUtil.readBytes(tldFile)));

				source = mergeTlds(source, target);
			}

			writeFile(tldFile, content, true);
		}
	}

	private static final String _BASE = "base";

	private static final String _BASE_CLASS_PREFIX = "Base";

	private static final String _CLASS_SUFFIX = ".java";

	private static final String _DEFAULT_ATTRIBUTE_NAMESPACE = "alloy";

	private static final String _DEFAULT_TAGLIB_SHORT_NAME = "alloy";

	private static final String _DEFAULT_TAGLIB_URI =
		"http://alloy.liferay.com/tld/alloy";

	private static final String _DEFAULT_TAGLIB_VERSION = "1.0";

	private static final String _INIT_EXT_PAGE = "/init-ext.jspf";

	private static final String _INIT_PAGE = "/init.jsp";

	private static final String _PAGE = "/page.jsp";

	private static final String _START_PAGE = "/start.jsp";

	private static final String _TEMPLATES_DIR =
		"com/liferay/alloy/tools/builder/templates/taglib/";

	private static final String _TLD_EXTENSION = ".tld";

	private static final String _TLD_XPATH_PREFIX = "tld";

	private static final String _TLD_XPATH_URI =
		"http://java.sun.com/xml/ns/j2ee";

	private String _docrootDir;
	private String _javaDir;
	private String _javaPackage;
	private String _jspCommonInitPath;
	private String _jspDir;
	private String _tldDir;
	private String _tplCommonInitJsp;
	private String _tplInitJsp;
	private String _tplJsp;
	private String _tplStartJsp;
	private String _tplTag;
	private String _tplTagBase;
	private String _tplTld;

}