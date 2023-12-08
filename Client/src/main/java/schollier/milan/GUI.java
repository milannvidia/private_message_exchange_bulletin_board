package schollier.milan;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class GUI {
    Client app;
    JFrame f=new JFrame();
    JTabbedPane tp= new JTabbedPane();
    HashMap<String, JTextArea> contactScreens= new HashMap<>();
    GUI(Client app) {
        this.app=app;

        loadContacts();


        tp.setMinimumSize(new Dimension(100,100));
        f.getContentPane().add(tp,BorderLayout.CENTER);
        f.setSize(540, 960);
        f.setVisible(true);
    }

    public void loadContacts(){
        for(Contact c:app.contacts){
            //create sub element
            JTextArea history=new JTextArea();
            for (String text:c.history) {
                history.append(text+"\n");
            }
            JTextField message=new JTextField();
            message.addActionListener(e -> {
                try {
                    c.send(e.getActionCommand());
                    message.setText("");
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });
            message.setPreferredSize(new Dimension(Integer.MAX_VALUE,50));
            message.setMaximumSize(new Dimension(Integer.MAX_VALUE,500));

            JPanel root=new JPanel();
            root.setLayout(new BoxLayout(root,BoxLayout.Y_AXIS));
            root.add(history);
            root.add(message);
            contactScreens.put(c.name,history);
            tp.add(c.name,root);
        }
    }

    public void update() {
        loadContacts();
    }
    public void addMessage(Contact contact) {
        contactScreens.get(contact.name).append(contact.history.getLast()+"\n");
    }

}
