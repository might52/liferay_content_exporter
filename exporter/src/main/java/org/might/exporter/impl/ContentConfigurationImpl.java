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

import com.liferay.portal.kernel.util.PrefsPropsUtil;
import org.might.exporter.CommonConstant;
import org.might.exporter.api.ContentConfiguration;
import org.osgi.service.component.annotations.Component;

@Component(immediate = true)
public class ContentConfigurationImpl implements ContentConfiguration {

    @Override
    public String getExportFolder() {
        return PrefsPropsUtil.getString(CommonConstant.OUTBOUND_FOLDER_PROPERTIES);
    }

    @Override
    public String getFilterKeywords() {
        return PrefsPropsUtil.getString(CommonConstant.KEYWORDS_PROPERTIES);
    }
}