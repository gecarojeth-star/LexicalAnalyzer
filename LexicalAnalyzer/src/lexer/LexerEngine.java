package lexer;

import java.util.ArrayList;
import java.util.List;
import tokenrepository.OperatorToken;
import tokenrepository.SymbolToken;

public class LexerEngine {
    
    public static List<TokenNode> tokenizeText(String text) {
        List<TokenNode> tokens = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inStr = false; 
        boolean inChar = false; 
        int curStart = -1;
        int currentLine = 1; 
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            
            if (c == '\n') currentLine++; 
            
            if (inStr) {
                cur.append(c);
                if (c == '\\' && i + 1 < text.length()) { 
                    cur.append(text.charAt(i + 1));
                    i++;
                } else if (c == '"') { 
                    tokens.add(new TokenNode(cur.toString(), curStart, currentLine));
                    cur = new StringBuilder();
                    inStr = false;
                }
                continue;
            }
            
            if (inChar) {
                cur.append(c);
                if (c == '\\' && i + 1 < text.length()) { 
                    cur.append(text.charAt(i + 1));
                    i++;
                } else if (c == '\'') { 
                    tokens.add(new TokenNode(cur.toString(), curStart, currentLine));
                    cur = new StringBuilder();
                    inChar = false;
                }
                continue;
            }
            
            if (c == '/' && i + 1 < text.length() && text.charAt(i + 1) == '/') {
                if (cur.length() > 0) { 
                    tokens.add(new TokenNode(cur.toString(), curStart, currentLine)); 
                    cur = new StringBuilder(); 
                }
                int endOfLine = text.indexOf('\n', i);
                if (endOfLine == -1) endOfLine = text.length();
                
                tokens.add(new TokenNode(text.substring(i, endOfLine), i, currentLine));
                i = endOfLine - 1; 
                continue;
            }
            
            if (c == '"') {
                if (cur.length() > 0) { tokens.add(new TokenNode(cur.toString(), curStart, currentLine)); cur = new StringBuilder(); }
                curStart = i; cur.append(c); inStr = true; continue;
            }
            
            if (c == '\'') {
                if (cur.length() > 0) { tokens.add(new TokenNode(cur.toString(), curStart, currentLine)); cur = new StringBuilder(); }
                curStart = i; cur.append(c); inChar = true; continue;
            }
            
            if (Character.isWhitespace(c)) {
                if (cur.length() > 0) { tokens.add(new TokenNode(cur.toString(), curStart, currentLine)); cur = new StringBuilder(); }
                continue;
            }
            
            if (OperatorToken.isOperatorChar(c) || SymbolToken.isSymbol(c)) {
                if (cur.length() > 0) { tokens.add(new TokenNode(cur.toString(), curStart, currentLine)); cur = new StringBuilder(); }
                if (i + 1 < text.length() && OperatorToken.isMultiOperator(String.valueOf(c) + text.charAt(i + 1))) {
                    tokens.add(new TokenNode(String.valueOf(c) + text.charAt(i + 1), i, currentLine)); 
                    i++; 
                    continue;
                }
                tokens.add(new TokenNode(String.valueOf(c), i, currentLine)); 
                continue;
            }
            
            if (cur.length() == 0) curStart = i;
            cur.append(c);
        }
        if (cur.length() > 0) tokens.add(new TokenNode(cur.toString(), curStart, currentLine));
        return tokens;
    }
}