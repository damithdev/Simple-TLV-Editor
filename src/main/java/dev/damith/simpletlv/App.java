package dev.damith.simpletlv;

import com.payneteasy.tlv.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;

public class App {
    private static String APP_NAME = "Simple TLV Util";
    private JPanel mainPanel;
    private JScrollPane tlvEditScrollPane;
    private JLabel messageLabel;
    private JTextArea parseTextArea;
    public App() {

    }

    private void onTLVValueChange(JTextField textField,BerTag berTag , DocumentEvent e){
        try {
            final String value = e.getDocument().getText(0,e.getDocument().getLength());
            if(value.length() % 2 == 1){
                throw new NumberFormatException("Tag:"+berTag.toString() +" Invalid Value");
            }

            System.out.println("Tag:"+berTag.toString() +" Value changed to "+value);

            String text =  parseTextArea.getDocument().getText(0,parseTextArea.getDocument().getLength());
            if(text.isEmpty()){
                messageLabel.setText("");
            }else if(checkHex(text)){
                byte[] bytes = HexUtil.parseHex(text);
                BerTlvParser parser = new BerTlvParser();
                BerTlvs tlvs = parser.parse(bytes, 0, bytes.length);
                if(tlvs.getList().size() == 0){
                    throw new NumberFormatException("Invalid TLV");
                }
                messageLabel.setText("");

                if(tlvs.find(berTag) != null){
                    BerTlvBuilder builder = new BerTlvBuilder();
                    tlvs.getList().forEach(x->{
                        if(x.getTag().equals(berTag)){
                            builder.addHex(x.getTag(),value);
                        }else {
                            builder.addBytes(x.getTag(),x.getBytesValue());

                        }
                    });

                    byte[] newBytes = builder.buildArray();
                    String newHex = bytes2HexStr(newBytes);
                    parseTextArea.setText(newHex);
                    textField.requestFocusInWindow();

                }else{
                    throw new NumberFormatException("Tag Not Found in HEX");
                }
            }else{
                throw new NumberFormatException("Invalid HEX");
            }


              }catch (NumberFormatException ex){
        messageLabel.setText("Error: "+ex.getMessage());
        messageLabel.setForeground(Color.RED);
    } catch (Exception ex){
            messageLabel.setText("Error: "+ex.getMessage());
            messageLabel.setForeground(Color.RED);
            ex.printStackTrace();
        }
    }

    private void onTLVTextChange(DocumentEvent e){
        try{
           String text =  e.getDocument().getText(0,e.getDocument().getLength());
            System.out.println(text);
            if(text.length() % 2 == 1){
                throw new NumberFormatException("Invalid HEX");
            }
            if(text.isEmpty()){
                messageLabel.setText("");

            }else if(checkHex(text)){

                byte[] bytes = HexUtil.parseHex(text);
                BerTlvParser parser = new BerTlvParser();
                BerTlvs tlvs = parser.parse(bytes, 0, bytes.length);
                if(tlvs.getList().size() == 0){
                    throw new NumberFormatException("Invalid TLV");
                }
                messageLabel.setText("");

                JPanel tlvGrid = new JPanel();
                int len = tlvs.getList().size();
                tlvGrid.setLayout(new GridLayout(len,1));

                tlvs.getList().forEach(x->{
                    JPanel item = new JPanel(new FlowLayout());
                    item.add(new JLabel(x.getTag().toString()));
                    JTextField textField = new JTextField(x.getHexValue());
                    textField.setPreferredSize(new Dimension(350,20));
                    textField.getDocument().addDocumentListener(new DocumentListener() {
                        @Override
                        public void insertUpdate(DocumentEvent e) {
                            onTLVValueChange(textField,x.getTag(),e);
                        }

                        @Override
                        public void removeUpdate(DocumentEvent e) {
                            onTLVValueChange(textField,x.getTag(),e);
                        }

                        @Override
                        public void changedUpdate(DocumentEvent e) {
                            onTLVValueChange(textField,x.getTag(),e);
                        }
                    });
                    item.add(textField);
                    tlvGrid.add(item);
                });

                tlvEditScrollPane.setViewportView(tlvGrid);
            }else{
                throw new NumberFormatException("Invalid HEX");
            }
        }catch (NumberFormatException ex){
            messageLabel.setText("Error: "+ex.getMessage());
            messageLabel.setForeground(Color.RED);
        }
        catch (Exception ex){
            messageLabel.setText("Error: "+ex.getMessage());
            messageLabel.setForeground(Color.RED);
            ex.printStackTrace();
        }
    }

    private void createUIComponents() {
        KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());

        messageLabel = new JLabel();
        mainPanel = new JPanel();
        mainPanel.setBorder(new EmptyBorder(10,10,10,10));
        mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
        mainPanel.add(getAppMenu(),BorderLayout.LINE_END);
        JLabel appLabel = new JLabel(APP_NAME);
        appLabel.setFont(new Font(appLabel.getFont().getName(),Font.BOLD,(int)(appLabel.getFont().getSize() * 1.5)));

        mainPanel.add(appLabel);
        mainPanel.add(new JSeparator());
        mainPanel.add(new JLabel("Parse TLV:"));
        mainPanel.add(Box.createVerticalStrut(10));

        parseTextArea.setLineWrap(true);
        parseTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onTLVTextChange(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onTLVTextChange(e);

            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onTLVTextChange(e);

            }
        });


        JScrollPane textAreaScrollPane = new JScrollPane(parseTextArea);
        textAreaScrollPane.getViewport().setPreferredSize(new Dimension(400,50));

        mainPanel.add(textAreaScrollPane);
        mainPanel.add(Box.createVerticalStrut(10));
        tlvEditScrollPane = new JScrollPane(new JPanel());


        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(new JSeparator());


        mainPanel.add(new JLabel("Modify TLV:"));
        mainPanel.add(Box.createVerticalStrut(10));


        tlvEditScrollPane.getViewport().setPreferredSize(new Dimension(400,400));

        mainPanel.add(tlvEditScrollPane);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(messageLabel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(new JSeparator());
        mainPanel.add(new JLabel("Developed by Damith Warnakulasuriya : damith.dev © 2021 | Special Thanks to @payneteasy"));



    }
    UndoManager manager = new UndoManager();

    private Component getAppMenu(){
        JToolBar toolBar = new JToolBar();
        JMenu  menu = new JMenu ("Actions");
        manager = new UndoManager();

        menu.add(new JMenuItem(new AbstractAction("Undo") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    manager.undo();
                } catch (Exception ex) {
                }
            }
        }));

        menu.add(new JMenuItem(new AbstractAction("Redo") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    manager.redo();
                } catch (Exception ex) {
                }
            }
        }));

        menu.add(new JMenuItem(new AbstractAction("Exit") {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);

            }
        }));


        parseTextArea = new JTextArea();
        parseTextArea.getDocument().addUndoableEditListener(new UndoableEditListener() {
            @Override
            public void undoableEditHappened(UndoableEditEvent e) {
                manager.addEdit(e.getEdit());
            }
        });


        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menu);
        return menuBar;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame(APP_NAME);
        frame.setContentPane(new App().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(150, 100);
        frame.setPreferredSize(new Dimension(600,700));

        frame.pack();
        frame.setVisible(true);
    }

    public boolean checkHex(String s)
    {
        // Size of string
        int n = s.length();

        // Iterate over string
        for (int i = 0; i < n; i++) {

            char ch = s.charAt(i);

            // Check if the character
            // is invalid
            if ((ch < '0' || ch > '9')
                    && (ch < 'A' || ch > 'F')) {

                System.out.println("No");
                return false;
            }
        }

        // Print true if all
        // characters are valid
        System.out.println("Yes");
        return true;
    }

    public static String bytes2HexStr(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        String temp;
        for (byte b : bytes) {
            temp = Integer.toHexString(0xFF & b);
            if (temp.length() == 1) {
                sb.append("0");
            }
            sb.append(temp);
        }
        return sb.toString().toUpperCase();
    }
}
