package com.skillsphere.skill.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.skillsphere.skill.entity.SkillAssessment;

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
                content.setStrokingColor(40 / 255f, 91 / 255f, 151 / 255f);
                content.setLineWidth(2.5f);
                content.addRect(30, 30, pageWidth - 60, pageHeight - 60);
                content.stroke();
                content.setStrokingColor(185 / 255f, 203 / 255f, 224 / 255f);
                content.setLineWidth(0.8f);
                content.addRect(42, 42, pageWidth - 84, pageHeight - 84);
                content.stroke();

                centered(content, font, pageWidth, 30, "SKILLSPHERE NEXUS", 500, 15, 37, 73, 111);
                centered(content, font, pageWidth, 30, "CERTIFICATE OF ACHIEVEMENT", 455, 25, 23, 39, 64);
                centered(content, font, pageWidth, 30, "This certifies that", 415, 13, 79, 95, 116);
                centered(content, font, pageWidth, 30, learnerName, 380, 27, 28, 73, 130);
                centered(content, font, pageWidth, 30, "has demonstrated verified achievement in", 335, 13, 79, 95, 116);
                centered(content, font, pageWidth, 30, skillName, 300, 23, 23, 39, 64);
                if (skillCategory != null && !skillCategory.isBlank()) {
                    centered(content, font, pageWidth, 30, "Category: " + skillCategory,
                            275, 12, 79, 95, 116);
                }
                centered(content, font, pageWidth, 30, "Verified proficiency level " + proficiencyLevel + " / 10",
                        245, 15, 40, 72, 112);
                centered(content, font, pageWidth, 30,
                        "Verified assessment: " + assessment.getScore() + " / " + assessment.getMaxScore()
                                + " (" + assessment.getAssessmentMethod() + ")",
                        213, 12, 79, 95, 116);
                centered(content, font, pageWidth, 30,
                        "Issued " + issueDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                                + "   |   Certificate " + certificateNumber,
                        175, 11, 79, 95, 116);
                centered(content, font, pageWidth, 30, "Verification code: " + verificationToken,
                        145, 10, 79, 95, 116);
                centered(content, font, pageWidth, 30, issuerName, 95, 14, 37, 73, 111);
            }
            document.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new java.io.UncheckedIOException("Could not generate the certificate PDF", exception);
        }
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
