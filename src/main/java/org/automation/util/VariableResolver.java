package org.automation.util;

import org.automation.driver.TestContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariableResolver {
    private static final Pattern VAR_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    public static String resolve(String input, TestContext context) {
        if (input == null) return null;
        Matcher matcher = VAR_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String varValue = context.getVar(matcher.group(1));
            if (varValue == null) throw new RuntimeException("Undefined variable: " + matcher.group(1));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(varValue));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String[] resolveArgs(String[] args, TestContext context) {
        if (args == null) return null;
        String[] resolved = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            resolved[i] = resolve(args[i], context);
        }
        return resolved;
    }
}