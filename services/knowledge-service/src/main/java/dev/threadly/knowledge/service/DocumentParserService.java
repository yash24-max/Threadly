package dev.threadly.knowledge.service;

import dev.threadly.knowledge.exception.DocumentIngestionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Service for parsing different document formats.
 * Supports PDF, TXT, HTML, MD, and DOCX files.
 */
@Slf4j
@Service
public class DocumentParserService {

  /**
   * Parse document content from file stream based on content type.
   *
   * @param inputStream the file input stream
   * @param contentType the MIME type
   * @param filename the filename
   * @return extracted text content
   */
  public String parseDocument(InputStream inputStream, String contentType, String filename) {
    log.info("Parsing document: {} with content type: {}", filename, contentType);

    try {
      return switch (contentType.toLowerCase()) {
        case "application/pdf" -> parsePdf(inputStream);
        case "text/plain" -> parseText(inputStream);
        case "text/html" -> parseHtml(inputStream);
        case "text/markdown" -> parseMarkdown(inputStream);
        case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ->
            parseDocx(inputStream);
        default -> throw new DocumentIngestionException("Unsupported content type: " + contentType);
      };
    } catch (Exception e) {
      throw new DocumentIngestionException(filename, "parsing", e);
    }
  }

  /**
   * Parse PDF document.
   *
   * @param inputStream the PDF input stream
   * @return extracted text
   */
  private String parsePdf(InputStream inputStream) throws IOException {
    log.debug("Parsing PDF document");

    try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
      PDFTextStripper stripper = new PDFTextStripper();
      int totalPages = document.getNumberOfPages();

      StringBuilder content = new StringBuilder();
      for (int pageNum = 0; pageNum < totalPages; pageNum++) {
        stripper.setStartPage(pageNum + 1);
        stripper.setEndPage(pageNum + 1);
        String pageText = stripper.getText(document);
        content.append("[Page ").append(pageNum + 1).append("]\n");
        content.append(pageText).append("\n");
      }

      return content.toString();
    }
  }

  /**
   * Parse plain text document.
   *
   * @param inputStream the text input stream
   * @return the text content
   */
  private String parseText(InputStream inputStream) throws IOException {
    log.debug("Parsing text document");

    return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
  }

  /**
   * Parse HTML document (strip tags).
   *
   * @param inputStream the HTML input stream
   * @return extracted text
   */
  private String parseHtml(InputStream inputStream) throws IOException {
    log.debug("Parsing HTML document");

    String html = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    // Simple HTML tag stripping
    return html.replaceAll("<[^>]*>", " ")
        .replaceAll("\\s+", " ")
        .trim();
  }

  /**
   * Parse Markdown document (remove markdown syntax but keep structure).
   *
   * @param inputStream the markdown input stream
   * @return cleaned text
   */
  private String parseMarkdown(InputStream inputStream) throws IOException {
    log.debug("Parsing Markdown document");

    String markdown = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    // Remove markdown syntax but keep content
    return markdown
        .replaceAll("^#+\\s+", "")  // Remove headers
        .replaceAll("\\[([^\\]]+)\\]\\([^)]+\\)", "$1")  // Convert links to text
        .replaceAll("`+", "")  // Remove code markers
        .replaceAll("^[-*]\\s+", "");  // Remove list markers
  }

  /**
   * Parse DOCX document.
   *
   * @param inputStream the DOCX input stream
   * @return extracted text
   */
  private String parseDocx(InputStream inputStream) throws IOException {
    log.debug("Parsing DOCX document");

    // For production, use Apache POI library
    // This is a basic implementation that reads raw text
    try (var docx = new org.apache.poi.xwpf.usermodel.XWPFDocument(inputStream)) {
      StringBuilder content = new StringBuilder();
      for (var paragraph : docx.getParagraphs()) {
        content.append(paragraph.getText()).append("\n");
      }
      for (var table : docx.getTables()) {
        for (var row : table.getRows()) {
          for (var cell : row.getTableCells()) {
            content.append(cell.getText()).append("\t");
          }
          content.append("\n");
        }
      }
      return content.toString();
    }
  }

  /**
   * Get supported content types.
   *
   * @return list of supported MIME types
   */
  public static List<String> getSupportedContentTypes() {
    return List.of(
        "application/pdf",
        "text/plain",
        "text/html",
        "text/markdown",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );
  }
}
