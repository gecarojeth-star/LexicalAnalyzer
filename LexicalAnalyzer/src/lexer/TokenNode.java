package lexer;

public class TokenNode {
    public String lexeme; 
    public int offset; 
    public int lineNumber;

    public TokenNode(String l, int o, int line) { 
        lexeme = l; 
        offset = o; 
        lineNumber = line; 
    }
}