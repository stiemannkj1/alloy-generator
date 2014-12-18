<#include "../base/init.ftl">
<#include "../base/copyright.ftl">
<#compress>

<#assign isChildClassOfAttributeTagSupport = component.isChildClassOf("com.liferay.taglib.util.AttributesTagSupport")>
<#assign isChildClassOfIncludeTag = component.isChildClassOf("com.liferay.taglib.util.IncludeTag")>

</#compress>

package ${packagePath}.${component.getUncamelizedName(BLANK)};
//J-

import javax.annotation.Generated;
import ${component.getParentClass()};

<#if component.isStyleable()>
import com.liferay.faces.util.component.Styleable;
</#if>
<#if component.isYui()>
import com.liferay.faces.util.component.ClientComponent;
</#if>

/**
<#list component.getAuthors()?sort as author>
 * @author	${author}
</#list>
 */
@Generated(value = "com.liferay.alloy.tools.builder.FacesBuilder")
public abstract class ${component.getCamelizedName()}${BASE_CLASS_SUFFIX} extends ${component.getUnqualifiedParentClass()}<#if component.isStyleable() || component.isYui()> implements<#if component.isStyleable()> Styleable</#if><#if component.isStyleable() && component.isYui()>,</#if><#if component.isYui()> ClientComponent</#if></#if> {
	<#assign enumWritten = false>
	<#list component.getAttributes()?sort_by("constantName") as attribute>
	<#if attribute.isGenerateJava() && !attribute.isInherited()>
	<#if !enumWritten>

	// Protected Enumerations
	protected enum ${component.getCamelizedName()}PropertyKeys {
		<#rt>${attribute.getJavaSafeName()}
		<#assign enumWritten = true>
	<#else>
		<#lt>,
		<#rt>${attribute.getJavaSafeName()}
	</#if>
	</#if>
	</#list>
	<#if enumWritten>

	}
	</#if>
	<#list component.getAttributes()?sort_by("javaBeanPropertyName") as attribute>
	<#if attribute.isGenerateJava() && !attribute.isInherited()>

	<#if attribute.isOverride()>
	@Override
	</#if>
	<#if attribute.getJavaWrapperType()?contains("<")>
	@SuppressWarnings("unchecked")
	</#if>
	public ${attribute.getUnprefixedType()} ${attribute.getGetterMethodPrefix()}${attribute.getJavaBeanPropertyName()}() {
		return (${attribute.getJavaWrapperType()}) getStateHelper().eval(${component.getCamelizedName()}PropertyKeys.${attribute.getJavaSafeName()}, ${attribute.getDefaultValue()});
	}

	<#if attribute.isOverride()>
	@Override
	</#if>
	public void set${attribute.getJavaBeanPropertyName()}(${attribute.getUnprefixedType()} ${attribute.getJavaSafeName()}) {
		getStateHelper().put(${component.getCamelizedName()}PropertyKeys.${attribute.getJavaSafeName()}, ${attribute.getJavaSafeName()});
	}
	</#if>
	</#list>
}
//J+
