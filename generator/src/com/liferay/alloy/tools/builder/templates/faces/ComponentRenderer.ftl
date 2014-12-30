<#include "../base/init.ftl">
<#include "../base/copyright.ftl">

package ${packagePath}.${component.getUncamelizedName()}.internal;

<#if component.isYui()>
import javax.faces.application.ResourceDependencies;
</#if>
import javax.faces.application.ResourceDependency;
import javax.faces.render.FacesRenderer;

import ${packagePath}.${component.getUncamelizedName()}.${component.getCamelizedName()};


/**
<#list component.getAuthors()?sort as author>
 * @author	${author}
</#list>
 */
//J-
@FacesRenderer(componentFamily = ${component.getCamelizedName()}.COMPONENT_FAMILY, rendererType = ${component.getCamelizedName()}.RENDERER_TYPE)
<#if component.isYui()>
@ResourceDependencies(
	{
		@ResourceDependency(library = "liferay-faces-reslib", name = "build/aui-css/css/bootstrap.min.css"),
		@ResourceDependency(library = "liferay-faces-reslib", name = "build/aui/aui-min.js"),
		@ResourceDependency(library = "liferay-faces-reslib", name = "liferay.js")
	}
)
<#else>
@ResourceDependency(library = "liferay-faces-alloy", name = "liferay.js")
</#if>
//J+
public class ${component.getCamelizedName()}Renderer extends ${component.getCamelizedName()}RendererBase {
	// Initial Generation
}
