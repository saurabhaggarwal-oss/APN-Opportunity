package com.ttn.ck.apn.service;

import com.ttn.ck.apn.exception.ExcelExportException;
import com.ttn.ck.apn.model.ApnOpportunityMasterData;
import com.ttn.ck.apn.model.ApnOpportunityRawData;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Utility service for generating Excel (.xlsx) files using Apache POI.
 * Produces styled workbooks with headers, auto-sized columns, and
 * properly formatted date/number cells.
 */
@Service
public class ExcelExportService {

    private static final Logger log = LoggerFactory.getLogger(ExcelExportService.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // ═══════════════════════════════════════════════════════════════════════
    //  Master Data Export
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Generates an Excel file containing master opportunity data.
     *
     * @param records list of master data records to export
     * @return byte array of the .xlsx file
     * @throws ExcelExportException if file generation fails
     */
    public byte[] exportMasterData(List<ApnOpportunityMasterData> records) {
        log.info("Generating master data Excel for {} records", records.size());

        String[] headers = {
                "UUID", "Customer Name", "Customer Company Name", "Industry",
                "Country", "State", "Partner Project Title",
                "Customer Business Problem", "Solution Offered",
                "Use Case", "Estimated Monthly Revenue", "Target Close Date",
                "Opportunity Type", "Delivery Model", "Sales Activity",
                "Account ID", "Opportunity Raised", "Opportunity Raised Date",
                "Opportunity Raised By", "Cloud Platform", "Partner Name",
                "Logged Date", "Created Date", "Modified Date"
        };

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Master Opportunity Data");

            // Create header row with styling
            CellStyle headerStyle = createHeaderStyle(workbook);
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Populate data rows
            CellStyle dateStyle = createDateStyle(workbook);
            for (int rowIdx = 0; rowIdx < records.size(); rowIdx++) {
                ApnOpportunityMasterData record = records.get(rowIdx);
                Row row = sheet.createRow(rowIdx + 1);

                int col = 0;
                row.createCell(col++).setCellValue(nullSafe(record.getLineitemUuid()));
                row.createCell(col++).setCellValue(nullSafe(record.getCustomerName()));
                row.createCell(col++).setCellValue(nullSafe(record.getCustomerCompanyName()));
                row.createCell(col++).setCellValue(nullSafe(record.getIndustry()));
                row.createCell(col++).setCellValue(nullSafe(record.getCountry()));
                row.createCell(col++).setCellValue(nullSafe(record.getState()));
                row.createCell(col++).setCellValue(nullSafe(record.getPartnerProjectTitle()));
                row.createCell(col++).setCellValue(nullSafe(record.getCustomerBusinessProblem()));
                row.createCell(col++).setCellValue(nullSafe(record.getSolutionOffered()));
                row.createCell(col++).setCellValue(nullSafe(record.getUseCase()));
                setCellDouble(row.createCell(col++), record.getEstimatedMonthlyRevenue());
                setCellDate(row.createCell(col++), record.getTargetCloseDate(), dateStyle);
                row.createCell(col++).setCellValue(nullSafe(record.getOpportunityType()));
                row.createCell(col++).setCellValue(nullSafe(record.getDeliveryModel()));
                row.createCell(col++).setCellValue(nullSafe(record.getSalesActivity()));
                row.createCell(col++).setCellValue(nullSafe(record.getLineitemUsageaccountid()));
                row.createCell(col++).setCellValue(record.getOpportunityRaised() != null
                        ? record.getOpportunityRaised().toString() : "");
                setCellDate(row.createCell(col++), record.getOpportunityRaisedDate(), dateStyle);
                row.createCell(col++).setCellValue(nullSafe(record.getOpportunityRaisedBy()));
                row.createCell(col++).setCellValue(nullSafe(record.getCloudPlatform()));
                row.createCell(col++).setCellValue(nullSafe(record.getPartnerName()));
                setCellDate(row.createCell(col++), record.getLoggedDate(), dateStyle);
                setCellDate(row.createCell(col++), record.getCreatedDate(), dateStyle);
                setCellDate(row.createCell(col++), record.getModifiedDate(), dateStyle);
            }

            // Auto-size columns for readability
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            return toByteArray(workbook);

        } catch (IOException e) {
            throw new ExcelExportException("Failed to generate master data Excel", e);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Raw Data Export
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Generates an Excel file containing raw opportunity data.
     *
     * @param records list of raw data records to export
     * @return byte array of the .xlsx file
     * @throws ExcelExportException if file generation fails
     */
    public byte[] exportRawData(List<ApnOpportunityRawData> records) {
        log.info("Generating raw data Excel for {} records", records.size());

        String[] headers = {
                "UUID", "Customer Name", "Account ID", "Service Name",
                "Region", "Operating System", "Resource ID",
                "Instance Type", "Product Code", "Name Tag",
                "Autoscaling Name", "Key", "Workload Title",
                "Workload Description", "Total Period Cost",
                "Resource Birth Date", "Active Days", "Expected Days",
                "Logged Date", "Cloud Platform"
        };

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Raw Opportunity Data");

            // Create header row
            CellStyle headerStyle = createHeaderStyle(workbook);
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Populate data rows
            CellStyle dateStyle = createDateStyle(workbook);
            for (int rowIdx = 0; rowIdx < records.size(); rowIdx++) {
                ApnOpportunityRawData record = records.get(rowIdx);
                Row row = sheet.createRow(rowIdx + 1);

                int col = 0;
                row.createCell(col++).setCellValue(nullSafe(record.getLineitemUuid()));
                row.createCell(col++).setCellValue(nullSafe(record.getCustomerName()));
                row.createCell(col++).setCellValue(nullSafe(record.getLineitemUsageaccountid()));
                row.createCell(col++).setCellValue(nullSafe(record.getServiceName()));
                row.createCell(col++).setCellValue(nullSafe(record.getMycloudRegionname()));
                row.createCell(col++).setCellValue(nullSafe(record.getMycloudOperatingsystem()));
                row.createCell(col++).setCellValue(nullSafe(record.getLineitemResourceid()));
                row.createCell(col++).setCellValue(nullSafe(record.getMycloudInstancetype()));
                row.createCell(col++).setCellValue(nullSafe(record.getProductcode()));
                row.createCell(col++).setCellValue(nullSafe(record.getFinalNameTag()));
                row.createCell(col++).setCellValue(nullSafe(record.getFinalAutoscalingName()));
                row.createCell(col++).setCellValue(nullSafe(record.getKey()));
                row.createCell(col++).setCellValue(nullSafe(record.getWorkloadTitle()));
                row.createCell(col++).setCellValue(nullSafe(record.getWorkloadDescription()));

                // BigDecimal cost
                if (record.getTotalPeriodCost() != null) {
                    row.createCell(col++).setCellValue(record.getTotalPeriodCost().doubleValue());
                } else {
                    row.createCell(col++).setCellValue("");
                }

                setCellDate(row.createCell(col++), record.getResourceBirthDate(), dateStyle);
                setCellLong(row.createCell(col++), record.getActiveDaysCount());
                setCellLong(row.createCell(col++), record.getExpectedDays());
                setCellDate(row.createCell(col++), record.getLoggedDate(), dateStyle);
                row.createCell(col++).setCellValue(nullSafe(record.getCloudPlatform()));
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            return toByteArray(workbook);

        } catch (IOException e) {
            throw new ExcelExportException("Failed to generate raw data Excel", e);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Styling Utilities
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Creates a bold, light-grey-background header cell style.
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * Creates a date-formatted cell style (yyyy-MM-dd HH:mm:ss).
     */
    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper helper = workbook.getCreationHelper();
        style.setDataFormat(helper.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));
        return style;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Cell Value Helpers
    // ═══════════════════════════════════════════════════════════════════════

    private String nullSafe(String value) {
        return value != null ? value : "";
    }

    private void setCellDate(Cell cell, Date date, CellStyle dateStyle) {
        if (date != null) {
            cell.setCellValue(date);
            cell.setCellStyle(dateStyle);
        } else {
            cell.setCellValue("");
        }
    }

    private void setCellDouble(Cell cell, Double value) {
        if (value != null) {
            cell.setCellValue(value);
        } else {
            cell.setCellValue("");
        }
    }

    private void setCellLong(Cell cell, Long value) {
        if (value != null) {
            cell.setCellValue(value);
        } else {
            cell.setCellValue("");
        }
    }

    /**
     * Writes the workbook to a byte array for HTTP response streaming.
     */
    private byte[] toByteArray(Workbook workbook) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.write(out);
            return out.toByteArray();
        }
    }
}
