package org.might.exporter.api;


import org.might.exporter.model.Root;

import java.io.ByteArrayOutputStream;

/**
 * Date: 5/9/2022
 * Time: 2:42 PM
 */
public interface XMLSerializer {
    String getXmlString(Root data);

    ByteArrayOutputStream getXmlStream(Root data);
}
