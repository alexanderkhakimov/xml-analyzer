package com.example;


import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public interface XmlParser {
    Document parse(File file) throws ParserConfigurationException, SAXException, IOException, org.xml.sax.SAXException;
}
