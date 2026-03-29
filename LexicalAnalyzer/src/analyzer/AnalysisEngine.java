package analyzer;

import java.util.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.*;
import lexer.LexerEngine;
import lexer.TokenNode;
import tokenrepository.TokenManager;
import editor.CodeEditorTab;

public class AnalysisEngine {
    
    public static void runAnalysis(CodeEditorTab activeTab, DefaultTableModel tableModel, boolean isLightMode) {
        StyledDocument doc = activeTab.textPane.getStyledDocument();
        String code;
        try {
            code = doc.getText(0, doc.getLength());
        } catch (BadLocationException e) { code = activeTab.textPane.getText(); }
        
        if (code.trim().isEmpty()) return;
        
        activeTab.resetHighlights(isLightMode);
        activeTab.tabCounts.clear();
        activeTab.tabTypes.clear();
        activeTab.tabOffsets.clear();
        activeTab.tabLines.clear();
        activeTab.totalTokens = 0;
        
        List<TokenNode> tokens = LexerEngine.tokenizeText(code);
        
        for (TokenNode token : tokens) {
            String type = TokenManager.getTokenType(token.lexeme);
            
            SimpleAttributeSet style = new SimpleAttributeSet();
            StyleConstants.setForeground(style, TokenManager.getTokenColor(type, isLightMode));
            if (type.equals(TokenManager.KEYWORD)) StyleConstants.setBold(style, true);
            
            doc.setCharacterAttributes(token.offset, token.lexeme.length(), style, true);
            
            if (!type.equals(TokenManager.COMMENT) && !token.lexeme.trim().isEmpty()) {
                activeTab.totalTokens++;
                activeTab.tabCounts.put(token.lexeme, activeTab.tabCounts.getOrDefault(token.lexeme, 0) + 1);
                activeTab.tabTypes.put(token.lexeme, type);
                activeTab.tabOffsets.putIfAbsent(token.lexeme, new ArrayList<>());
                activeTab.tabOffsets.get(token.lexeme).add(token.offset);
                activeTab.tabLines.putIfAbsent(token.lexeme, new HashSet<>());
                activeTab.tabLines.get(token.lexeme).add(token.lineNumber);
            }
        }
    }
    
    public static void refreshTable(CodeEditorTab tab, DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
        if (tab == null) return;
        
        for (Map.Entry<String, Integer> entry : tab.tabCounts.entrySet()) {
            String lexeme = entry.getKey();
            List<Integer> sortedLines = new ArrayList<>(tab.tabLines.get(lexeme));
            Collections.sort(sortedLines);
            String linesStr = sortedLines.toString();
            if (linesStr.length() > 30) linesStr = linesStr.substring(0, 27) + "...]";
            
            tableModel.addRow(new Object[]{lexeme, tab.tabTypes.get(lexeme), entry.getValue(), linesStr});
        }
    }
}