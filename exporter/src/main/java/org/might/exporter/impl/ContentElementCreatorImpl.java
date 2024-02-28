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
package org.might.exporter.impl;

import com.liferay.headless.delivery.dto.v1_0.Document;
import com.liferay.headless.delivery.dto.v1_0.StructuredContent;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Company;
import org.might.exporter.CommonConstant;
import org.might.exporter.ContentElementUtil;
import org.might.exporter.api.ContentConfiguration;
import org.might.exporter.api.ContentElementCreator;
import org.might.exporter.api.ContentLoader;
import org.might.exporter.model.Locales;
import org.might.exporter.model.Root;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

@Component(immediate = true, service = {ContentElementCreator.class})
public class ContentElementCreatorImpl implements ContentElementCreator {
    private static final String SOMETHING_WENT_WRONG = "Something went wrong during getting web content from liferay.";
    private static final String START_GENERATE_ROOT = "Start generate Root element with keywords={}";
    private static final String FINISH_GENERATE_ROOT = "Root element generation completed";
    private static final int PAGE_SIZE = 1000;
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentElementCreatorImpl.class);
    private Root root;

    @Reference
    private ContentConfiguration contentConfiguration;
    @Reference
    private ContentLoader contentLoader;

    public ContentElementCreatorImpl() {
    }

    public Root getRootContent(Company company) {
        try {
            LOGGER.info(START_GENERATE_ROOT, contentConfiguration.getFilterKeywords());
            initRoot();
            contentLoader.setUpSearchConfiguration(company, PAGE_SIZE);
            fillDocuments(contentLoader.getDocuments(CommonConstant.LOCALE_EN));
            fillStructuredContent(contentLoader.getStructuredContents(CommonConstant.LOCALE_EN), CommonConstant.LOCALE_EN);
            fillStructuredContent(contentLoader.getStructuredContents(CommonConstant.LOCALE_HU), CommonConstant.LOCALE_HU);
            LOGGER.info(FINISH_GENERATE_ROOT);
            return root;
        } catch (Exception ex) {
            LOGGER.error(SOMETHING_WENT_WRONG, ex);
            return null;
        }
    }

    private void initRoot() {
        root = new Root();
        root.setLocales(new ArrayList<>());
        root.setDocuments(new ArrayList<>());
    }

    public Locales initOrGetLocale(String locale) {
        return root.getLocales().stream()
                .filter(loc -> loc.getLocaleId().equals(locale))
                .findFirst()
                .orElseGet(() -> {
                    Locales loc = new Locales();
                    loc.setContent(new ArrayList<>());
                    if (CommonConstant.LOCALE_HU.equals(locale)) {
                        loc.setLocaleId(CommonConstant.LOCALE_HU);
                        loc.setLocaleName(CommonConstant.LOCAL_NAME_HU);
                    } else {
                        loc.setLocaleId(CommonConstant.LOCALE_EN);
                        loc.setLocaleName(CommonConstant.LOCAL_NAME_EN);
                    }
                    root.getLocales().add(loc);
                    return loc;
                });
    }

    private void fillDocuments(Collection<Document> content) throws IOException, PortalException {
        LOGGER.info("Start filling documents.");
        for (Document doc : content) {
            root.getDocuments().add(ContentElementUtil.getDocument(doc));
        }
        LOGGER.info("Filling documents completed");
    }

    private void fillStructuredContent(Collection<StructuredContent> content, String locale) {
        LOGGER.info("Start filling structured content for locale: {}", locale);
        Locales loc = initOrGetLocale(locale);
        for (StructuredContent sc : content) {
            loc.getContent().add(ContentElementUtil.getContent(sc));
        }
        LOGGER.info("Filling structured content for locale: {} completed", locale);
    }
}
