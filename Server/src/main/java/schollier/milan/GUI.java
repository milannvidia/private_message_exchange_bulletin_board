package schollier.milan;

import javax.swing.*;
import java.awt.*;

public class GUI {
    ConnectImpl connect;
    JFrame f=new JFrame();
    JList<String> list=new JList<>();
    JScrollPane scrollPane;

    public GUI(ConnectImpl app) {
        this.connect=app;
        loadList();
        scrollPane=new JScrollPane(list);

        f.getContentPane().add(scrollPane, BorderLayout.CENTER);
        f.setSize(540, 960);
        f.setVisible(true);
    }

    public void loadList() {
        DefaultListModel<String> dlm=new DefaultListModel<>();
        for (int i = 0; i < connect.bulletinBoard.size(); i++) {
            String row=i+ ": ";
            for (String s: connect.bulletinBoard.get(i).values()) {
                row=row.concat(s.substring(0,6)+ "; ");
            }
            dlm.addElement(row);
        }
        list.setModel(dlm);

    }
}
