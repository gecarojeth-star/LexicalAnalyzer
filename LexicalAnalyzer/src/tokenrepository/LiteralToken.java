package tokenrepository;

import java.util.regex.Pattern;

public class LiteralToken {
    private static final Pattern NUM_PAT = Pattern.compile("-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?");

    public static boolean isNumber(String lex) { return NUM_PAT.matcher(lex).matches(); }
    public static boolean isStringLiteral(String lex) { return lex.startsWith("\"") && lex.endsWith("\"") && lex.length() >= 2; }
    public static boolean isCharLiteral(String lex) { return lex.startsWith("'") && lex.endsWith("'") && lex.length() >= 2; }
}