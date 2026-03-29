package tokenrepository;

import java.awt.Color;

public class TokenManager {
    public static final String KEYWORD = "KEYWORD", IDENTIFIER = "IDENTIFIER";
    public static final String LITERAL = "LITERAL", STRING_LITERAL = "STRING_LITERAL", CHAR_LITERAL = "CHAR_LITERAL";
    public static final String SYMBOL = "SYMBOL", COMMENT = "COMMENT", UNKNOWN = "UNKNOWN";
    public static final String ARITHMETIC_OP = "ARITHMETIC_OP", ASSIGNMENT_OP = "ASSIGNMENT_OP";
    public static final String RELATIONAL_OP = "RELATIONAL_OP", LOGICAL_OP = "LOGICAL_OP", BITWISE_OP = "BITWISE_OP";

    public static String getTokenType(String lex) {
        if (lex.startsWith("//")) return COMMENT;
        if (KeywordToken.isKeyword(lex)) return KEYWORD;
        if (LiteralToken.isNumber(lex)) return LITERAL;
        if (LiteralToken.isStringLiteral(lex)) return STRING_LITERAL;
        if (LiteralToken.isCharLiteral(lex)) return CHAR_LITERAL;
        if (OperatorToken.isArithmetic(lex)) return ARITHMETIC_OP;
        if (OperatorToken.isAssignment(lex)) return ASSIGNMENT_OP;
        if (OperatorToken.isRelational(lex)) return RELATIONAL_OP;
        if (OperatorToken.isLogical(lex)) return LOGICAL_OP;
        if (OperatorToken.isBitwise(lex)) return BITWISE_OP;
        if (lex.length() == 1 && SymbolToken.isSymbol(lex.charAt(0))) return SYMBOL;
        if (IdentifierToken.isIdentifier(lex)) return IDENTIFIER;
        return UNKNOWN;
    }

    public static Color getTokenColor(String type, boolean isLightMode) {
        if (isLightMode) {
            switch(type) {
                case KEYWORD: return new Color(166, 38, 164); 
                case IDENTIFIER: return new Color(36, 41, 46); 
                case LITERAL: return new Color(0, 92, 197); 
                case STRING_LITERAL: 
                case CHAR_LITERAL: return new Color(3, 47, 98); 
                case SYMBOL: return new Color(36, 41, 46); 
                case COMMENT: return new Color(106, 115, 125); 
                case ARITHMETIC_OP: case ASSIGNMENT_OP: case RELATIONAL_OP:
                case LOGICAL_OP: case BITWISE_OP: return new Color(0, 92, 197); 
                default: return new Color(203, 36, 49); 
            }
        } else {
            switch(type) {
                case KEYWORD: return new Color(198, 120, 221);
                case IDENTIFIER: return new Color(97, 175, 239);
                case LITERAL: return new Color(209, 154, 102);
                case STRING_LITERAL: 
                case CHAR_LITERAL: return new Color(152, 195, 121);
                case SYMBOL: return new Color(171, 178, 191);
                case COMMENT: return new Color(92, 99, 112);
                case ARITHMETIC_OP: case ASSIGNMENT_OP: case RELATIONAL_OP:
                case LOGICAL_OP: case BITWISE_OP: return new Color(86, 182, 194);
                default: return new Color(224, 108, 117);
            }
        }
    }
}