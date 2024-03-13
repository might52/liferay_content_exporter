/*
 * MIT License
 *
 * Copyright (c) 2024 Andrei F._
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.might.exporter.menu;

import com.liferay.portal.kernel.io.unsync.UnsyncStringWriter;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.template.Template;
import com.liferay.portal.kernel.template.TemplateConstants;
import com.liferay.portal.kernel.template.TemplateException;
import com.liferay.portal.kernel.template.TemplateManagerUtil;
import com.liferay.portal.kernel.template.TemplateResource;
import com.liferay.portal.kernel.template.TemplateResourceLoaderUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.UnsyncPrintWriterPool;
import com.liferay.portal.kernel.util.WebKeys;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.MimeResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceURL;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;


@Component(
        immediate = true,
        property = {
                "com.liferay.portlet.add-default-resource=true",
                "com.liferay.portlet.display-category=category.hidden",
                "com.liferay.portlet.use-default-template=true",
                "com.liferay.portlet.requires-namespaced-parameters=false",
                "com.liferay.portlet.header-portlet-css=/css/view.css",
                "com.liferay.portlet.header-portlet-javascript=/js/main.js",
                "com.liferay.portlet.instanceable=true",
                "javax.portlet.display-name=Liferay content exporter",
                "javax.portlet.name=ContentExporterPortlet",
                "javax.portlet.init-param.template-path=/META-INF/resources/",
                "javax.portlet.init-param.view-template=/ftl/view.ftl",
                "javax.portlet.resource-bundle=content.Language",
                "javax.portlet.version=3.0"
        },
        service = Portlet.class
)
public class ContentExporterPortlet extends MVCPortlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentExporterPortlet.class);

    protected String currentTemplatePathPLEASEREFACTORME;
    protected boolean currentPortletIsThinSSRPLEASEREFACTORME;

    @Override
    protected void include(String path, RenderRequest renderRequest, RenderResponse renderResponse) throws IOException, PortletException {
        String portletInstanceId = renderRequest.getAttribute(WebKeys.PORTLET_ID).toString();
        Map<String, Object> templateParametersMap = new HashMap<>();
        templateParametersMap.put("portletInstanceId", portletInstanceId);
        this.currentTemplatePathPLEASEREFACTORME = path;
        this.currentPortletIsThinSSRPLEASEREFACTORME = false;
        TemplateResource templateResource = null;
        String resourcePath = getResourcePath(path, getPortletContext());
        try {
            boolean resourceExists = TemplateResourceLoaderUtil.hasTemplateResource(
                    TemplateConstants.LANG_TYPE_FTL, resourcePath);
            if (resourceExists) {
                templateResource = TemplateResourceLoaderUtil.getTemplateResource(
                        TemplateConstants.LANG_TYPE_FTL, resourcePath);
            }
        } catch (TemplateException te) {
            throw new IOException(te);
        }

        if (templateResource == null) {
            LOGGER.error(path + " is not a valid include");
        } else {
            try {
                Template template = createTemplate(templateResource, renderRequest, renderResponse, getPortletContext(), templateParametersMap);
                Writer writer = null;
                if (renderResponse instanceof MimeResponse) {
                    MimeResponse mimeResponse = (MimeResponse) renderResponse;
                    writer = UnsyncPrintWriterPool.borrow(mimeResponse.getWriter());
                } else {
                    writer = new UnsyncStringWriter();
                }

                template.processTemplate(writer);
                String portletName = this.getPortletConfig().getPortletName();

                if (!currentPortletIsThinSSRPLEASEREFACTORME) {
                    ResourceURL baseURL = renderResponse.createResourceURL();
                    baseURL.setResourceID("__PortletResourceIdTemplatePlaceholder__");
                    StringBundler sb = new StringBundler(3);
                    PortletData portletData = new PortletData(portletName, templateParametersMap, baseURL.toString());
                    String portletDataSerialized = JSONFactoryUtil.looseSerializeDeep(portletData);
                    LOGGER.info("PortletInstanceId: " + portletInstanceId);
                    LOGGER.info("PortletInitParams: " + portletDataSerialized);
                    sb.append("<script>");
                    sb.append(String.format("window.portlets = window.portlets || {}; window.portlets['%s'] = %s;", portletInstanceId, portletDataSerialized));
                    sb.append("</script>");
                    writer.write(sb.toString());
                }
            } catch (Exception e) {
                throw new PortletException(e);
            }

        }

        if (clearRequestParameters) {
            renderResponse.setProperty("clear-request-parameters", "true");
        }
    }

    private String getResourcePath(String path, PortletContext portletContext) {
        String servletContextName = portletContext.getPortletContextName();
        return servletContextName.concat(TemplateConstants.SERVLET_SEPARATOR).concat(path);
    }

    private Template createTemplate(TemplateResource templateResource, PortletRequest renderRequest,
                                    PortletResponse renderResponse, PortletContext portletContext,
                                    Map<String, Object> templateParametersMap) throws TemplateException {
        Template template = TemplateManagerUtil.getTemplate(
                TemplateConstants.LANG_TYPE_FTL,
                templateResource,
                false
        );
        template.prepareTaglib(
                PortalUtil.getHttpServletRequest(renderRequest),
                PortalUtil.getHttpServletResponse(renderResponse)
        );
        template.put("portletContext", portletContext);
        template.prepare(PortalUtil.getHttpServletRequest(renderRequest));
        template.put("portletParams", templateParametersMap);
        template.put("userInfo", renderRequest.getAttribute(PortletRequest.USER_INFO));
        return template;
    }
}
