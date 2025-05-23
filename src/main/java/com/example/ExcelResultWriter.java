package com.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

class ExcelResultWriter implements ResultWriter {
    private static final String TOTAL_WORDS_SHEET = "Общее количество слов";
    private static final String STATS_SHEET = "Статистика";

    @Override
    public void writeResults(Map<String, Integer> totalWordCounts, Map<String, Integer> changeMarkWordCounts,
                             Map<String, Map<String, Integer>> wordStats, String outputPath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Вкладка "Общее количество слов"
            Sheet totalSheet = workbook.createSheet(TOTAL_WORDS_SHEET);
            Row headerRow = totalSheet.createRow(0);
            headerRow.createCell(0).setCellValue("Код МД");
            headerRow.createCell(1).setCellValue("Количество слов (шт.)");
            headerRow.createCell(2).setCellValue("Количество слов с changeMark=1 (шт.)");

            int rowNum = 1;
            for (Map.Entry<String, Integer> entry : totalWordCounts.entrySet()) {
                Row row = totalSheet.createRow(rowNum++);
                String dmcCode = entry.getKey();
                row.createCell(0).setCellValue(dmcCode);
                row.createCell(1).setCellValue(entry.getValue());
                row.createCell(2).setCellValue(changeMarkWordCounts.getOrDefault(dmcCode, 0));
            }

            // Вкладка "Статистика"
            Sheet statsSheet = workbook.createSheet(STATS_SHEET);
            headerRow = statsSheet.createRow(0);
            headerRow.createCell(0).setCellValue("Код МД");
            headerRow.createCell(1).setCellValue("Слово");
            headerRow.createCell(2).setCellValue("Количество упоминаний (раз.)");

            rowNum = 1;
            for (Map.Entry<String, Map<String, Integer>> dmcEntry : wordStats.entrySet()) {
                String dmcCode = dmcEntry.getKey();
                Map<String, Integer> words = dmcEntry.getValue();
                for (Map.Entry<String, Integer> wordEntry : words.entrySet()) {
                    Row row = statsSheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(dmcCode);
                    row.createCell(1).setCellValue(wordEntry.getKey());
                    row.createCell(2).setCellValue(wordEntry.getValue());
                }
            }

            // Автоматическая настройка ширины столбцов
            for (int i = 0; i < 3; i++) {
                totalSheet.autoSizeColumn(i);
                statsSheet.autoSizeColumn(i);
            }

            // Сохранение файла
            try (FileOutputStream fileOut = new FileOutputStream(outputPath)) {
                workbook.write(fileOut);
            }
        }
    }
}