package com.hrlite.utils;

import com.lowagie.text.*;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.Map;

@Component
@Slf4j
public class OfferLetterPdfGenerator {

    public byte[] generate(String htmlContent, Map<String, String> variables) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Replace placeholders
            String processedHtml = htmlContent;
            if (variables != null) {
                for (Map.Entry<String, String> entry : variables.entrySet()) {
                    String placeholder = "{{" + entry.getKey() + "}}";
                    String value = entry.getValue() != null ? escapeHtml(entry.getValue()) : "";
                    processedHtml = processedHtml.replace(placeholder, value);
                }
            }

            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Parse HTML to PDF
            HTMLWorker htmlWorker = new HTMLWorker(document);
            htmlWorker.parse(new StringReader(processedHtml));

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating offer letter PDF", e);
            throw new RuntimeException("Failed to generate offer letter PDF", e);
        }
    }

    private String escapeHtml(String input) {
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
