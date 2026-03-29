package tokenrepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class OperatorToken {
    private static final Set<Character> OPERATOR_CHARS = new HashSet<>(Arrays.asList('=','+','-','*','/','%','<','>','!','&','|','^','~'));
    private static final Set<String> MULTI_OPERATORS = new HashSet<>(Arrays.asList("==","!=","<=",">=","&&","||","++","--","+=","-=","*=","/=","%="));
    private static final Set<String> ARITHMETIC = new HashSet<>(Arrays.asList("+", "-", "*", "/", "%", "++", "--"));
    private static final Set<String> ASSIGNMENT = new HashSet<>(Arrays.asList("=", "+=", "-=", "*=", "/=", "%="));
    private static final Set<String> RELATIONAL = new HashSet<>(Arrays.asList("==", "!=", "<", ">", "<=", ">="));
    private static final Set<String> LOGICAL = new HashSet<>(Arrays.asList("&&", "||", "!"));
    private static final Set<String> BITWISE = new HashSet<>(Arrays.asList("&", "|", "^", "~"));

    public static boolean isOperatorChar(char c) { return OPERATOR_CHARS.contains(c); }
    public static boolean isMultiOperator(String s) { return MULTI_OPERATORS.contains(s); }
    public static boolean isArithmetic(String s) { return ARITHMETIC.contains(s); }
    public static boolean isAssignment(String s) { return ASSIGNMENT.contains(s); }
    public static boolean isRelational(String s) { return RELATIONAL.contains(s); }
    public static boolean isLogical(String s) { return LOGICAL.contains(s); }
    public static boolean isBitwise(String s) { return BITWISE.contains(s); }
}