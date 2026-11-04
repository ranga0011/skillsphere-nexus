package com.skillsphere.skill.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.skillsphere.skill.entity.SkillAssessment;
import com.skillsphere.skill.service.qrcode.QrCode;

@Component
public class CertificatePdfGenerator {

    private final String configuredFontPath;

    public CertificatePdfGenerator(@Value("${app.certificates.font-path:}") String configuredFontPath) {
        this.configuredFontPath = configuredFontPath;
    }

    public byte[] generate(String learnerName, String skillName, String skillCategory, String issuerName,
            String certificateNumber, String verificationToken, int proficiencyLevel,
            SkillAssessment assessment, LocalDate issueDate) {
        try (PDDocument document = new PDDocument();
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
            document.addPage(page);
            PDFont font = PDType0Font.load(document, resolveFont());
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                // Outer ornamental frame
                content.setStrokingColor(30 / 255f, 64 / 255f, 120 / 255f);
                content.setLineWidth(2.5f);
                content.addRect(28, 28, pageWidth - 56, pageHeight - 56);
                content.stroke();

                // Inner thin gold accent frame
                content.setStrokingColor(217 / 255f, 158 / 255f, 43 / 255f);
                content.setLineWidth(1.0f);
                content.addRect(36, 36, pageWidth - 72, pageHeight - 72);
                content.stroke();

                // Innermost hairline frame
                content.setStrokingColor(203 / 255f, 213 / 255f, 225 / 255f);
                content.setLineWidth(0.6f);
                content.addRect(42, 42, pageWidth - 84, pageHeight - 84);
                content.stroke();

                // Top Official Emblem / Logo
                drawCertificateLogo(content, font, pageWidth / 2f, 520);

                // Main Typography
                centered(content, font, pageWidth, 50, "SKILLSPHERE NEXUS", 492, 13, 30, 64, 120);
                centered(content, font, pageWidth, 50, "CERTIFICATE OF ACHIEVEMENT", 456, 23, 15, 23, 42);
                centered(content, font, pageWidth, 50, "This certifies that", 422, 12, 100, 116, 139);
                centered(content, font, pageWidth, 50, learnerName, 388, 25, 29, 78, 216);
                centered(content, font, pageWidth, 50, "has demonstrated verified mastery and competency in", 350, 12, 100, 116, 139);
                centered(content, font, pageWidth, 50, skillName, 318, 21, 15, 23, 42);

                if (skillCategory != null && !skillCategory.isBlank()) {
                    centered(content, font, pageWidth, 50, "Category: " + skillCategory, 294, 11, 100, 116, 139);
                }

                centered(content, font, pageWidth, 50, "Verified Proficiency Level " + proficiencyLevel + " / 10", 266, 14, 30, 64, 120);
                centered(content, font, pageWidth, 50,
                        "Assessment Result: " + assessment.getScore() + " / " + assessment.getMaxScore()
                                + " (" + assessment.getAssessmentMethod() + ")",
                        240, 11, 100, 116, 139);

                centered(content, font, pageWidth, 50,
                        "Issued on " + issueDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                                + "   |   Certificate ID: " + certificateNumber,
                        206, 10.5f, 71, 85, 105);

                centered(content, font, pageWidth, 50,
                        "Public Verification Token: " + verificationToken,
                        186, 9.5f, 100, 116, 139);

                // Issuer signature & title line
                centered(content, font, pageWidth, 50, issuerName, 106, 13, 30, 64, 120);
                content.setStrokingColor(148 / 255f, 163 / 255f, 184 / 255f);
                content.setLineWidth(1.0f);
                content.moveTo(pageWidth / 2f - 90, 100);
                content.lineTo(pageWidth / 2f + 90, 100);
                content.stroke();
                centered(content, font, pageWidth, 50, "Authorized Institutional Issuer", 86, 9, 100, 116, 139);

                // Bottom Left Official Verification Seal
                drawOfficialSeal(content, font, 115, 115);

                // Bottom Right Scannable Verification QR Code
                String verificationUrl = "http://localhost:8080/api/certificates/verify/" + verificationToken;
                drawVerificationQrCode(content, font, verificationUrl, pageWidth - 165, 75);
            }
            document.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new java.io.UncheckedIOException("Could not generate the certificate PDF", exception);
        }
    }

    /**
     * Draws the top SkillSphere Nexus crest/logo with shield and gold star.
     */
    private void drawCertificateLogo(PDPageContentStream content, PDFont font, float cx, float cy) throws IOException {
        // Shield background
        content.setNonStrokingColor(30 / 255f, 64 / 255f, 120 / 255f);
        content.setStrokingColor(217 / 255f, 158 / 255f, 43 / 255f);
        content.setLineWidth(1.5f);

        float w = 24;
        float h = 28;
        float x = cx - w / 2f;
        float y = cy - h / 2f;

        content.moveTo(x, y + h);
        content.lineTo(x + w, y + h);
        content.lineTo(x + w, y + h * 0.45f);
        content.curveTo(x + w, y + h * 0.15f, x + w * 0.5f, y, cx, y);
        content.curveTo(x + w * 0.5f, y, x, y + h * 0.15f, x, y + h * 0.45f);
        content.closePath();
        content.fillAndStroke();

        // Monogram "S" inside shield
        content.setNonStrokingColor(1f, 1f, 1f);
        content.beginText();
        content.setFont(font, 14);
        content.newLineAtOffset(cx - 4.5f, cy - 5.5f);
        content.showText("S");
        content.endText();

        // Little gold crown star above shield
        content.setNonStrokingColor(217 / 255f, 158 / 255f, 43 / 255f);
        content.addRect(cx - 2, cy + h / 2f + 2, 4, 4);
        content.fill();
    }

    /**
     * Draws an official golden certified seal with decorative ribbon tails.
     */
    private void drawOfficialSeal(PDPageContentStream content, PDFont font, float cx, float cy) throws IOException {
        // Decorative ribbons
        content.setNonStrokingColor(30 / 255f, 64 / 255f, 120 / 255f);
        // Left ribbon tail
        content.moveTo(cx - 14, cy - 16);
        content.lineTo(cx - 20, cy - 48);
        content.lineTo(cx - 10, cy - 42);
        content.lineTo(cx - 2, cy - 48);
        content.lineTo(cx - 6, cy - 16);
        content.closePath();
        content.fill();

        // Right ribbon tail
        content.moveTo(cx + 6, cy - 16);
        content.lineTo(cx + 2, cy - 48);
        content.lineTo(cx + 10, cy - 42);
        content.lineTo(cx + 20, cy - 48);
        content.lineTo(cx + 14, cy - 16);
        content.closePath();
        content.fill();

        // Outer Gold Medallion
        content.setNonStrokingColor(234 / 255f, 179 / 255f, 8 / 255f);
        content.setStrokingColor(180 / 255f, 83 / 255f, 9 / 255f);
        content.setLineWidth(1.2f);
        drawCircle(content, cx, cy, 26, true, true);

        // Inner Navy Disc
        content.setNonStrokingColor(30 / 255f, 64 / 255f, 120 / 255f);
        drawCircle(content, cx, cy, 21, true, false);

        // Inner Gold Ring
        content.setStrokingColor(234 / 255f, 179 / 255f, 8 / 255f);
        content.setLineWidth(0.8f);
        drawCircle(content, cx, cy, 18, false, true);

        // Central Gold Vector Star
        content.setNonStrokingColor(254 / 255f, 240 / 255f, 138 / 255f);
        drawStar(content, cx, cy, 9f, 3.8f, 5);

        // Seal Caption
        centeredAt(content, font, cx, cy - 58, "OFFICIAL SEAL", 7.5f, 30, 64, 120);
        centeredAt(content, font, cx, cy - 67, "Verified Credential", 6.5f, 100, 116, 139);
    }

    /**
     * Draws the Scannable Verification QR Code using vector boxes.
     */
    private void drawVerificationQrCode(PDPageContentStream content, PDFont font, String url, float qrx, float qry)
            throws IOException {
        QrCode qr = QrCode.encodeText(url, QrCode.Ecc.MEDIUM);
        int qrSize = qr.size;
        float boxWidth = 72f;
        float cellSize = boxWidth / qrSize;

        // Clean white background card for QR code
        content.setNonStrokingColor(1f, 1f, 1f);
        content.setStrokingColor(203 / 255f, 213 / 255f, 225 / 255f);
        content.setLineWidth(0.8f);
        content.addRect(qrx - 5, qry - 5, boxWidth + 10, boxWidth + 10);
        content.fillAndStroke();

        // Draw QR Modules in deep navy
        content.setNonStrokingColor(15 / 255f, 23 / 255f, 42 / 255f);
        for (int y = 0; y < qrSize; y++) {
            for (int x = 0; x < qrSize; x++) {
                if (qr.getModule(x, y)) {
                    content.addRect(qrx + x * cellSize, qry + (qrSize - 1 - y) * cellSize, cellSize, cellSize);
                    content.fill();
                }
            }
        }

        // Caption under QR
        centeredAt(content, font, qrx + boxWidth / 2f, qry - 14, "SCAN TO VERIFY", 7.5f, 30, 64, 120);
        centeredAt(content, font, qrx + boxWidth / 2f, qry - 22, "Instant Authenticity Check", 6.5f, 100, 116, 139);
    }

    private void drawCircle(PDPageContentStream content, float cx, float cy, float r, boolean fill, boolean stroke)
            throws IOException {
        float k = 0.552284749831f * r;
        content.moveTo(cx - r, cy);
        content.curveTo(cx - r, cy + k, cx - k, cy + r, cx, cy + r);
        content.curveTo(cx + k, cy + r, cx + r, cy + k, cx + r, cy);
        content.curveTo(cx + r, cy - k, cx + k, cy - r, cx, cy - r);
        content.curveTo(cx - k, cy - r, cx - r, cy - k, cx - r, cy);
        if (fill && stroke) {
            content.fillAndStroke();
        } else if (fill) {
            content.fill();
        } else if (stroke) {
            content.stroke();
        }
    }

    private void drawStar(PDPageContentStream content, float cx, float cy, float outerR, float innerR, int points)
            throws IOException {
        double angleStep = Math.PI / points;
        double startAngle = Math.PI / 2; // top point
        content.moveTo(
                (float) (cx + outerR * Math.cos(startAngle)),
                (float) (cy + outerR * Math.sin(startAngle)));
        for (int i = 1; i < points * 2; i++) {
            double r = (i % 2 == 1) ? innerR : outerR;
            double a = startAngle + i * angleStep;
            content.lineTo(
                    (float) (cx + r * Math.cos(a)),
                    (float) (cy + r * Math.sin(a)));
        }
        content.closePath();
        content.fill();
    }

    private void centeredAt(PDPageContentStream content, PDFont font, float cx, float y, String text, float size,
            int red, int green, int blue) throws IOException {
        float textWidth = font.getStringWidth(text) / 1000f * size;
        content.setNonStrokingColor(red / 255f, green / 255f, blue / 255f);
        content.beginText();
        content.setFont(font, size);
        content.newLineAtOffset(cx - textWidth / 2f, y);
        content.showText(text);
        content.endText();
    }

    private File resolveFont() throws IOException {
        if (configuredFontPath != null && !configuredFontPath.isBlank()) {
            File configuredFont = new File(configuredFontPath);
            if (configuredFont.isFile()) {
                return configuredFont;
            }
            throw new IOException("Configured certificate font file does not exist");
        }
        String windowsDirectory = System.getenv("WINDIR");
        List<String> candidates = List.of(
                windowsDirectory == null ? "" : windowsDirectory + "\\Fonts\\arial.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "/usr/share/fonts/truetype/liberation2/LiberationSans-Regular.ttf",
                "/Library/Fonts/Arial.ttf");
        for (String candidate : candidates) {
            File fontFile = new File(candidate);
            if (fontFile.isFile()) {
                return fontFile;
            }
        }
        throw new IOException("No Unicode sans-serif font found; configure CERTIFICATE_FONT_PATH");
    }

    private void centered(PDPageContentStream content, PDFont font, float pageWidth, float margin,
            String text, float y, float size, int red, int green, int blue) throws IOException {
        String safeText = text.replaceAll("\\p{Cc}", " ").trim();
        float maxWidth = pageWidth - margin * 2;
        float fittedSize = size;
        while (font.getStringWidth(safeText) / 1000 * fittedSize > maxWidth && fittedSize > 9) {
            fittedSize -= 1;
        }
        List<String> lines = wrapText(safeText, font, fittedSize, maxWidth);
        content.setNonStrokingColor(red / 255f, green / 255f, blue / 255f);
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            float textWidth = font.getStringWidth(line) / 1000 * fittedSize;
            float x = Math.max(margin, (pageWidth - textWidth) / 2);
            content.beginText();
            content.setFont(font, fittedSize);
            content.newLineAtOffset(x, y - index * (fittedSize + 2));
            content.showText(line);
            content.endText();
        }
    }

    private List<String> wrapText(String text, PDFont font, float size, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split("\\s+")) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (font.getStringWidth(candidate) / 1000 * size <= maxWidth) {
                line.setLength(0);
                line.append(candidate);
            } else {
                if (!line.isEmpty()) {
                    lines.add(line.toString());
                    line.setLength(0);
                }
                for (int offset = 0; offset < word.length();) {
                    int codePoint = word.codePointAt(offset);
                    String unit = new String(Character.toChars(codePoint));
                    String part = line + unit;
                    if (font.getStringWidth(part) / 1000 * size > maxWidth && !line.isEmpty()) {
                        lines.add(line.toString());
                        line.setLength(0);
                    }
                    line.append(unit);
                    offset += Character.charCount(codePoint);
                }
            }
        }
        if (!line.isEmpty()) {
            lines.add(line.toString());
        }
        return lines;
    }
}
