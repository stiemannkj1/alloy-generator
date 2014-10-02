<#include "../base/init.ftl">
<#include "../base/copyright.ftl">

package ${packagePath}.${component.getUncamelizedName(BLANK)}.internal;
//J-

<#if component.isYui()>
import java.io.IOException;
</#if>

import javax.annotation.Generated;
<#if component.isYui()>
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
</#if>

<#if component.isYui()>
import ${packagePath}.${component.getUncamelizedName(BLANK)}.${component.getCamelizedName()};
</#if>

import ${component.getRendererParentClass()};


/**
<#list component.getAuthors()?sort as author>
 * @author	${author}
</#list>
 */
@Generated(value = "com.liferay.alloy.tools.builder.FacesBuilder")
public abstract class ${component.getCamelizedName()}${RENDERER_BASE_CLASS_SUFFIX} extends ${component.getUnqualifiedRendererParentClass()} {
	<#assign first = true>
	<#list component.getAttributes()?sort_by("yuiName") as attribute>
	<#if attribute.isGenerateJava()>
	<#if first>

	// Protected Constants
	</#if>
	protected static final String ${attribute.getYuiConstantName()} = "${attribute.getYuiName()}";
	<#assign first = false>
	</#if>
	</#list>
	<#if component.isYui()>

	// Private Constants
	private static final String ALLOY_CLASS_NAME = "${component.getYuiClassName()}";
	private static final String ALLOY_MODULE_NAME = "${component.getModule()}";

	// Modules
	protected static final String[] MODULES = {ALLOY_MODULE_NAME};

	@Override
	public void encodeAlloyAttributes(FacesContext facesContext, ResponseWriter responseWriter, UIComponent uiComponent) throws IOException {

		${component.getCamelizedName()} ${component.getUncapitalizedName()} = (${component.getCamelizedName()}) uiComponent;
		boolean first = true;
		<#list component.getAttributes()?sort_by("javaSafeName") as attribute>
		<#if attribute.isGenerateJava() && attribute.isYui()>

		${attribute.getJavaWrapperType()} ${attribute.getJavaSafeName()} = ${component.getUncapitalizedName()}.${attribute.getGetterMethodPrefix()}${attribute.getJavaBeanPropertyName()}();

		if (${attribute.getJavaSafeName()} != null) {

			encode${attribute.getYuiName()?cap_first}(responseWriter, ${component.getUncapitalizedName()}, ${attribute.getJavaSafeName()}, first);
			first = false;
		}
		</#if>
		</#list>

		encodeHiddenAttributes(facesContext, responseWriter, ${component.getUncapitalizedName()}, first);
	}

	@Override
	public String getAlloyClassName(FacesContext facesContext, UIComponent uiComponent) {
		return ALLOY_CLASS_NAME;
	}

	@Override
	protected String[] getModules(FacesContext facesContext, UIComponent uiComponent) {
		return MODULES;
	}
	<#list component.getAttributes()?sort_by("capitalizedName") as attribute>
	<#if attribute.isGenerateJava() && attribute.isYui()>

	protected void encode${attribute.getYuiName()?cap_first}(ResponseWriter responseWriter, ${component.getCamelizedName()} ${component.getUncapitalizedName()}, ${attribute.getJavaWrapperType()} ${attribute.getJavaSafeName()}, boolean first) throws IOException {
		encode${attribute.getYuiType()}(responseWriter, ${attribute.getYuiConstantName()}, ${attribute.getJavaSafeName()},<#if attribute.getYuiType() == "ClientId"> ${component.getUncapitalizedName()},</#if> first);
	}
	</#if>
	</#list>

	protected void encodeHiddenAttributes(FacesContext facesContext, ResponseWriter responseWriter, ${component.getCamelizedName()} ${component.getUncapitalizedName()}, boolean first) throws IOException {
		// no-op
	}
	</#if>
}
//J+
