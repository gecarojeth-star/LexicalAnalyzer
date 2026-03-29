package editor;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import tokenrepository.TokenManager;
import static utils.Theme.*;

public class CodeEditorTab extends JPanel {
    public JTextPane textPane;
    public JTextArea lineNumbers;
    
    public Map<String, Integer> tabCounts = new LinkedHashMap<>();
    public Map<String, String> tabTypes = new HashMap<>();
    public Map<String, List<Integer>> tabOffsets = new HashMap<>();
    public Map<String, Set<Integer>> tabLines = new HashMap<>(); 
    
    public int totalTokens = 0;
    
    public CodeEditorTab(String initialText, Consumer<String> cursorUpdater) {
        setLayout(new BorderLayout());
        setBackground(SURFACE_COLOR);
        
        textPane = new JTextPane() {
            @Override public boolean getScrollableTracksViewportWidth() { return true; }
            @Override public String getToolTipText(MouseEvent event) {
                int pos = viewToModel(event.getPoint());
                if (pos >= 0) {
                    try {
                        int start = Utilities.getWordStart(this, pos);
                        int end = Utilities.getWordEnd(this, pos);
                        String word = getText(start, end - start);
                        if (!word.trim().isEmpty()) return "Token: " + TokenManager.getTokenType(word);
                    } catch (BadLocationException e) {}
                }
                return null;
            }
        };
        
        ToolTipManager.sharedInstance().registerComponent(textPane);
        textPane.setFont(CODE_FONT);
        textPane.setMargin(new Insets(10, 10, 10, 10));
        textPane.setText(initialText);
        
        textPane.addCaretListener(e -> {
            try {
                int caretPos = textPane.getCaretPosition();
                int row = (caretPos == 0) ? 1 : 0;
                for (int offset = caretPos; offset > 0;) {
                    offset = Utilities.getRowStart(textPane, offset) - 1;
                    row++;
                }
                int col = caretPos - Utilities.getRowStart(textPane, caretPos) + 1;
                cursorUpdater.accept(String.format("Line: %d, Col: %d", row, col));
            } catch (Exception ex) { }
        });
        
        lineNumbers = new JTextArea("1");
        lineNumbers.setFont(CODE_FONT);
        lineNumbers.setEditable(false);
        lineNumbers.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        textPane.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateLines(); }
            public void removeUpdate(DocumentEvent e) { updateLines(); }
            public void changedUpdate(DocumentEvent e) { updateLines(); }
            private void updateLines() {
                int lines = textPane.getDocument().getDefaultRootElement().getElementCount();
                StringBuilder sb = new StringBuilder();
                for(int i = 1; i <= lines; i++) sb.append(i).append(System.lineSeparator());
                lineNumbers.setText(sb.toString());
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setRowHeaderView(lineNumbers);
        scrollPane.setBorder(new LineBorder(BORDER_COLOR));
        scrollPane.getVerticalScrollBar().setBackground(APP_BG);
        add(scrollPane, BorderLayout.CENTER);
        
        textPane.setText(textPane.getText());
    }
    
    public void highlightSelectedToken(String lexeme, boolean isLightMode) {
        textPane.getHighlighter().removeAllHighlights();
        List<Integer> offsets = tabOffsets.get(lexeme);
        
        if (offsets != null) {
            try {
                Color hColor = isLightMode ? new Color(0, 0, 0, 30) : new Color(255, 255, 255, 40);
                DefaultHighlighter.DefaultHighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(hColor);
                
                for (int offset : offsets) {
                    textPane.getHighlighter().addHighlight(offset, offset + lexeme.length(), painter);
                }
            } catch (Exception ex) {}
        }
    }
    
    public void applyTheme(boolean lightMode) {
        if (lightMode) {
            textPane.setBackground(Color.WHITE);
            textPane.setForeground(new Color(36, 41, 46));
            textPane.setCaretColor(Color.BLACK);
            lineNumbers.setBackground(new Color(246, 248, 250));
            lineNumbers.setForeground(new Color(110, 118, 129));
        } else {
            textPane.setBackground(SURFACE_COLOR);
            textPane.setForeground(TEXT_MAIN);
            textPane.setCaretColor(Color.WHITE);
            lineNumbers.setBackground(APP_BG);
            lineNumbers.setForeground(TEXT_MUTED);
        }
        resetHighlights(lightMode);
    }
    
    public void resetHighlights(boolean lightMode) {
        textPane.getHighlighter().removeAllHighlights();
        SimpleAttributeSet base = new SimpleAttributeSet();
        StyleConstants.setForeground(base, lightMode ? new Color(36, 41, 46) : TEXT_MAIN);
        StyleConstants.setBold(base, false);
        StyleConstants.setUnderline(base, false);
        textPane.getStyledDocument().setCharacterAttributes(0, textPane.getDocument().getLength(), base, true);
    }
}