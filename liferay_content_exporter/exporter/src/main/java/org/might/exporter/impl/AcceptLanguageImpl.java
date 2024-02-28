package org.might.exporter.impl;

import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.vulcan.accept.language.AcceptLanguage;

import java.util.List;
import java.util.Locale;

/**
 * Date: 18.02.2021
 * Time: 11:47
 */
public class AcceptLanguageImpl implements AcceptLanguage {

    private final String languageId;

    AcceptLanguageImpl(String languageId) {
        this.languageId = languageId;
    }

    @Override
    public List<Locale> getLocales() {
        return null;
    }

    @Override
    public String getPreferredLanguageId() {
        return languageId;
    }

    @Override
    public Locale getPreferredLocale() {
        return LocaleUtil.fromLanguageId(languageId);

    }
}