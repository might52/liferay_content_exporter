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

import com.liferay.portal.kernel.model.Company;
import org.might.exporter.api.ContentElementCreator;
import org.might.exporter.api.ContentExporter;
import org.might.exporter.api.Uploader;
import org.might.exporter.api.XMLSerializer;
import org.might.exporter.model.Root;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Component(immediate = true)
public class ContentExporterImpl implements ContentExporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContentExporterImpl.class);

    private static final String START_EXPORT = "Start export process";
    private static final String STOP_EXPORT = "Stop export process";
    private static final String SKIP_EXPORT_COMPANY_NULL = "Company value is null, start exporting skipped.";

    @Reference
    private Uploader fileUploader;

    @Reference
    private ContentElementCreator contentElementCreator;

    @Reference
    private XMLSerializer xmlSerializer;

    @Override
    public void export(Company company) {
        if (Objects.isNull(company)) {
            LOGGER.error(SKIP_EXPORT_COMPANY_NULL);
            return;
        }

        CompletableFuture.runAsync(() -> {
            LOGGER.info(START_EXPORT);
            Root rootContent = contentElementCreator.getRootContent(company);
            ByteArrayOutputStream stream = null;
            if (Objects.nonNull(rootContent)) {
                stream = xmlSerializer.getXmlStream(rootContent);
            }

            fileUploader.uploadFile(stream);
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception ignored) {
            }
            LOGGER.info(STOP_EXPORT);
        });
    }
}