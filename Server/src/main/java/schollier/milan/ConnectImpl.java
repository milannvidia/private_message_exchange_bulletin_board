package schollier.milan;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

public class ConnectImpl extends UnicastRemoteObject implements ConnectIF {
    ArrayList<HashMap<String,String>> bulletinBoard=new ArrayList<>(20);
    GUI gui;
    public  ConnectImpl() throws RemoteException{
        this.gui=new GUI(this);
        for (int i = 0; i < 20; i++) {
            bulletinBoard.add(new HashMap<>());
        }
    }


    @Override
    public void write(int idx, String message, String tag) {
        bulletinBoard.get(idx).put(tag,message);
        gui.loadList();
    }

    @Override
    public String get(int idx, int tag) throws RemoteException {
        HashMap<String,String> current=bulletinBoard.get(idx);
        //make tag
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte [] hash= md.digest(new byte[]{Integer.valueOf(tag).byteValue()});
            String hashBase64=getStringBase64(hash);

            String message=current.get(hashBase64);
            if(message==null)return null;
            else {
                current.remove(hashBase64);
                gui.loadList();
                return message;
            }
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
    private String getStringBase64(byte[] bytearray){
        return Base64.getEncoder().encodeToString(bytearray);
    }

}
