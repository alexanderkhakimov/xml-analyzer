package com.example;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// Реализация для чтения тегов из файла
class FileTagReader implements TagReader {
    @Override
    public List<String> readTags(String tagsPath) throws IOException {
        return Files.readAllLines(Paths.get(tagsPath)).stream()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toList());
    }
}

// Реализация для парсинга XML
class DefaultXmlParser implements XmlParser {
    @Override
    public Document parse(File file) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(file);
    }
}

// Реализация для извлечения кода МД
class DefaultDmcCodeExtractor implements DmcCodeExtractor {
    @Override
    public String extractDmcCode(Document doc) {
        NodeList dmCodeList = doc.getElementsByTagName("dmCode");
        if (dmCodeList.getLength() > 0) {
            Element dmCode = (Element) dmCodeList.item(0);
            String modelIdentCode = dmCode.getAttribute("modelIdentCode");
            String systemDiffCode = dmCode.getAttribute("systemDiffCode");
            String systemCode = dmCode.getAttribute("systemCode");
            String subSystemCode = dmCode.getAttribute("subSystemCode");
            String subSubSystemCode = dmCode.getAttribute("subSubSystemCode");
            String assyCode = dmCode.getAttribute("assyCode");
            String disassyCode = dmCode.getAttribute("disassyCode");
            String disassyCodeVariant = dmCode.getAttribute("disassyCodeVariant");
            String infoCode = dmCode.getAttribute("infoCode");
            String infoCodeVariant = dmCode.getAttribute("infoCodeVariant");
            String itemLocationCode = dmCode.getAttribute("itemLocationCode");

            NodeList languageList = doc.getElementsByTagName("language");
            String languageIsoCode = "", countryIsoCode = "";
            if (languageList.getLength() > 0) {
                Element language = (Element) languageList.item(0);
                languageIsoCode = language.getAttribute("languageIsoCode");
                countryIsoCode = language.getAttribute("countryIsoCode");
            }

            NodeList issueInfoList = doc.getElementsByTagName("issueInfo");
            String issueNumber = "", inWork = "";
            if (issueInfoList.getLength() > 0) {
                Element issueInfo = (Element) issueInfoList.item(0);
                issueNumber = issueInfo.getAttribute("issueNumber");
                inWork = issueInfo.getAttribute("inWork");
            }

            return String.format("DMC-%s-%s-%s-%s%s-%s-%s%s-%s%s-%s_%s-%s_%s-%s",
                    modelIdentCode, systemDiffCode, systemCode, subSystemCode, subSubSystemCode,
                    assyCode, disassyCode, disassyCodeVariant, infoCode, infoCodeVariant,
                    itemLocationCode, issueNumber, inWork, languageIsoCode, countryIsoCode);
        }
        return "Unknown_DMC";
    }
}

// Реализация для обработки слов (исключение чисел)
class NonNumericWordProcessor implements WordProcessor {
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^-?\\d*\\.?\\d+$");

    @Override
    public boolean isValidWord(String word) {
        return !word.isEmpty() && !NUMBER_PATTERN.matcher(word).matches();
    }

    @Override
    public String normalizeWord(String word) {
        return word.replaceAll("[^\\p{L}\\p{N}]", "").toLowerCase();
    }
}

// Реализация для подсчета слов
class XmlWordCounter implements WordCounter {
    @Override
    public int[] countWords(Node node, boolean isTemporaryRevision, List<String> tagsToProcess, Map<String, Integer> wordCountMap) {
        int wordCount = 0;
        int changeMarkWordCount = 0;

        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            String tagName = element.getTagName();
            boolean shouldProcess = tagsToProcess.contains(tagName) &&
                    (!isTemporaryRevision || "1".equals(element.getAttribute("changeMark")));

            if (shouldProcess) {
                String text = element.getTextContent().trim();
                if (!text.isEmpty()) {
                    String[] words = text.split("\\s+");
                    wordCount += words.length;
                    if ("1".equals(element.getAttribute("changeMark"))) {
                        changeMarkWordCount += words.length;
                    }
                    NonNumericWordProcessor processor = new NonNumericWordProcessor();
                    for (String word : words) {
                        String normalizedWord = processor.normalizeWord(word);
                        if (processor.isValidWord(normalizedWord)) {
                            wordCountMap.put(normalizedWord, wordCountMap.getOrDefault(normalizedWord, 0) + 1);
                        }
                    }
                }
            }

            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                int[] childCounts = countWords(childNodes.item(i), isTemporaryRevision, tagsToProcess, wordCountMap);
                wordCount += childCounts[0];
                changeMarkWordCount += childCounts[1];
            }
        }
        return new int[]{wordCount, changeMarkWordCount};
    }
}