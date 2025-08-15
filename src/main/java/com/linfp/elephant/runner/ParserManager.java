package com.linfp.elephant.runner;

import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ParserManager {
    private final MapAccessor MAP_AC = new MapAccessor();
    private final ParserContext PARSER_CTX = new TemplateParserContext();

    private final ReentrantReadWriteLock locker = new ReentrantReadWriteLock();
    private final Map<String, Expression> exps = new HashMap<>();
    private final ExpressionParser parser = new SpelExpressionParser();

    public String parse(String expStr, Map<String, Object> data) {
        var ctx = SimpleEvaluationContext.forPropertyAccessors(MAP_AC).withRootObject(data).build();
        var exp = getOrCreateExp(expStr);
        return exp.getValue(ctx, String.class);
    }

    private Expression getOrCreateExp(String expStr) {
        locker.readLock().lock();
        var exp = exps.get(expStr);
        locker.readLock().unlock();
        if (exp != null) {
            return exp;
        }
        locker.writeLock().lock();
        try {
            exp = exps.get(expStr);
            if (exp != null) {
                return exp;
            }
            exp = parser.parseExpression(expStr, PARSER_CTX);
            exps.put(expStr, exp);
        } finally {
            locker.writeLock().unlock();
        }
        return exp;
    }
}
