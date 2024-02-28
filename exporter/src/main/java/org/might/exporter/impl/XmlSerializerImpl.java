package org.might.exporter.impl;

import org.might.exporter.api.XMLSerializer;
import org.might.exporter.model.Root;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

@Component(immediate = true, service = {XMLSerializer.class})
public class XmlSerializerImpl implements XMLSerializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(XmlSerializerImpl.class);
    private static final String START_XML_SERIALIZATION = "Start xml serialization process";
    private static final String STOP_XML_SERIALIZATION = "Stop xml serialization process";
    private static final String ERROR_XML_SERIALIZATION = "Something went wrong at xml serialization process";
    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    @Override
    public String getXmlString(Root data) {
        StringWriter sw = new StringWriter();
        try {
            LOGGER.info(START_XML_SERIALIZATION);
            JAXBContext jaxbContext = JAXBContext.newInstance(Root.class);
            sw.append(XML_HEADER);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            jaxbMarshaller.marshal(data, sw);
            return sw.toString();
        } catch (Exception ex) {
            LOGGER.error(ERROR_XML_SERIALIZATION, ex);
            return sw.toString();
        } finally {
            LOGGER.info(STOP_XML_SERIALIZATION);
        }
    }

    @Override
    public ByteArrayOutputStream getXmlStream(Root data) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            LOGGER.info(START_XML_SERIALIZATION);
            JAXBContext jaxbContext = JAXBContext.newInstance(Root.class);
            outputStream.write(XML_HEADER.getBytes(StandardCharsets.UTF_8));
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            jaxbMarshaller.marshal(data, outputStream);
            return outputStream;
        } catch (Exception ex) {
            LOGGER.error(ERROR_XML_SERIALIZATION, ex);
            return outputStream;
        } finally {
            LOGGER.info(STOP_XML_SERIALIZATION);
        }
    }
}