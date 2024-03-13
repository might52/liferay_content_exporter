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

import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.portal.kernel.servlet.PortalWebResourceConstants;
import com.liferay.portal.kernel.servlet.PortalWebResourcesUtil;
import com.liferay.portal.kernel.template.TemplateConstants;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.template.TemplateResourceParser;
import com.liferay.portal.template.URLResourceParser;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.net.URL;

@Component(
        immediate = true, property = "lang.type=" + TemplateConstants.LANG_TYPE_FTL,
        service = TemplateResourceParser.class
)
public class FreeMarkerResourceParser extends URLResourceParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(FreeMarkerResourceParser.class);

    @Reference
    private Portal portal;

    private ServiceTrackerMap<String, ServletContext> _serviceTrackerMap;

    @Override
    public URL getURL(String name) throws IOException {
        int pos = name.indexOf(TemplateConstants.SERVLET_SEPARATOR);
        if (pos == -1) {
            return null;
        }

        String servletContextName = name.substring(0, pos);
        if (servletContextName.equals(portal.getPathContext())) {
            servletContextName = portal.getServletContextName();
        }

        ServletContext servletContext = _serviceTrackerMap.getService(servletContextName);
        if (servletContext == null) {
            LOGGER.error("{} is invalid because {} does not map to a servlet context.", name, servletContextName);
            return null;
        }

        String templateName = name.substring(pos + TemplateConstants.SERVLET_SEPARATOR.length());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{} is associated with the servlet context {} {}", name, servletContextName, servletContext);
        }

        URL url = servletContext.getResource(templateName);
        if (url == null) {
            url = PortalWebResourcesUtil.getResource(templateName);
        }

        if (url == null) {
            url = FrameworkUtil.getBundle(getClass()).getEntry(templateName);
        }

        if ((url == null) && templateName.endsWith("/init_custom.ftl")) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("The template {} should be created", name);
            }
            ServletContext themeClassicServletContext = PortalWebResourcesUtil.getServletContext(
                    PortalWebResourceConstants.RESOURCE_TYPE_THEME_CLASSIC);
            url = themeClassicServletContext.getResource("/classic/templates/init_custom.ftl");
        }

        return url;
    }

    @Activate
    protected void activate(final BundleContext bundleContext) {
        _serviceTrackerMap = ServiceTrackerMapFactory.openSingleValueMap(
                bundleContext, ServletContext.class, null, (serviceReference, emitter) -> {
                    try {
                        ServletContext servletContext = bundleContext.getService(serviceReference);
                        String servletContextName = GetterUtil.getString(servletContext.getServletContextName());
                        emitter.emit(servletContextName);
                    } finally {
                        bundleContext.ungetService(serviceReference);
                    }
                });
    }

    @Deactivate
    protected void deactivate() {
        _serviceTrackerMap.close();
    }

}

