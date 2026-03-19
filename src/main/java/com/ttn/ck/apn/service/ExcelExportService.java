package com.ttn.ck.apn.service;

import com.ttn.ck.apn.errorhandler.GenericStatusException;
import com.ttn.ck.apn.model.ApnOpportunityMasterData;
import com.ttn.ck.apn.model.ApnOpportunityRawData;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Utility service for generating Excel (.xlsx) files using Apache POI.
 * Produces styled workbooks with headers, auto-sized columns, and
 * properly formatted date/number cells.
 */

@Slf4j
@Service
public class ExcelExportService {

    /**
     * Generates an Excel file containing master opportunity data.
     *
     * @param records list of master data records to export
     * @return byte array of the .xlsx file
     * @throws GenericStatusException if file generation fails
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
                ApnOpportunityMasterData opportunityMasterData = records.get(rowIdx);
                Row row = sheet.createRow(rowIdx + 1);

                int col = 0;
                row.createCell(col++).setCellValue(nullSafe(opportunityMasterData.getLineitemUuid()));
                row.createCell(col++).setCellValue(nullSafe(opportunityMasterData.getCustomerName()));
                row.createCell(col++).setCellValue(nullSafe(opportunityMasterData.getCustomerCompanyName()));
                row.createCell(col++).setCellValue(nullSafe(opportunityMasterData.getIndustry()));
                row.createCell(col++).setCellValue(nullSafe(opportunityMasterData.getCountry()));
                row.createCell(col++).setCellValue(nullSafe(opportunityMasterData.getState()));
                row.createCell(col++).setCellValue(nullSafe(opportunityMasterData.getPartnerProjectTitle()));
                row.createCell(col++).setCellValue(nullSafe(opportunityMasterData.getCustomerBusinessProblem()));
                row.createCell(col++).setCellValue(nullSafe(opportunityMasterData.getSolutionOffered()));
                row.createCell(col++).setCellValue(nullSafe(opportunityMasterData.getUseCase()));
                setCellDouble(row.createCell(col++), opportunityMasterData.getEstimatedMonthlyRevenue());
                setCellDate(row.createCell(col++), opportunityMasterData.getTargetCloseDate(), dateStyle);
                row.createCell(col++).setCellValue(nullSafe(opportunityMasterData.getOpportunityType()));
                row.createCell(col++).setCellValue(nullSafe(opportunityMasterData.getDeliveryModel()));
                row.createCell(col++).setCellValue(nullSafe(opportunityMasterData.getSalesActivity()));
                row.createCell(col++).setCellValue(nullSafe(opportunityMasterData.getLineitemUsageaccountid()));
                row.createCell(col++).setCellValue(opportunityMasterData.getOpportunityRaised() != null
                        ? opportunityMasterData.getOpportunityRaised().toString() : "");
                setCellDate(row.createCell(col++), opportunityMasterData.getOpportunityRaisedDate(), dateStyle);
                row.createCell(col++).setCellValue(nullSafe(opportunityMasterData.getOpportunityRaisedBy()));
                row.createCell(col++).setCellValue(nullSafe(opportunityMasterData.getCloudPlatform()));
                row.createCell(col++).setCellValue(nullSafe(opportunityMasterData.getPartnerName()));
                setCellDate(row.createCell(col++), opportunityMasterData.getLoggedDate(), dateStyle);
                setCellDate(row.createCell(col++), opportunityMasterData.getCreatedDate(), dateStyle);
                setCellDate(row.createCell(col++), opportunityMasterData.getModifiedDate(), dateStyle);
            }

            // Auto-size columns for readability
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            return toByteArray(workbook);

        } catch (IOException e) {
            throw new GenericStatusException("Failed to generate master data Excel", HttpStatus.BAD_REQUEST.value());
        }
    }

    /**
     * Generates an Excel file containing raw opportunity data.
     *
     * @param records list of raw data records to export
     * @return byte array of the .xlsx file
     * @throws GenericStatusException if file generation fails
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
                ApnOpportunityRawData opportunityRawData = records.get(rowIdx);
                Row row = sheet.createRow(rowIdx + 1);

                int col = 0;
                row.createCell(col++).setCellValue(nullSafe(opportunityRawData.getLineitemUuid()));
                row.createCell(col++).setCellValue(nullSafe(opportunityRawData.getCustomerName()));
                row.createCell(col++).setCellValue(nullSafe(opportunityRawData.getLineitemUsageaccountid()));
                row.createCell(col++).setCellValue(nullSafe(opportunityRawData.getServiceName()));
                row.createCell(col++).setCellValue(nullSafe(opportunityRawData.getMycloudRegionname()));
                row.createCell(col++).setCellValue(nullSafe(opportunityRawData.getMycloudOperatingsystem()));
                row.createCell(col++).setCellValue(nullSafe(opportunityRawData.getLineitemResourceid()));
                row.createCell(col++).setCellValue(nullSafe(opportunityRawData.getMycloudInstancetype()));
                row.createCell(col++).setCellValue(nullSafe(opportunityRawData.getProductcode()));
                row.createCell(col++).setCellValue(nullSafe(opportunityRawData.getFinalNameTag()));
                row.createCell(col++).setCellValue(nullSafe(opportunityRawData.getFinalAutoscalingName()));
                row.createCell(col++).setCellValue(nullSafe(opportunityRawData.getKey()));
                row.createCell(col++).setCellValue(nullSafe(opportunityRawData.getWorkloadTitle()));
                row.createCell(col++).setCellValue(nullSafe(opportunityRawData.getWorkloadDescription()));

                // BigDecimal cost
                if (opportunityRawData.getTotalPeriodCost() != null) {
                    row.createCell(col++).setCellValue(opportunityRawData.getTotalPeriodCost().doubleValue());
                } else {
                    row.createCell(col++).setCellValue("");
                }

                setCellDate(row.createCell(col++), opportunityRawData.getResourceBirthDate(), dateStyle);
                setCellLong(row.createCell(col++), opportunityRawData.getActiveDaysCount());
                setCellLong(row.createCell(col++), opportunityRawData.getExpectedDays());
                setCellDate(row.createCell(col++), opportunityRawData.getLoggedDate(), dateStyle);
                row.createCell(col++).setCellValue(nullSafe(opportunityRawData.getCloudPlatform()));
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            return toByteArray(workbook);

        } catch (IOException e) {
            throw new GenericStatusException("Failed to generate raw data Excel", HttpStatus.BAD_REQUEST.value());
        }
    }

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
