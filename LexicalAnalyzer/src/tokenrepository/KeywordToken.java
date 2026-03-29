package tokenrepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class KeywordToken {
    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
        "abstract","assert","boolean","break","byte","case","catch","char","class","continue",
        "default","do","double","else","enum","extends","final","finally","float","for","if",
        "implements","import","instanceof","int","interface","long","native","new","package",
        "private","protected","public","return","short","static","strictfp","super","switch",
        "synchronized","this","throw","throws","transient","try","void","volatile","while","true","false","null"));

    public static boolean isKeyword(String lex) {
        return KEYWORDS.contains(lex);
    }
}