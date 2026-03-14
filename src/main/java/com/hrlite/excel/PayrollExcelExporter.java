package com.hrlite.excel;

import com.hrlite.entity.PayrollRun;
import com.hrlite.entity.Payslip;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.Month;
import java.util.List;

@Component
@Slf4j
public class PayrollExcelExporter {

    private static final String[] HEADERS = {
            "Employee Code", "Employee Name", "Basic", "HRA", "Special Allowance",
            "Gross Earnings", "PF (Employee)", "PF (Employer)", "ESI (Employee)",
            "ESI (Employer)", "Professional Tax", "TDS", "Other Deductions",
            "Total Deductions", "Net Pay", "Working Days", "LOP Days"
    };

    public byte[] export(PayrollRun run, List<Payslip> payslips) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            String monthName = Month.of(run.getMonth()).name();
            Sheet sheet = workbook.createSheet("Payroll " + monthName + " " + run.getYear());

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Currency style
            CellStyle currencyStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            currencyStyle.setDataFormat(format.getFormat("#,##0.00"));

            // Header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowIdx = 1;
            for (Payslip p : payslips) {
                Row row = sheet.createRow(rowIdx++);
                int col = 0;

                row.createCell(col++).setCellValue(p.getEmployeeCode());
                row.createCell(col++).setCellValue(p.getEmployeeName());

                Cell basicCell = row.createCell(col++);
                basicCell.setCellValue(p.getBasic().doubleValue());
                basicCell.setCellStyle(currencyStyle);

                Cell hraCell = row.createCell(col++);
                hraCell.setCellValue(p.getHra().doubleValue());
                hraCell.setCellStyle(currencyStyle);

                Cell saCell = row.createCell(col++);
                saCell.setCellValue(p.getSpecialAllowance().doubleValue());
                saCell.setCellStyle(currencyStyle);

                Cell grossCell = row.createCell(col++);
                grossCell.setCellValue(p.getGrossEarnings().doubleValue());
                grossCell.setCellStyle(currencyStyle);

                Cell pfEmpCell = row.createCell(col++);
                pfEmpCell.setCellValue(p.getPfEmployee().doubleValue());
                pfEmpCell.setCellStyle(currencyStyle);

                Cell pfErCell = row.createCell(col++);
                pfErCell.setCellValue(p.getPfEmployer().doubleValue());
                pfErCell.setCellStyle(currencyStyle);

                Cell esiEmpCell = row.createCell(col++);
                esiEmpCell.setCellValue(p.getEsiEmployee().doubleValue());
                esiEmpCell.setCellStyle(currencyStyle);

                Cell esiErCell = row.createCell(col++);
                esiErCell.setCellValue(p.getEsiEmployer().doubleValue());
                esiErCell.setCellStyle(currencyStyle);

                Cell ptCell = row.createCell(col++);
                ptCell.setCellValue(p.getProfessionalTax().doubleValue());
                ptCell.setCellStyle(currencyStyle);

                Cell tdsCell = row.createCell(col++);
                tdsCell.setCellValue(p.getTds().doubleValue());
                tdsCell.setCellStyle(currencyStyle);

                Cell otherCell = row.createCell(col++);
                otherCell.setCellValue(p.getOtherDeductions().doubleValue());
                otherCell.setCellStyle(currencyStyle);

                Cell totalDeducCell = row.createCell(col++);
                totalDeducCell.setCellValue(p.getTotalDeductions().doubleValue());
                totalDeducCell.setCellStyle(currencyStyle);

                Cell netCell = row.createCell(col++);
                netCell.setCellValue(p.getNetPay().doubleValue());
                netCell.setCellStyle(currencyStyle);

                row.createCell(col++).setCellValue(p.getWorkingDays());
                row.createCell(col).setCellValue(p.getLopDays());
            }

            // Auto-size columns
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error exporting payroll to Excel", e);
            throw new RuntimeException("Failed to export payroll to Excel", e);
        }
    }
}
