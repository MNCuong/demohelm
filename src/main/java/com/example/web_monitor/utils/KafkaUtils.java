package com.example.web_monitor.utils;

import com.example.web_monitor.dto.ParsedKafkaMessageDto;
import com.example.web_monitor.kafka.MessageProducer;
import com.example.web_monitor.model.entities.MessageEntity;
import com.example.web_monitor.model.entities.RptlogEntity;
import com.example.web_monitor.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Slf4j
public final class KafkaUtils {

    private static final Pattern BLOCK1 =
            Pattern.compile("\\{1:(.*?)\\}");
    private static final Pattern BLOCK2 =
            Pattern.compile("\\{2:(.*?)\\}");
    private static final Pattern BLOCK4 =
            Pattern.compile("\\{4:(.*?)\\-\\}", Pattern.DOTALL);

    private static final Pattern SENDER_BIC =
            Pattern.compile("^F01([A-Z0-9]{8})");

    private static final Pattern MT_TYPE =
            Pattern.compile("[IO](\\d{3})");

    private static final Pattern TAG_20 =
            Pattern.compile(":20:([^\\r\\n]+)");

    private static final Pattern TAG_20C =
            Pattern.compile("\\s*:20C::(PREV|RELA)//([^\\r\\n]+)");

    private static final Pattern TRD_DATE_PATTERN =
            Pattern.compile("/TRD_DATE/(\\d+)");

    private static final Pattern PAYLOAD_PATTERN =
            Pattern.compile("<payload>([\\s\\S]*?)</payload>");
    private static final Pattern BLOCK1_PATTERN =
            Pattern.compile("\\{1:(F\\d{2})");
    private static final Pattern BLOCK2_PATTERN =
            Pattern.compile("\\{2:([A-Z])[^}]*\\}");
    @Value("${kafka.topic.report.file}")
    private static String reportFile;

    private KafkaUtils() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }

    /**
     * Build topic name cho resend message theo format: integration.{usrName}.InMessageResend
     *
     * @param usrName Username từ bảng TXMSGLOG
     * @return Topic name được format
     */
    public static String buildResendTopic(String usrName) {
        return String.format("integration.%s.InMessageResend", usrName);
    }

    /**
     * Trích xuất giá trị tag 25 từ payload SWIFT
     */
    public static String extractTag25D(String payload) {
        if (payload == null || payload.isBlank()) {
            return null;
        }

        // :25D::IPRC//REJT
        Pattern pattern = Pattern.compile(
                ":25D::IPRC//([^\\r\\n]+)"
        );

        Matcher matcher = pattern.matcher(payload);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    public static String extractTag79(String payload) {
        if (payload == null) {
            return null;
        }

        Pattern pattern = Pattern.compile("STATUS/([^\\r\\n/]+)");
        Matcher matcher = pattern.matcher(payload);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }


    public static String buildHeaderResend(String usrName) {
        try {
            Map<String, Object> headerMap = new HashMap<>();
            headerMap.put("spring_json_header_types", "{\"QUEUE_DESTINATION\":\"java.lang.String\"}");
            headerMap.put("QUEUE_DESTINATION", "disruptor-vm:integration." + usrName + ".InMessage");

            // Chuyển map sang JSON string
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(headerMap);

        } catch (Exception e) {
            throw new RuntimeException("Failed to build header for resend", e);
        }
    }

    /**
     * Build topic name generic theo pattern: integration.{identifier}.{suffix}
     *
     * @param identifier Identifier (ví dụ: username, participant code, etc.)
     * @param suffix     Suffix của topic (ví dụ: InMessageResend, OutMessage, etc.)
     * @return Topic name được format
     */
    public static String buildIntegrationTopic(String identifier, String suffix) {
        return String.format("integration.%s.%s", identifier, suffix);
    }

    public static String extractPayloadv2(String xmlString) {
        if (xmlString == null || xmlString.isEmpty()) {
            return "";
        }

        try {
            Pattern pattern = Pattern.compile("<payload>(.*?)</payload>", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(xmlString);

            if (matcher.find()) {
                String payload = matcher.group(1).trim();
                return payload;
            }

            int start = xmlString.indexOf("<payload>");
            int end = xmlString.indexOf("</payload>");

            if (start != -1 && end != -1 && end > start) {
                start += "<payload>".length();
                return xmlString.substring(start, end).trim();
            }

            return "";

        } catch (Exception e) {
            log.error("Error extracting payload from XML", e);
            return "";
        }
    }

    public static String extractPayload(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document document = builder.parse(
                    new InputSource(new StringReader(xml))
            );

            NodeList payloadNodes = document.getElementsByTagName("payload");
            if (payloadNodes.getLength() > 0) {
                return payloadNodes.item(0).getTextContent().trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String block1(String payload) {
        return extract(BLOCK1, payload);
    }

    public static String block2(String payload) {
        return extract(BLOCK2, payload);
    }

    public static String block4(String payload) {
        return extract(BLOCK4, payload);
    }

    private static String extract(Pattern p, String src) {
        if (src == null) return null;
        Matcher m = p.matcher(src);
        return m.find() ? m.group(1).trim() : null;
    }

    public static String senderBic(String payload) {
        String b1 = block1(payload);
        if (b1 == null) return null;
        Matcher m = SENDER_BIC.matcher(b1);
        return m.find() ? m.group(1) : null;
    }

    public static String refSenderBic(String xmlString) {
        if (xmlString == null || xmlString.isEmpty()) {
            return "";
        }

        log.info("xmlString: {}", xmlString);

        try {
            Pattern pattern = Pattern.compile("<usrname>(.*?)</usrname>", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(xmlString);

            if (matcher.find()) {
                String username = matcher.group(1).trim();
                log.info("username: {}", username);
                return username;
            }

            return "";

        } catch (Exception e) {
            log.error("Error extracting usrname from XML", e);
            return "";
        }
    }

    public static String msgType(String payload) {
        String b2 = block2(payload);
        if (b2 == null) return null;
        Matcher m = MT_TYPE.matcher(b2);
        return m.find() ? m.group(1) : null;
    }

    public static String msgRef(String payload) {
        String b4 = block4(payload);
        if (b4 == null) return null;

        Matcher m20c = TAG_20C.matcher(b4);
        while (m20c.find()) {
            String qualifier = m20c.group(1);
            if ("PREV".equals(qualifier) || "RELA".equals(qualifier)) {
                return m20c.group(2).trim();
            }
        }

        Matcher m20 = TAG_20.matcher(b4);
        if (m20.find()) {
            return m20.group(1).trim();
        }

        return null;
    }

    public static String relatedRef(String payload) {
        String b4 = block4(payload);
        if (b4 == null) return "";
        Matcher m = TAG_20C.matcher(b4);
        while (m.find()) {
            if ("PREV".equals(m.group(1)) || "RELA".equals(m.group(1))) {
                return m.group(2).trim();
            }
        }

        return "";
    }

    public static LocalDate extractTradeDateFromBlock4(String block4) {
        Matcher matcher = TRD_DATE_PATTERN.matcher(block4);
        log.info("=================block4: {}", block4);

        if (!matcher.find()) {
            throw new IllegalArgumentException("TRD_DATE tag not found");
        }

        String dateStr = matcher.group(1);
        log.info("=--------------------dateStr: {}", dateStr);

        if (dateStr.length() != 8) {
            throw new IllegalArgumentException("Invalid date length");
        }

        try {
            return LocalDate.parse(
                    dateStr,
                    DateTimeFormatter.BASIC_ISO_DATE
            );
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format");
        }
    }


    public static String extractCoreRefId(String xml) {
        return extractSimpleTag(xml, "corerefid");
    }

    public static String extractUsrName(String xml) {
        return extractSimpleTag(xml, "usrname");
    }

    private static String extractSimpleTag(String xml, String tag) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));

            NodeList nodes = doc.getElementsByTagName(tag);
            if (nodes.getLength() > 0) {
                return nodes.item(0).getTextContent().trim();
            }
        } catch (Exception e) {
            log.warn("Failed to parse xml: {}", xml, e);
        }
        return null;
    }

    public static String safeError(Exception ex) {
        return ex.getMessage() != null
                ? ex.getMessage().substring(0, Math.min(500, ex.getMessage().length()))
                : "UNKNOWN_ERROR";
    }


    public static boolean validateTradeDate(
            LocalDate tradeDate
//            String senderBic,
//            String tag20,
//            MessageProducer messageProducer,
//            String key
    ) {
        if (tradeDate == null || tradeDate.isAfter(LocalDate.now())) {
//            messageProducer.sendMessage(
//                    reportFile,
//                    key,
//                    "REJT." + senderBic + "." + tag20
//            );
            return false;
        }
        return true;
    }

    private static String buildFileName(String senderBic, long ts, String tag20) {
        return String.format("PACK.%s_%d.%s.csv", senderBic, ts, tag20);
    }

    public static String updateStatusKeepFormat(String xml, String newStatus) {
        if (xml == null) return null;

        if (xml.contains("<status>")) {
            // <status>E</status> -> <status>N</status>
            return xml.replaceAll(
                    "<status>.*?</status>",
                    "<status>" + newStatus + "</status>"
            );
        }

        // nếu chưa có <status> -> chèn sau <msgpriority>
        return xml.replaceFirst(
                "(</msgpriority>)",
                "$1\n    <status>" + newStatus + "</status>"
        );
    }

    public static String clearExtensionKeepOpenClose(String xml) {
        if (xml == null) return null;

        // <extension>...</extension> -> <extension></extension>
        xml = xml.replaceAll(
                "(?s)<extension[^>]*>.*?</extension>",
                "<extension></extension>"
        );

        // <extension/> -> <extension></extension>
        xml = xml.replaceAll(
                "<extension\\s*/>",
                "<extension></extension>"
        );

        return xml;
    }

    public static String extractOrgSeqIdFromPayload(String xml) {
        if (xml == null || xml.isBlank()) {
            return null;
        }

        // Lấy riêng phần payload
        Pattern payloadPattern = Pattern.compile(
                "<payload>(.*?)</payload>",
                Pattern.DOTALL
        );
        Matcher payloadMatcher = payloadPattern.matcher(xml);

        if (!payloadMatcher.find()) {
            return null;
        }

        String payload = payloadMatcher.group(1);

        // Lấy giá trị sau :20:
        Pattern refPattern = Pattern.compile(":20:([^\\r\\n]+)");
        Matcher refMatcher = refPattern.matcher(payload);

        if (refMatcher.find()) {
            return refMatcher.group(1).trim();
        }

        return null;
    }


    public static boolean cannotResend(String xml) {
        if (xml == null || xml.isBlank()) {
            return true;
        }

        //  Tách payload
        Matcher payloadMatcher = PAYLOAD_PATTERN.matcher(xml);
        if (!payloadMatcher.find()) {
            return true;
        }

        String payload = payloadMatcher.group(1);

        // Check block 1 = F21
        Matcher block1Matcher = BLOCK1_PATTERN.matcher(payload);
        if (block1Matcher.find()) {
            String block1Type = block1Matcher.group(1);
            log.info("block1Type:{}", block1Type);
            if ("F21".equals(block1Type)) {
                return true;
            }
        }

        // Check block 2 = O
        Matcher block2Matcher = BLOCK2_PATTERN.matcher(payload);
        if (block2Matcher.find()) {
            String block2Type = block2Matcher.group(1);
            log.info("block2Type:{}", block2Type);
            if ("O".equals(block2Type)) {
                return true;
            }
        }

        return false;
    }

    public static String extract20C_RELA(String payload) {
        if (payload == null || payload.isBlank()) {
            return null;
        }

        // :20C::RELA//<value>
        Pattern pattern = Pattern.compile(
                ":20C::RELA//([^\\r\\n]+)"
        );

        Matcher matcher = pattern.matcher(payload);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    public static String extractRef20(String payload) {

        if (payload == null) {
            return null;
        }

        // Case 1 :20C::SEME//VALUE
        Pattern p1 = Pattern.compile(":20C::SEME//([^\\r\\n]+)");
        Matcher m1 = p1.matcher(payload);

        if (m1.find()) {
            return m1.group(1).trim();
        }

        // Case 2 :20:VALUE
        Pattern p2 = Pattern.compile(":20:([^\\r\\n]+)");
        Matcher m2 = p2.matcher(payload);

        if (m2.find()) {
            return m2.group(1).trim();
        }

        return null;
    }
}
