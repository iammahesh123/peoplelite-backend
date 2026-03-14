package com.hrlite.utils;

import com.hrlite.entity.Payslip;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.Month;
import java.util.Locale;

@Component
@Slf4j
public class PayslipPdfGenerator {

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 16, Font.BOLD);
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 10, Font.BOLD);
    private static final Font NORMAL_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL);
    private static final Font BOLD_FONT = new Font(Font.HELVETICA, 10, Font.BOLD);

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public byte[] generate(Payslip payslip, String companyName) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Company header
            Paragraph company = new Paragraph(companyName, TITLE_FONT);
            company.setAlignment(Element.ALIGN_CENTER);
            document.add(company);

            String monthName = Month.of(payslip.getMonth()).name();
            Paragraph period = new Paragraph("Payslip for " + monthName + " " + payslip.getYear(), HEADER_FONT);
            period.setAlignment(Element.ALIGN_CENTER);
            period.setSpacingAfter(20);
            document.add(period);

            // Employee info
            PdfPTable infoTable = new PdfPTable(4);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(15);

            addInfoCell(infoTable, "Employee Name:", payslip.getEmployeeName());
            addInfoCell(infoTable, "Employee Code:", payslip.getEmployeeCode());
            addInfoCell(infoTable, "Working Days:", String.valueOf(payslip.getWorkingDays()));
            addInfoCell(infoTable, "LOP Days:", String.valueOf(payslip.getLopDays()));

            document.add(infoTable);

            // Earnings and Deductions side by side
            PdfPTable mainTable = new PdfPTable(2);
            mainTable.setWidthPercentage(100);
            mainTable.setSpacingAfter(15);

            // Earnings column
            PdfPTable earningsTable = new PdfPTable(2);
            earningsTable.setWidthPercentage(100);
            PdfPCell earningsHeader = new PdfPCell(new Phrase("Earnings", HEADER_FONT));
            earningsHeader.setColspan(2);
            earningsHeader.setBackgroundColor(new Color(230, 240, 255));
            earningsHeader.setPadding(8);
            earningsTable.addCell(earningsHeader);

            addRow(earningsTable, "Basic Salary", currencyFormat.format(payslip.getBasic()));
            addRow(earningsTable, "HRA", currencyFormat.format(payslip.getHra()));
            addRow(earningsTable, "Special Allowance", currencyFormat.format(payslip.getSpecialAllowance()));
            addBoldRow(earningsTable, "Gross Earnings", currencyFormat.format(payslip.getGrossEarnings()));

            PdfPCell earningsCell = new PdfPCell(earningsTable);
            earningsCell.setBorder(0);
            earningsCell.setPaddingRight(5);
            mainTable.addCell(earningsCell);

            // Deductions column
            PdfPTable deductionsTable = new PdfPTable(2);
            deductionsTable.setWidthPercentage(100);
            PdfPCell deductionsHeader = new PdfPCell(new Phrase("Deductions", HEADER_FONT));
            deductionsHeader.setColspan(2);
            deductionsHeader.setBackgroundColor(new Color(255, 230, 230));
            deductionsHeader.setPadding(8);
            deductionsTable.addCell(deductionsHeader);

            if (payslip.getPfEmployee().compareTo(java.math.BigDecimal.ZERO) > 0) {
                addRow(deductionsTable, "PF (Employee)", currencyFormat.format(payslip.getPfEmployee()));
            }
            if (payslip.getEsiEmployee().compareTo(java.math.BigDecimal.ZERO) > 0) {
                addRow(deductionsTable, "ESI (Employee)", currencyFormat.format(payslip.getEsiEmployee()));
            }
            if (payslip.getProfessionalTax().compareTo(java.math.BigDecimal.ZERO) > 0) {
                addRow(deductionsTable, "Professional Tax", currencyFormat.format(payslip.getProfessionalTax()));
            }
            if (payslip.getTds().compareTo(java.math.BigDecimal.ZERO) > 0) {
                addRow(deductionsTable, "TDS", currencyFormat.format(payslip.getTds()));
            }
            if (payslip.getOtherDeductions().compareTo(java.math.BigDecimal.ZERO) > 0) {
                addRow(deductionsTable, "Other Deductions", currencyFormat.format(payslip.getOtherDeductions()));
            }
            // If no deductions at all, show a note
            if (payslip.getTotalDeductions().compareTo(java.math.BigDecimal.ZERO) == 0) {
                addRow(deductionsTable, "No statutory deductions", "-");
            }
            addBoldRow(deductionsTable, "Total Deductions", currencyFormat.format(payslip.getTotalDeductions()));

            PdfPCell deductionsCell = new PdfPCell(deductionsTable);
            deductionsCell.setBorder(0);
            deductionsCell.setPaddingLeft(5);
            mainTable.addCell(deductionsCell);

            document.add(mainTable);

            // Net Pay
            PdfPTable netTable = new PdfPTable(2);
            netTable.setWidthPercentage(100);
            PdfPCell netLabel = new PdfPCell(new Phrase("Net Pay", new Font(Font.HELVETICA, 14, Font.BOLD)));
            netLabel.setBackgroundColor(new Color(200, 255, 200));
            netLabel.setPadding(12);
            netTable.addCell(netLabel);

            PdfPCell netValue = new PdfPCell(new Phrase(currencyFormat.format(payslip.getNetPay()),
                    new Font(Font.HELVETICA, 14, Font.BOLD)));
            netValue.setBackgroundColor(new Color(200, 255, 200));
            netValue.setPadding(12);
            netValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
            netTable.addCell(netValue);

            document.add(netTable);

            // Employer contributions (if applicable)
            boolean hasEmployerContrib = payslip.getPfEmployer().compareTo(java.math.BigDecimal.ZERO) > 0
                    || payslip.getEsiEmployer().compareTo(java.math.BigDecimal.ZERO) > 0;
            if (hasEmployerContrib) {
                Paragraph empContribHeader = new Paragraph("\nEmployer Contributions (not deducted from salary)", HEADER_FONT);
                empContribHeader.setSpacingBefore(10);
                document.add(empContribHeader);

                PdfPTable empContribTable = new PdfPTable(2);
                empContribTable.setWidthPercentage(50);
                empContribTable.setHorizontalAlignment(Element.ALIGN_LEFT);
                empContribTable.setSpacingBefore(5);

                if (payslip.getPfEmployer().compareTo(java.math.BigDecimal.ZERO) > 0) {
                    addRow(empContribTable, "PF (Employer)", currencyFormat.format(payslip.getPfEmployer()));
                }
                if (payslip.getEsiEmployer().compareTo(java.math.BigDecimal.ZERO) > 0) {
                    addRow(empContribTable, "ESI (Employer)", currencyFormat.format(payslip.getEsiEmployer()));
                }
                document.add(empContribTable);
            }

            // Footer
            Paragraph footer = new Paragraph("\nThis is a system-generated payslip.", NORMAL_FONT);
            footer.setSpacingBefore(30);
            document.add(footer);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating payslip PDF", e);
            throw new RuntimeException("Failed to generate payslip PDF", e);
        }
    }

    private void addInfoCell(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, HEADER_FONT));
        labelCell.setBorder(0);
        labelCell.setPadding(4);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valueCell.setBorder(0);
        valueCell.setPadding(4);
        table.addCell(valueCell);
    }

    private void addRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, NORMAL_FONT));
        labelCell.setPadding(6);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valueCell.setPadding(6);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private void addBoldRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, BOLD_FONT));
        labelCell.setPadding(6);
        labelCell.setBackgroundColor(new Color(245, 245, 245));
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, BOLD_FONT));
        valueCell.setPadding(6);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setBackgroundColor(new Color(245, 245, 245));
        table.addCell(valueCell);
    }
}
