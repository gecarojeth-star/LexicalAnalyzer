package tokenrepository;

import java.util.regex.Pattern;

public class IdentifierToken {
    private static final Pattern ID_PAT = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");

    public static boolean isIdentifier(String lex) {
        return ID_PAT.matcher(lex).matches();
    }
}