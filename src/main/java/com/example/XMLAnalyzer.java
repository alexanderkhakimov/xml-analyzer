package com.example;

import org.w3c.dom.Document;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public class XMLAnalyzer {
    private final TagReader tagReader;
    private final XmlParser xmlParser;
    private final DmcCodeExtractor dmcCodeExtractor;
    private final WordCounter wordCounter;
    private final ResultWriter resultWriter;
    private final Pattern dmcPattern;

    // Конструктор с внедрением зависимостей
    public XMLAnalyzer(TagReader tagReader, XmlParser xmlParser, DmcCodeExtractor dmcCodeExtractor,
                       WordCounter wordCounter, ResultWriter resultWriter) {
        this.tagReader = tagReader;
        this.xmlParser = xmlParser;
        this.dmcCodeExtractor = dmcCodeExtractor;
        this.wordCounter = wordCounter;
        this.resultWriter = resultWriter;
        this.dmcPattern = Pattern.compile("DMC-[A-Z0-9-]+_\\d{3}-[1-9][0-9]+_[a-z]{2}-[A-Z]{2}");
    }

    public void analyze(String xmlPath, String tagsPath, String outputPath) {
        try {
            // 1. Чтение тегов
            List<String> tagsToProcess = tagReader.readTags(tagsPath);
            System.out.println("Загружены теги для обработки: " + tagsToProcess);

            // 2. Обработка XML-файлов
            Map<String, Integer> totalWordCounts = new TreeMap<>();
            Map<String, Integer> changeMarkWordCounts = new TreeMap<>();
            Map<String, Map<String, Integer>> wordStats = new TreeMap<>();
            processXMLFiles(xmlPath, tagsToProcess, totalWordCounts, changeMarkWordCounts, wordStats);

            // 3. Запись результатов
            resultWriter.writeResults(totalWordCounts, changeMarkWordCounts, wordStats, outputPath);

            System.out.println("Выполнено");
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    private void processXMLFiles(String xmlPath, List<String> tagsToProcess,
                                 Map<String, Integer> totalWordCounts, Map<String, Integer> changeMarkWordCounts,
                                 Map<String, Map<String, Integer>> wordStats) {
        File xmlFile = new File(xmlPath);
        List<File> xmlFiles = new ArrayList<>();
        if (xmlFile.isDirectory()) {
            xmlFiles = Arrays.asList(Objects.requireNonNull(xmlFile.listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"))));
        } else if (xmlFile.getName().toLowerCase().endsWith(".xml")) {
            xmlFiles.add(xmlFile);
        } else {
            throw new IllegalArgumentException("Указан неверный путь к XML-файлам");
        }

        for (File file : xmlFiles) {
            try {
                Document doc = xmlParser.parse(file);
                String dmcCode = dmcCodeExtractor.extractDmcCode(doc);
                boolean isTemporaryRevision = dmcPattern.matcher(dmcCode).matches();

                Map<String, Integer> wordCountMap = new HashMap<>();
                int[] counts = wordCounter.countWords(doc.getDocumentElement(), isTemporaryRevision, tagsToProcess, wordCountMap);
                totalWordCounts.put(dmcCode, counts[0]);
                changeMarkWordCounts.put(dmcCode, counts[1]);
                wordStats.put(dmcCode, wordCountMap);
                System.out.println(file.getName() + ": обработан!");
            } catch (Exception e) {
                System.err.println("Ошибка при обработке файла " + file.getName() + ": " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Введите путь к папке с XML-файлами или к одному XML-файлу: ");
        String xmlPath = scanner.nextLine().trim();

        System.out.println("Введите путь для сохранения файла .xlsx: ");
        String outputPath = scanner.nextLine().trim();
        if (!outputPath.endsWith(".xlsx")) {
            outputPath += File.separator + "result.xlsx";
        }

        System.out.println("Введите путь к файлу с перечнем тегов (.txt): ");
        String tagsPath = scanner.nextLine().trim();

        // Создание зависимостей (Фабрика)
        TagReader tagReader = new FileTagReader();
        XmlParser xmlParser = new DefaultXmlParser();
        DmcCodeExtractor dmcCodeExtractor = new DefaultDmcCodeExtractor();
        WordCounter wordCounter = new XmlWordCounter();
        ResultWriter resultWriter = new ExcelResultWriter();

        // Создание и запуск анализатора
        XMLAnalyzer analyzer = new XMLAnalyzer(tagReader, xmlParser, dmcCodeExtractor, wordCounter, resultWriter);
        analyzer.analyze(xmlPath, tagsPath, outputPath);

        scanner.close();
    }
}