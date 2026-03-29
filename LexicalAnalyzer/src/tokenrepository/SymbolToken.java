package tokenrepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SymbolToken {
    private static final Set<Character> SYMBOLS = new HashSet<>(Arrays.asList(';',',','(',')','{','}','[',']','.',':','@','?'));

    public static boolean isSymbol(char c) {
        return SYMBOLS.contains(c);
    }
}