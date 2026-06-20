package com.workflow.service;

import com.workflow.dto.ReportRequest;
import com.workflow.entity.*;
import com.workflow.repository.*;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {
    private final DailyLogRepository dailyLogRepository;
    private final EmployeeRepository employeeRepository;
    private final BranchRepository branchRepository;

    public ReportService(DailyLogRepository dlr, EmployeeRepository er, BranchRepository br) {
        this.dailyLogRepository = dlr; this.employeeRepository = er; this.branchRepository = br;
    }

    public Map<String, Object> generateReport(ReportRequest request) {
        LocalDate startDate = LocalDate.parse(request.getStartDate());
        LocalDate endDate = LocalDate.parse(request.getEndDate());
        if (endDate.isBefore(startDate))
            throw new RuntimeException("End date must be after start date");
        List<DailyLog> logs = dailyLogRepository.findByLogDateBetweenAndStatus(startDate, endDate, LogStatus.APPROVED);
        if (request.getEmployeeId() != null)
            logs = logs.stream().filter(l -> l.getEmployee().getId().equals(request.getEmployeeId()))
                    .collect(Collectors.toList());
        if (request.getBranchId() != null)
            logs = logs.stream().filter(l -> l.getBranch() != null
                    && l.getBranch().getId().equals(request.getBranchId())).collect(Collectors.toList());
        Set<Long> employeeIds = logs.stream().map(l -> l.getEmployee().getId()).collect(Collectors.toSet());
        long totalDays = logs.size();
        BigDecimal totalHours = logs.stream()
                .map(l -> l.getHoursWorked() != null ? l.getHoursWorked() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("startDate", request.getStartDate());
        summary.put("endDate", request.getEndDate());
        summary.put("employeeCount", employeeIds.size());
        summary.put("totalDays", totalDays);
        summary.put("totalHours", totalHours);
        List<Map<String, Object>> details = new ArrayList<>();
        for (DailyLog log : logs) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", log.getId());
            row.put("employeeId", log.getEmployee().getId());
            row.put("employeeName", log.getEmployee().getName());
            row.put("branch", log.getBranch() != null ? log.getBranch().getName() : null);
            row.put("date", log.getLogDate().toString());
            row.put("hoursWorked", log.getHoursWorked());
            row.put("category", log.getCategory().name());
            row.put("status", log.getStatus().name());
            row.put("submittedAt", log.getSubmittedAt() != null ? log.getSubmittedAt().toString() : null);
            details.add(row);
        }
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("summary", summary);
        report.put("details", details);
        return report;
    }

    public void exportReport(ReportRequest request, HttpServletResponse response) {
        Map<String, Object> reportData = generateReport(request);
        String format = request.getFormat() != null ? request.getFormat().toLowerCase() : "csv";
        try {
            if ("csv".equals(format)) {
                exportCsv(reportData, response);
            } else if ("pdf".equals(format)) {
                exportPdf(reportData, response);
            } else if ("excel".equals(format)) {
                exportExcel(reportData, response);
            } else {
                throw new RuntimeException("Unsupported format: " + format);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to export " + format.toUpperCase() + ": " + e.getMessage());
        }
    }

    private void exportCsv(Map<String, Object> reportData, HttpServletResponse response) throws Exception {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=report.csv");
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Employee ID,Employee Name,Branch,Date,Hours Worked,Category,Status,Submitted At\n");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> details = (List<Map<String, Object>>) reportData.get("details");
        for (Map<String, Object> row : details) {
            csv.append(sanitizeCsvCell(str(row.get("id")))).append(",");
            csv.append(sanitizeCsvCell(str(row.get("employeeId")))).append(",");
            csv.append(escapeCsv(sanitizeCsvCell(str(row.get("employeeName"))))).append(",");
            csv.append(escapeCsv(sanitizeCsvCell(str(row.get("branch"))))).append(",");
            csv.append(sanitizeCsvCell(str(row.get("date")))).append(",");
            csv.append(sanitizeCsvCell(str(row.get("hoursWorked")))).append(",");
            csv.append(escapeCsv(sanitizeCsvCell(str(row.get("category"))))).append(",");
            csv.append(escapeCsv(sanitizeCsvCell(str(row.get("status"))))).append(",");
            csv.append(escapeCsv(sanitizeCsvCell(str(row.get("submittedAt"))))).append("\n");
        }
        response.getOutputStream().write(csv.toString().getBytes());
        response.getOutputStream().flush();
    }

    @SuppressWarnings("unchecked")
    private void exportPdf(Map<String, Object> reportData, HttpServletResponse response) throws Exception {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=report.pdf");

        PDDocument doc = new PDDocument();
        try {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);
            float margin = 50;
            float yStart = page.getMediaBox().getHeight() - margin;
            float yPos = yStart;

            // Title
            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
            cs.beginText();
            cs.newLineAtOffset(margin, yPos);
            cs.showText("All in One & Network Solutions - Report");
            cs.endText();
            yPos -= 25;

            // Summary
            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            Map<String, Object> summary = (Map<String, Object>) reportData.get("summary");
            cs.beginText();
            cs.newLineAtOffset(margin, yPos);
            cs.showText("Period: " + summary.get("startDate") + " to " + summary.get("endDate"));
            cs.endText();
            yPos -= 14;
            cs.beginText();
            cs.newLineAtOffset(margin, yPos);
            cs.showText("Employees: " + summary.get("employeeCount") + " | Days: " + summary.get("totalDays") + " | Hours: " + summary.get("totalHours"));
            cs.endText();
            yPos -= 20;

            // Table header
            String[] headers = {"Employee", "Branch", "Date", "Category", "Hours", "Status"};
            float[] colWidths = {80, 60, 70, 70, 40, 50};
            float xPos;

            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 8);
            xPos = margin;
            for (int i = 0; i < headers.length; i++) {
                cs.addRect(xPos, yPos - 12, colWidths[i], 12);
                cs.stroke();
                cs.beginText();
                cs.newLineAtOffset(xPos + 2, yPos - 9);
                cs.showText(headers[i]);
                cs.endText();
                xPos += colWidths[i];
            }
            yPos -= 12;

            // Table rows
            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 7);
            List<Map<String, Object>> details = (List<Map<String, Object>>) reportData.get("details");
            for (Map<String, Object> row : details) {
                if (yPos < 60) {
                    cs.close();
                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    yPos = yStart;
                }
                xPos = margin;
                String[] values = {
                    str(row.get("employeeName")),
                    str(row.get("branch")),
                    str(row.get("date")),
                    row.get("category") != null ? ((String) row.get("category")).replace("_", " ") : "",
                    str(row.get("hoursWorked")),
                    str(row.get("status"))
                };
                for (int i = 0; i < values.length; i++) {
                    cs.addRect(xPos, yPos - 10, colWidths[i], 10);
                    cs.stroke();
                    cs.beginText();
                    cs.newLineAtOffset(xPos + 2, yPos - 8);
                    cs.showText(truncate(values[i], (int) colWidths[i] / 4));
                    cs.endText();
                    xPos += colWidths[i];
                }
                yPos -= 10;
            }
            cs.close();
            doc.save(response.getOutputStream());
        } finally {
            doc.close();
        }
    }

    @SuppressWarnings("unchecked")
    private void exportExcel(Map<String, Object> reportData, HttpServletResponse response) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=report.xlsx");

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            // Summary sheet
            Sheet summarySheet = wb.createSheet("Summary");
            Map<String, Object> summary = (Map<String, Object>) reportData.get("summary");
            int r = 0;
            for (Map.Entry<String, Object> entry : summary.entrySet()) {
                Row row = summarySheet.createRow(r++);
                row.createCell(0).setCellValue(entry.getKey());
                row.createCell(1).setCellValue(entry.getValue() != null ? entry.getValue().toString() : "");
            }

            // Details sheet
            Sheet detailsSheet = wb.createSheet("Details");
            String[] headers = {"Employee", "Branch", "Date", "Category", "Hours", "Status"};
            Row headerRow = detailsSheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            List<Map<String, Object>> details = (List<Map<String, Object>>) reportData.get("details");
            int rowNum = 1;
            for (Map<String, Object> row : details) {
                Row excelRow = detailsSheet.createRow(rowNum++);
                excelRow.createCell(0).setCellValue(str(row.get("employeeName")));
                excelRow.createCell(1).setCellValue(str(row.get("branch")));
                excelRow.createCell(2).setCellValue(str(row.get("date")));
                excelRow.createCell(3).setCellValue(row.get("category") != null ? ((String) row.get("category")).replace("_", " ") : "");
                excelRow.createCell(4).setCellValue(row.get("hoursWorked") != null ? Double.parseDouble(row.get("hoursWorked").toString()) : 0);
                excelRow.createCell(5).setCellValue(str(row.get("status")));
            }

            for (int i = 0; i < headers.length; i++) {
                detailsSheet.autoSizeColumn(i);
            }

            wb.write(response.getOutputStream());
        }
    }

    private String str(Object v) {
        return v != null ? v.toString() : "";
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String sanitizeCsvCell(String value) {
        if (value == null) return "";
        if (value.startsWith("=") || value.startsWith("+") || value.startsWith("-") || value.startsWith("@")) {
            return "'" + value;
        }
        return value;
    }

    private String truncate(String value, int maxChars) {
        if (value == null) return "";
        return value.length() <= maxChars ? value : value.substring(0, maxChars - 1) + "…";
    }
}
