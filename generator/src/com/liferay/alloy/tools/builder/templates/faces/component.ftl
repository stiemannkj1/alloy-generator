<#include "../base/init.ftl">
<#include "../base/copyright.ftl">

package ${packagePath}.${component.getUncamelizedName(BLANK)};

import javax.faces.component.FacesComponent;

import com.liferay.faces.util.component.ComponentUtil;

/**
<#list component.getAuthors()?sort as author>
 * @author	${author}
</#list>
 */
@FacesComponent(value = ${component.getCamelizedName()}.COMPONENT_TYPE)
public class ${component.getCamelizedName()} extends ${component.getCamelizedName()}${BASE_CLASS_SUFFIX} {

	// Public Constants
	public static final String COMPONENT_TYPE = "${packagePath}.${component.getUncamelizedName(BLANK)}.${component.getCamelizedName()}";
	public static final String RENDERER_TYPE = "${packagePath}.${component.getUncamelizedName(BLANK)}.internal.${component.getCamelizedName()}${RENDERER_CLASS_SUFFIX}";
	public static final String STYLE_CLASS_NAME = "${namespace}-${component.getUncamelizedName()}";

	public ${component.getCamelizedName()}() {
		super();
		setRendererType(RENDERER_TYPE);
	}

	@Override
	public String getStyleClass() {

		// getStateHelper().eval(PropertyKeys.styleClass, null) is called because super.getStyleClass() may return the
		// STYLE_CLASS_NAME of the super class.
		String styleClass = (String) getStateHelper().eval(PropertyKeys.styleClass, null);

		return ComponentUtil.concatCssClasses(styleClass, STYLE_CLASS_NAME);
	}
}
