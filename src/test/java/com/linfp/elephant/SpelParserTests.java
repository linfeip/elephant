package com.linfp.elephant;

import com.linfp.elephant.runner.ParserManager;
import org.junit.jupiter.api.Test;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class SpelParserTests {

    @Test
    public void parseSpelTest() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Alice");
        data.put("age", 18);

        ExpressionParser parser = new SpelExpressionParser();
        var simpleCtx = SimpleEvaluationContext.forPropertyAccessors(new MapAccessor()).withRootObject(data).build();
        TemplateParserContext template = new TemplateParserContext(); // "#{", "}"

        var stdCtx = new StandardEvaluationContext();
        stdCtx.setRootObject(data);
        stdCtx.addPropertyAccessor(new MapAccessor());

        for (var i = 0; i < 1000_000; i++) {
            parser.parseExpression("#{name}", template).getValue(simpleCtx);
            parser.parseExpression("#{name}", template).getValue(stdCtx);
        }

        var start = System.nanoTime();
        var n = 10_000_000;
        for (var i = 0; i < n; i++) {
            parser.parseExpression("#{name}", template).getValue(simpleCtx);
        }

        var elapsed = System.nanoTime() - start;
        System.out.println("simple parser context: " + Duration.ofNanos(elapsed));

        start = System.nanoTime();
        for (var i = 0; i < n; i++) {
            parser.parseExpression("#{name}", template).getValue(stdCtx);
        }

        elapsed = System.nanoTime() - start;
        System.out.println("std parser context: " + Duration.ofNanos(elapsed));
    }

    @Test
    public void parserManagerTest() throws Exception {
        var pm = new ParserManager();
        var out = pm.parse("#{user.name}", Map.of("user", Map.of("name", "Alice", "age", 18)));
        System.out.println(out);
    }
}
