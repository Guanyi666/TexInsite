package com.texinsite.engine.service;

import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.metadata.Metadata;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Duan Guanyi
 * @version 1.0.0
 * @date 2026/3/12
 */

/**
 * PDF智能解析器
 */

@Service
public class TikaParserService {
    public Map<String, String> parsePdf(String filePath) {
        Map<String, String> result = new HashMap<>();
        try (InputStream stream = new FileInputStream(new File(filePath))) {
            Parser parser = new AutoDetectParser();
            // 增加提取字符的上限，防止长篇论文被截断(-1表示无限制)
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();

            parser.parse(stream, handler, metadata, context);

            // 提取核心文本
            String rawText = handler.toString();
            // TODO: 后续应加入正则表达式清洗逻辑，防止行内公式被破坏
            result.put("content", rawText);

            // 提取元数据（不同PDF生成器存放的key可能不同，这里做兼容处理)
            result.put("title", metadata.get("title") != null ? metadata.get("title") : "Unknown Title");
            result.put("author", metadata.get("Author") != null ? metadata.get("Author") : "Unknown Author");

            String date = metadata.get("Creation-Date");
            result.put("year", date != null && date.length() >= 4 ? date.substring(0, 4) : "Unknown Year");

            return result;

        } catch (Exception e) {
            throw new RuntimeException("PDF解析异常：" + e.getMessage());
        }
    }
}
