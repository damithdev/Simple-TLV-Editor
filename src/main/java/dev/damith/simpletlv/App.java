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
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class App{
    private static final String APP_NAME = "Simple TLV Editor";
    JFrame frame;
    JPanel mainPanel;
    JMenuBar menuBar;
    JButton uMenu,rMenu;
    JScrollPane tlvEditScrollPane;
    JLabel messageLabel;
    JTextArea parseTextArea;
    UndoManager manager = new UndoManager();



    public App() {
        frame = new JFrame(APP_NAME);

        createAppMenu();
        createUIComponents();

        frame.add(menuBar);
        frame.setJMenuBar(menuBar);
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.setLocation(150, 100);
        frame.setPreferredSize(new Dimension(850,700));

        frame.pack();
        frame.setVisible(true);
        EmvData.init();
    }

    private void createAppMenu(){

        menuBar = new JMenuBar();
        uMenu = new JButton("Undo");
        uMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    manager.undo();
                } catch (Exception ex) {
                }
            }
        });
        rMenu = new JButton("Redo");

        rMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    manager.redo();
                } catch (Exception ex) {
                }
            }
        });

        menuBar.add(uMenu);
        menuBar.add(rMenu);



    }

    private void createUIComponents() {

        KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JPanel panelHeader = new JPanel();
        JLabel appLabel = new JLabel(APP_NAME);
        appLabel.setFont(new Font(appLabel.getFont().getName(),Font.BOLD,(int)(appLabel.getFont().getSize() * 1.5)));
        panelHeader.add(appLabel);
        panelHeader.setSize(new Dimension(mainPanel.getWidth() , 100));
        mainPanel.add(panelHeader);
        mainPanel.add(new JSeparator());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("String: Paste/Edit the TLV String here"));

        parseTextArea = new JTextArea();
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
        parseTextArea.getDocument().addUndoableEditListener(new UndoableEditListener() {
            @Override
            public void undoableEditHappened(UndoableEditEvent e) {
                manager.addEdit(e.getEdit());
            }
        });

        JScrollPane textAreaScrollPane = new JScrollPane(parseTextArea);
        textAreaScrollPane.getViewport().setPreferredSize(new Dimension(400,50));
        panel.add(textAreaScrollPane);
        panel.add(Box.createVerticalStrut(10));
        mainPanel.add(panel);


        JPanel panelEdit = new JPanel();
        panelEdit.setLayout(new BoxLayout(panelEdit, BoxLayout.Y_AXIS));
        panelEdit.setBorder(BorderFactory.createTitledBorder("Tags: Edit the TLV Tags here"));

        tlvEditScrollPane = new JScrollPane(new JPanel());
        tlvEditScrollPane.getViewport().setPreferredSize(new Dimension(400,400));
        panelEdit.add(tlvEditScrollPane);
        mainPanel.add(panelEdit);

        messageLabel = new JLabel("");

        JPanel panelAlerts = new JPanel();
        panelAlerts.setLayout(new FlowLayout(FlowLayout.LEFT));
        panelAlerts.add(messageLabel);
        mainPanel.add(panelAlerts);

        mainPanel.add(new JSeparator());
        JPanel panelFooter = new JPanel();
        JLabel appFooterLabel = new JLabel("Developed by damith.dev © 2021 | ");
        JLabel hyperlink = new JLabel("Visit Repo");
        hyperlink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        hyperlink.setForeground(Color.BLUE.darker());
        hyperlink.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("https://github.com/damithdev/Simple-TLV-Editor"));

                } catch (IOException | URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // the mouse has entered the label
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // the mouse has exited the label
            }
        });
        panelFooter.add(appFooterLabel);
        panelFooter.add(hyperlink);
        mainPanel.add(panelFooter);

    }

    public static void main(String[] args) {
        new App();
    }


////////////////////////////////////////////////////////////////////////////////

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
                    textField.getDocument().addUndoableEditListener(new UndoableEditListener() {
                        @Override
                        public void undoableEditHappened(UndoableEditEvent e) {
                            manager.addEdit(e.getEdit());
                        }
                    });
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
                    String pretag = x.getTag().toString();
                    String tag = pretag.substring(2,pretag.length());
                    System.out.println(tag);
                    item.add(textField);

                    EmvData data = StaticEntry.emvTags.get(tag);

                    JLabel tagName = new JLabel();
                    tagName.setPreferredSize(new Dimension(350,20));
                    item.add(tagName);
                    if(data != null){
                        tagName.setText(data.getName());
                    }

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

    public String bytes2HexStr(byte[] bytes) {
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
