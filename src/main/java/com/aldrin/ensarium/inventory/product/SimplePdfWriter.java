package com.aldrin.ensarium.inventory.product;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class SimplePdfWriter {

    private static final float PAGE_WIDTH = 842f;
    private static final float PAGE_HEIGHT = 595f;
    private static final float MARGIN_LEFT = 24f;
    private static final float MARGIN_TOP = 28f;
    private static final float MARGIN_BOTTOM = 24f;
    private static final float TITLE_FONT = 14f;
    private static final float BODY_FONT = 7f;
    private static final float LINE_HEIGHT = 9f;
    private static final int BODY_LINES_PER_PAGE = 55;

    private SimplePdfWriter() {
    }

    static void writeMonospaceReport(Path file, String title, List<String> bodyLines) throws IOException {
        List<List<String>> pages = paginate(bodyLines, BODY_LINES_PER_PAGE);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();

        write(out, "%PDF-1.4\n%\u00E2\u00E3\u00CF\u00D3\n");

        int pageCount = pages.size();
        int totalObjects = 3 + pageCount * 2;

        offsets.add(0);
        writeObject(out, offsets, 1, "<< /Type /Catalog /Pages 2 0 R >>");

        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < pageCount; i++) {
            int pageObj = 4 + (i * 2);
            kids.append(pageObj).append(" 0 R ");
        }
        writeObject(out, offsets, 2, "<< /Type /Pages /Count " + pageCount + " /Kids [ " + kids + "] >>");
        writeObject(out, offsets, 3, "<< /Type /Font /Subtype /Type1 /BaseFont /Courier >>");

        for (int i = 0; i < pageCount; i++) {
            int pageObj = 4 + (i * 2);
            int contentObj = pageObj + 1;
            String content = buildPageContent(title, pages.get(i), i + 1, pageCount);
            byte[] contentBytes = content.getBytes(StandardCharsets.US_ASCII);
            writeObject(out, offsets, pageObj,
                    "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + PAGE_WIDTH + " " + PAGE_HEIGHT + "] "
                            + "/Resources << /Font << /F1 3 0 R >> >> /Contents " + contentObj + " 0 R >>");
            writeStreamObject(out, offsets, contentObj, contentBytes);
        }

        int xrefStart = out.size();
        write(out, "xref\n0 " + (totalObjects + 1) + "\n");
        write(out, "0000000000 65535 f \n");
        for (int i = 1; i <= totalObjects; i++) {
            write(out, String.format("%010d 00000 n \n", offsets.get(i)));
        }
        write(out, "trailer\n<< /Size " + (totalObjects + 1) + " /Root 1 0 R >>\nstartxref\n" + xrefStart + "\n%%EOF");

        Files.write(file, out.toByteArray());
    }

    private static List<List<String>> paginate(List<String> lines, int bodyLinesPerPage) {
        List<List<String>> pages = new ArrayList<>();
        if (lines.isEmpty()) {
            pages.add(new ArrayList<>());
            return pages;
        }
        List<String> current = new ArrayList<>();
        for (String line : lines) {
            if (current.size() >= bodyLinesPerPage) {
                pages.add(current);
                current = new ArrayList<>();
            }
            current.add(line == null ? "" : line);
        }
        if (!current.isEmpty()) {
            pages.add(current);
        }
        return pages;
    }

    private static String buildPageContent(String title, List<String> lines, int pageNo, int pageCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("BT\n");
        sb.append("/F1 ").append(TITLE_FONT).append(" Tf\n");
        sb.append("1 0 0 1 ").append(MARGIN_LEFT).append(" ").append(PAGE_HEIGHT - MARGIN_TOP).append(" Tm\n");
        sb.append("(").append(escape(title)).append(") Tj\n");

        float y = PAGE_HEIGHT - MARGIN_TOP - 18f;
        sb.append("/F1 ").append(BODY_FONT).append(" Tf\n");
        for (String line : lines) {
            if (y < MARGIN_BOTTOM) {
                break;
            }
            sb.append("1 0 0 1 ").append(MARGIN_LEFT).append(" ").append(y).append(" Tm\n");
            sb.append("(").append(escape(line)).append(") Tj\n");
            y -= LINE_HEIGHT;
        }
        sb.append("1 0 0 1 ").append(PAGE_WIDTH - 120f).append(" ").append(12f).append(" Tm\n");
        sb.append("(Page ").append(pageNo).append(" of ").append(pageCount).append(") Tj\n");
        sb.append("ET\n");
        return sb.toString();
    }

    private static String escape(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            switch (ch) {
                case '\\' -> sb.append("\\\\");
                case '(' -> sb.append("\\(");
                case ')' -> sb.append("\\)");
                default -> {
                    if (ch < 32 || ch > 126) {
                        sb.append('?');
                    } else {
                        sb.append(ch);
                    }
                }
            }
        }
        return sb.toString();
    }

    private static void writeObject(ByteArrayOutputStream out, List<Integer> offsets, int objNum, String content) throws IOException {
        offsets.add(out.size());
        write(out, objNum + " 0 obj\n" + content + "\nendobj\n");
    }

    private static void writeStreamObject(ByteArrayOutputStream out, List<Integer> offsets, int objNum, byte[] content) throws IOException {
        offsets.add(out.size());
        write(out, objNum + " 0 obj\n<< /Length " + content.length + " >>\nstream\n");
        out.write(content);
        write(out, "endstream\nendobj\n");
    }

    private static void write(ByteArrayOutputStream out, String text) throws IOException {
        out.write(text.getBytes(StandardCharsets.US_ASCII));
    }
}
