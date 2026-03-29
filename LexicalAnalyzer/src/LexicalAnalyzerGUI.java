import analyzer.AnalysisEngine;
import editor.CodeEditorTab;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.io.*;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import tokenrepository.TokenManager;
import static utils.Theme.*;

public class LexicalAnalyzerGUI extends JFrame {
    
    private CardLayout cardLayout;
    private JPanel rootPanel;
    private JTabbedPane tabbedPane;
    private JTable resultTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<TableModel> rowSorter;
    private JTextField searchField;
    private JLabel statusLabel;
    private JLabel cursorLabel;
    
    private JLabel statTotalTokens;
    private JLabel statUniqueTokens;
    
    private boolean isEditorLightMode = false;

    public LexicalAnalyzerGUI() {
        setTitle("Lexical Analyzer Studio");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 850);
        setLocationRelativeTo(null);
        
        cardLayout = new CardLayout();
        rootPanel = new JPanel(cardLayout);
        rootPanel.setBackground(APP_BG);
        
        rootPanel.add(createWelcomePanel(), "WELCOME");
        rootPanel.add(createMainAppPanel(), "MAIN");
        
        setContentPane(rootPanel);
        setupDragAndDrop();
        setupKeyBindings();
        
        cardLayout.show(rootPanel, "WELCOME");
    }
    
    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(APP_BG);
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(APP_BG);
        
        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth() / 2, cy = getHeight() / 2;
                g2.setStroke(new BasicStroke(12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
                g2.setColor(new Color(86, 182, 194)); 
                g2.drawPolyline(new int[]{cx - 20, cx - 60, cx - 20}, new int[]{cy - 40, cy, cy + 40}, 3);
                g2.setColor(new Color(198, 120, 221)); 
                g2.drawLine(cx + 15, cy - 45, cx - 15, cy + 45);
                g2.setColor(new Color(97, 175, 239)); 
                g2.drawPolyline(new int[]{cx + 20, cx + 60, cx + 20}, new int[]{cy - 40, cy, cy + 40}, 3);
                g2.dispose();
            }
        };
        logoPanel.setPreferredSize(new Dimension(200, 120));
        logoPanel.setMaximumSize(new Dimension(200, 120));
        logoPanel.setOpaque(false);
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel title = new JLabel("Lexical Analyzer");
        title.setFont(new Font("Segoe UI", Font.BOLD, 42));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitle = new JLabel("A modern tokenization engine for source code");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton enterBtn = createStyledButton("Launch Workspace", new VectorIcon(VectorIcon.Type.PLAY, 14, Color.WHITE), PRIMARY_COLOR, PRIMARY_HOVER, Color.WHITE);
        enterBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        enterBtn.setBorder(new EmptyBorder(12, 30, 12, 30));
        enterBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        enterBtn.addActionListener(e -> {
            cardLayout.show(rootPanel, "MAIN");
            if (tabbedPane.getTabCount() == 0) addNewTab("Untitled.java", getSampleCode());
        });
        
        content.add(logoPanel); content.add(Box.createVerticalStrut(10));
        content.add(title); content.add(Box.createVerticalStrut(15));
        content.add(subtitle); content.add(Box.createVerticalStrut(40));
        content.add(enterBtn);
        panel.add(content);
        return panel;
    }
    
    private JPanel createMainAppPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel topHeaderPanel = new JPanel(new BorderLayout());
        
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        toolbar.setBackground(SURFACE_COLOR);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        
        JButton newBtn = createStyledButton("New Tab", new VectorIcon(VectorIcon.Type.PLUS, 14, TEXT_MAIN), SURFACE_COLOR, BORDER_COLOR, TEXT_MAIN);
        JButton openBtn = createStyledButton("Open File...", new VectorIcon(VectorIcon.Type.FOLDER, 14, TEXT_MAIN), SURFACE_COLOR, BORDER_COLOR, TEXT_MAIN);
        JButton clearBtn = createStyledButton("Clear", new VectorIcon(VectorIcon.Type.TRASH, 14, TEXT_MAIN), SURFACE_COLOR, BORDER_COLOR, TEXT_MAIN);
        JButton themeBtn = createStyledButton("Theme", new VectorIcon(VectorIcon.Type.SUN, 14, TEXT_MAIN), SURFACE_COLOR, BORDER_COLOR, TEXT_MAIN);
        JButton runBtn = createStyledButton("Tokenize Active Tab", new VectorIcon(VectorIcon.Type.PLAY, 12, Color.WHITE), PRIMARY_COLOR, PRIMARY_HOVER, Color.WHITE);
        
        newBtn.addActionListener(e -> addNewTab("Untitled" + (tabbedPane.getTabCount() + 1) + ".java", ""));
        openBtn.addActionListener(e -> openFile());
        clearBtn.addActionListener(e -> {
            if (tabbedPane.getSelectedComponent() != null) {
                CodeEditorTab activeTab = (CodeEditorTab) tabbedPane.getSelectedComponent();
                activeTab.textPane.setText("");
                AnalysisEngine.runAnalysis(activeTab, tableModel, isEditorLightMode);
                syncDashboard();
            }
        });
        runBtn.addActionListener(e -> {
            if (tabbedPane.getSelectedComponent() != null) {
                AnalysisEngine.runAnalysis((CodeEditorTab) tabbedPane.getSelectedComponent(), tableModel, isEditorLightMode);
                AnalysisEngine.refreshTable((CodeEditorTab) tabbedPane.getSelectedComponent(), tableModel);
                syncDashboard();
                updateStatus("Analysis complete.", false);
            }
        });
        
        themeBtn.addActionListener(e -> {
            isEditorLightMode = !isEditorLightMode;
            for(int i = 0; i < tabbedPane.getTabCount(); i++) {
                CodeEditorTab tab = (CodeEditorTab) tabbedPane.getComponentAt(i);
                tab.applyTheme(isEditorLightMode);
                AnalysisEngine.runAnalysis(tab, tableModel, isEditorLightMode);
            }
            if (tabbedPane.getSelectedComponent() != null) {
                AnalysisEngine.refreshTable((CodeEditorTab) tabbedPane.getSelectedComponent(), tableModel);
            }
            syncDashboard();
        });
        
        toolbar.add(newBtn); toolbar.add(openBtn); toolbar.add(clearBtn); toolbar.add(themeBtn);
        toolbar.add(Box.createHorizontalStrut(20)); toolbar.add(runBtn);
        
        JPanel dashboard = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 8));
        dashboard.setBackground(APP_BG);
        dashboard.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        
        statTotalTokens = new JLabel("Total Tokens: 0"); statTotalTokens.setForeground(TEXT_MAIN); statTotalTokens.setFont(UI_FONT_BOLD);
        statUniqueTokens = new JLabel("Unique Lexemes: 0"); statUniqueTokens.setForeground(TEXT_MAIN); statUniqueTokens.setFont(UI_FONT_BOLD);
        dashboard.add(statTotalTokens); dashboard.add(new JLabel(" | ")); dashboard.add(statUniqueTokens);
        
        topHeaderPanel.add(toolbar, BorderLayout.NORTH);
        topHeaderPanel.add(dashboard, BorderLayout.SOUTH);
        mainPanel.add(topHeaderPanel, BorderLayout.NORTH);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.55);
        splitPane.setBorder(null);
        splitPane.setDividerSize(5);
        splitPane.setBackground(APP_BG);
        
        UIManager.put("TabbedPane.background", APP_BG);
        UIManager.put("TabbedPane.foreground", TEXT_MAIN);
        UIManager.put("TabbedPane.selected", SURFACE_COLOR);
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UI_FONT);
        tabbedPane.setBackground(APP_BG);
        tabbedPane.setBorder(new EmptyBorder(10, 10, 10, 5));
        
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedComponent() != null) {
                CodeEditorTab activeTab = (CodeEditorTab) tabbedPane.getSelectedComponent();
                AnalysisEngine.refreshTable(activeTab, tableModel);
                syncDashboard();
            }
        });
        
        splitPane.setLeftComponent(tabbedPane);
        splitPane.setRightComponent(createResultsPanel());
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(createFooterPanel(), BorderLayout.SOUTH);
        
        return mainPanel;
    }
    
    private void syncDashboard() {
        if (tabbedPane.getSelectedComponent() != null) {
            CodeEditorTab tab = (CodeEditorTab) tabbedPane.getSelectedComponent();
            statTotalTokens.setText("Total Tokens: " + tab.totalTokens);
            statUniqueTokens.setText("Unique Lexemes: " + tab.tabCounts.size());
        } else {
            statTotalTokens.setText("Total Tokens: 0"); statUniqueTokens.setText("Unique Lexemes: 0");
        }
    }
    
    private String showCustomRenameDialog(String currentName) {
        JDialog dialog = new JDialog(this, "Rename Tab", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(SURFACE_COLOR);
        dialog.setResizable(false);
        
        JPanel content = new JPanel(new BorderLayout(10, 15));
        content.setBorder(new EmptyBorder(20, 25, 20, 25));
        content.setOpaque(false);
        
        JLabel label = new JLabel("Enter new tab name:");
        label.setForeground(TEXT_MAIN);
        label.setFont(UI_FONT);
        
        JTextField inputField = new JTextField(currentName, 20);
        inputField.setBackground(APP_BG);
        inputField.setForeground(Color.WHITE);
        inputField.setCaretColor(Color.WHITE);
        inputField.setFont(UI_FONT);
        inputField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true), new EmptyBorder(8, 10, 8, 10)
        ));
        inputField.selectAll(); 
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        
        String[] newName = new String[]{null};
        
        JButton okBtn = createStyledButton("OK", null, PRIMARY_COLOR, PRIMARY_HOVER, Color.WHITE);
        JButton cancelBtn = createStyledButton("Cancel", null, APP_BG, BORDER_COLOR, TEXT_MAIN);
        
        okBtn.addActionListener(e -> { newName[0] = inputField.getText(); dialog.dispose(); });
        cancelBtn.addActionListener(e -> dialog.dispose());
        inputField.addActionListener(e -> okBtn.doClick());
        
        content.add(label, BorderLayout.NORTH);
        content.add(inputField, BorderLayout.CENTER);
        buttonPanel.add(cancelBtn); buttonPanel.add(okBtn);
        content.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(content); dialog.pack();
        dialog.setLocationRelativeTo(this); dialog.setVisible(true);
        return newName[0];
    }
    
    private void addNewTab(String title, String content) {
        CodeEditorTab tab = new CodeEditorTab(content, cursorText -> cursorLabel.setText(cursorText));
        tab.applyTheme(isEditorLightMode); 
        
        tabbedPane.addTab(title, tab);
        tabbedPane.setSelectedComponent(tab);
        
        JPanel pnlTab = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlTab.setOpaque(false);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(TEXT_MAIN);
        lblTitle.setToolTipText("Double-click to rename");
        
        lblTitle.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String newName = showCustomRenameDialog(lblTitle.getText());
                    if (newName != null && !newName.trim().isEmpty()) lblTitle.setText(newName.trim());
                }
            }
        });
        
        JButton btnClose = new JButton("x");
        btnClose.setOpaque(false); btnClose.setContentAreaFilled(false); btnClose.setBorderPainted(false);
        btnClose.setForeground(TEXT_MUTED); btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.setPreferredSize(new Dimension(15, 15)); btnClose.setMargin(new Insets(0,0,0,0));
        btnClose.addActionListener(e -> {
            tabbedPane.remove(tab);
            if(tabbedPane.getTabCount() == 0) {
                tableModel.setRowCount(0);
                syncDashboard();
            }
        });
        
        pnlTab.add(lblTitle); pnlTab.add(btnClose);
        tabbedPane.setTabComponentAt(tabbedPane.indexOfComponent(tab), pnlTab);
    }
    
    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(APP_BG);
        panel.setBorder(new EmptyBorder(10, 5, 10, 10));
        
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(APP_BG);
        JLabel searchIcon = new JLabel(" Filter:");
        searchIcon.setIcon(new VectorIcon(VectorIcon.Type.SEARCH, 14, TEXT_MUTED));
        searchIcon.setForeground(TEXT_MUTED);
        searchIcon.setFont(UI_FONT_BOLD);
        
        searchField = new JTextField();
        searchField.setBackground(SURFACE_COLOR);
        searchField.setForeground(Color.WHITE);
        searchField.setCaretColor(Color.WHITE);
        searchField.setFont(UI_FONT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true), new EmptyBorder(5, 10, 5, 10)
        ));
        
        searchPanel.add(searchIcon, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        panel.add(searchPanel, BorderLayout.NORTH);
        
        String[] columns = {"Lexeme", "Token Type", "Count", "Lines"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        
        resultTable = new JTable(tableModel);
        resultTable.setFont(CODE_FONT);
        resultTable.setRowHeight(32);
        resultTable.setBackground(SURFACE_COLOR);
        resultTable.setForeground(TEXT_MAIN);
        resultTable.setGridColor(BORDER_COLOR);
        resultTable.setSelectionBackground(new Color(50, 56, 68));
        resultTable.setSelectionForeground(Color.WHITE);
        
        rowSorter = new TableRowSorter<>(tableModel);
        resultTable.setRowSorter(rowSorter);
        
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            public void changedUpdate(DocumentEvent e) { filterTable(); }
            private void filterTable() {
                String text = searchField.getText();
                if (text.trim().length() == 0) rowSorter.setRowFilter(null);
                else rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
            }
        });
        
        resultTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (resultTable.getSelectedRow() != -1 && tabbedPane.getSelectedComponent() != null) {
                    CodeEditorTab activeTab = (CodeEditorTab) tabbedPane.getSelectedComponent();
                    int modelRow = resultTable.convertRowIndexToModel(resultTable.getSelectedRow());
                    String lexeme = (String) tableModel.getValueAt(modelRow, 0);
                    
                    activeTab.highlightSelectedToken(lexeme, isEditorLightMode);
                    
                    if (e.getClickCount() == 2) {
                        List<Integer> offsets = activeTab.tabOffsets.get(lexeme);
                        if (offsets != null && !offsets.isEmpty()) {
                            activeTab.textPane.setCaretPosition(offsets.get(0));
                            activeTab.textPane.requestFocusInWindow();
                        }
                    }
                }
            }
        });
        
        resultTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && resultTable.getSelectedRow() != -1 && tabbedPane.getSelectedComponent() != null) {
                CodeEditorTab activeTab = (CodeEditorTab) tabbedPane.getSelectedComponent();
                int modelRow = resultTable.convertRowIndexToModel(resultTable.getSelectedRow());
                activeTab.highlightSelectedToken((String) tableModel.getValueAt(modelRow, 0), isEditorLightMode);
            }
        });
        
        resultTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                
                int modelRow = table.convertRowIndexToModel(row);
                String tokenType = (String) table.getModel().getValueAt(modelRow, 1);
                Color tokenColor = TokenManager.getTokenColor(tokenType, false); 
                
                if (!isSelected) {
                    c.setBackground(APP_BG); 
                    if (column == 0 || column == 1) {
                        c.setForeground(tokenColor);
                        c.setFont(new Font("Consolas", Font.BOLD, column == 0 ? 14 : 13));
                    } else if (column == 3) {
                        c.setForeground(new Color(110, 118, 129));
                        c.setFont(new Font("Consolas", Font.ITALIC, 13));
                    } else {
                        c.setForeground(TEXT_MUTED); 
                        c.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    }
                } else {
                     if (column == 0 || column == 1) c.setForeground(tokenColor);
                     else c.setForeground(Color.WHITE);
                }
                return c;
            }
        });
        
        resultTable.getTableHeader().setBackground(SURFACE_COLOR);
        resultTable.getTableHeader().setForeground(TEXT_MAIN);
        resultTable.getTableHeader().setFont(UI_FONT_BOLD);
        
        resultTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        resultTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        resultTable.getColumnModel().getColumn(2).setPreferredWidth(50);
        resultTable.getColumnModel().getColumn(3).setPreferredWidth(200);
        
        JScrollPane scroll = new JScrollPane(resultTable);
        scroll.setBorder(new LineBorder(BORDER_COLOR));
        scroll.getViewport().setBackground(SURFACE_COLOR);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }
    
    private void openFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File file = chooser.getSelectedFile();
                loadFileIntoTab(file);
            } catch (Exception e) { updateStatus("Failed to open file", true); }
        }
    }
    
    // The method that was accidentally deleted!
    private void loadFileIntoTab(java.io.File file) {
        try {
            String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
            addNewTab(file.getName(), content);
            updateStatus("Loaded: " + file.getName(), false);
        } catch (IOException e) { updateStatus("Failed to open file", true); }
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(SURFACE_COLOR);
        footer.setBorder(new EmptyBorder(5, 15, 5, 15));
        statusLabel = new JLabel("Ready"); statusLabel.setForeground(TEXT_MUTED); statusLabel.setFont(UI_FONT);
        cursorLabel = new JLabel("Line: 1, Col: 1"); cursorLabel.setForeground(TEXT_MUTED); cursorLabel.setFont(UI_FONT);
        footer.add(statusLabel, BorderLayout.WEST); footer.add(cursorLabel, BorderLayout.EAST);
        return footer;
    }

    private void updateStatus(String msg, boolean isError) {
        statusLabel.setForeground(isError ? DANGER_COLOR : TEXT_MUTED);
        statusLabel.setText(msg);
        new javax.swing.Timer(4000, e -> statusLabel.setText("Ready")).start();
    }
    
    private static class VectorIcon implements Icon {
        public enum Type { PLAY, PLUS, FOLDER, TRASH, SEARCH, SUN }
        private Type type; private int size; private Color color;
        public VectorIcon(Type type, int size, Color color) { this.type = type; this.size = size; this.color = color; }
        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color); g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int pad = 1, w = size - pad * 2, h = size - pad * 2, cx = x + pad, cy = y + pad;
            switch (type) {
                case PLAY: g2.fillPolygon(new int[]{cx+2, cx+w, cx+2}, new int[]{cy, cy+h/2, cy+h}, 3); break;
                case PLUS: g2.drawLine(cx+w/2, cy, cx+w/2, cy+h); g2.drawLine(cx, cy+h/2, cx+w, cy+h/2); break;
                case FOLDER:
                    Path2D folder = new Path2D.Float(); folder.moveTo(cx, cy+h); folder.lineTo(cx, cy+2);
                    folder.lineTo(cx+w*0.4, cy+2); folder.lineTo(cx+w*0.55, cy+4); folder.lineTo(cx+w, cy+4);
                    folder.lineTo(cx+w, cy+h); folder.closePath(); g2.draw(folder); break;
                case TRASH:
                    g2.drawLine(cx, cy+2, cx+w, cy+2); g2.drawRect(cx+2, cy+2, w-4, h-2);
                    g2.drawLine(cx+w/2-2, cy, cx+w/2+2, cy); g2.drawLine(cx+4, cy+5, cx+4, cy+h-2);
                    g2.drawLine(cx+w-4, cy+5, cx+w-4, cy+h-2); break;
                case SEARCH: 
                    int r = (int)(w * 0.6); g2.drawOval(cx, cy, r, r);
                    g2.drawLine(cx + (int)(r * 0.85), cy + (int)(r * 0.85), cx + w, cy + h); break;
                case SUN: 
                    int rad = (int)(w * 0.45); g2.drawOval(cx + w/2 - rad/2, cy + h/2 - rad/2, rad, rad);
                    g2.drawLine(cx + w/2, cy, cx + w/2, cy + 2); g2.drawLine(cx + w/2, cy + h - 2, cx + w/2, cy + h); 
                    g2.drawLine(cx, cy + h/2, cx + 2, cy + h/2); g2.drawLine(cx + w - 2, cy + h/2, cx + w, cy + h/2); 
                    break;
            }
            g2.dispose();
        }
    }
    
    private void setupKeyBindings() {
        InputMap in = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getRootPane().getActionMap();
        int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        
        in.put(KeyStroke.getKeyStroke(KeyEvent.VK_T, mask), "newTab");
        am.put("newTab", new AbstractAction() { public void actionPerformed(ActionEvent e) { addNewTab("Untitled.java", ""); }});
        in.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, mask), "openFile");
        am.put("openFile", new AbstractAction() { public void actionPerformed(ActionEvent e) { openFile(); }});
        in.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, mask), "closeTab");
        am.put("closeTab", new AbstractAction() { 
            public void actionPerformed(ActionEvent e) { 
                Component selected = tabbedPane.getSelectedComponent();
                if (selected != null) {
                    tabbedPane.remove(selected);
                    if (tabbedPane.getTabCount() == 0) {
                        tableModel.setRowCount(0); syncDashboard();
                    }
                }
            }
        });
        in.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, mask), "tokenize");
        am.put("tokenize", new AbstractAction() { public void actionPerformed(ActionEvent e) { 
            if (tabbedPane.getSelectedComponent() != null) {
                AnalysisEngine.runAnalysis((CodeEditorTab) tabbedPane.getSelectedComponent(), tableModel, isEditorLightMode);
                AnalysisEngine.refreshTable((CodeEditorTab) tabbedPane.getSelectedComponent(), tableModel);
                syncDashboard();
                updateStatus("Analysis complete.", false);
            }
        }});
        
        for (int i = 1; i <= 9; i++) {
            int targetIndex = i - 1;
            in.put(KeyStroke.getKeyStroke(KeyEvent.VK_0 + i, mask), "switchTab" + i);
            am.put("switchTab" + i, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    if (targetIndex < tabbedPane.getTabCount()) tabbedPane.setSelectedIndex(targetIndex);
                }
            });
        }
    }
    
    private void setupDragAndDrop() {
        new java.awt.dnd.DropTarget(this, new java.awt.dnd.DropTargetAdapter() {
            @Override public void drop(java.awt.dnd.DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(java.awt.dnd.DnDConstants.ACTION_COPY);
                    Object data = dtde.getTransferable().getTransferData(java.awt.datatransfer.DataFlavor.javaFileListFlavor);
                    if (data instanceof java.util.List) {
                        for (Object obj : (java.util.List<?>) data) {
                            if (obj instanceof java.io.File) loadFileIntoTab((java.io.File) obj);
                        }
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });
    }
    
    private JButton createStyledButton(String text, Icon icon, Color bg, Color hover, Color fg) {
        JButton btn = new JButton(text);
        if (icon != null) { btn.setIcon(icon); btn.setIconTextGap(8); }
        btn.setBackground(bg); btn.setForeground(fg); btn.setFont(UI_FONT_BOLD); 
        btn.setFocusPainted(false); btn.setBorderPainted(false); btn.setOpaque(true);
        btn.setBorder(new EmptyBorder(6, 16, 6, 16)); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }

    private String getSampleCode() {
        return "// Welcome to your Lexical Analyzer Studio\n" +
               "public class SampleProgram {\n" +
               "    public static void main(String[] args) {\n" +
               "        int number = 42; \n" +
               "        number += 10;\n" +
               "    }\n" +
               "}";
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } 
        catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new LexicalAnalyzerGUI().setVisible(true));
    }
}