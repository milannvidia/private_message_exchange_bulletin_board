package schollier.milan;

import org.json.JSONObject;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

public class Contact {
    //
    public String name;
    public ArrayList<String>history=new ArrayList<>();
    private final Client app;

    private SecretKey sendkey;
    private int sendIdx;
    private int sendTag;
    private byte[] sendSalt;

    private SecretKey receiveKey;
    private int receiveIdx;
    private int receiveTag;
    private byte[] receiveSalt;




    public Contact(String name, Client a){
        this.name=name;
        history.add("Chat start with "+name);
        this.app=a;
    }

    public void send(String message) throws Exception {
        //next values
        int nextIdx=new Random().nextInt(20);
        int nextTag=new Random().nextInt();
        //check if not the same
        while(nextIdx==sendIdx){
            nextIdx=new Random().nextInt(20);
        }
        while(nextTag==sendTag){
            nextTag=new Random().nextInt();
        }
        //encrypt message
        byte [] encrypted=encrypt(message,nextIdx,nextTag);

        //make tag
        MessageDigest md1 = MessageDigest.getInstance("SHA-256");
        byte [] hash= md1.digest(new byte[]{Integer.valueOf(sendTag).byteValue()});
        String hashBase64=getStringBase64(hash);

        if(!app.write(sendIdx,getStringBase64(encrypted),hashBase64)){
            return;
        }
        history.add("You: "+message);
        app.gui.addMessage(this);

        //set new values
        sendIdx=nextIdx;
        sendTag=nextTag;
        //gen new key
        PBEKeySpec keySpec=new PBEKeySpec(sendkey.toString().toCharArray(),sendSalt,524288,256);
        sendkey= app.kdfGen(keySpec);

    }
    public void receive() throws Exception {
        String message=app.get(receiveIdx,receiveTag);

        if(message==null) return;
        JSONObject payload=decrypt(message);


        String messageReceived=payload.getString("message");
        history.add(this.name+": "+messageReceived);
        app.gui.addMessage(this);

        receiveIdx=payload.getInt("nextIdx");
        receiveTag=payload.getInt("nextTag");
        //gen new key
        PBEKeySpec keySpec=new PBEKeySpec(receiveKey.toString().toCharArray(),receiveSalt,524288,256);
        receiveKey= app.kdfGen(keySpec);
    }

    private JSONObject decrypt(String message) throws Exception{
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE,receiveKey);
        byte[] cipherText=getBytearray(message);
        String jsonString=new String(cipher.doFinal(cipherText));
        JSONObject decrypted=new JSONObject(jsonString);

        JSONObject payload=new JSONObject(decrypted.getString("payload"));
        String macReceived=decrypted.getString("MAC");
        byte[] macReceivedbytes=getBytearray(macReceived);

        //get mac
        Mac hmac= Mac.getInstance("HmacSHA256");
        hmac.init(receiveKey);
        hmac.update(payload.toString().getBytes());
        byte[] macCalculated=hmac.doFinal();

        if(Arrays.equals(macReceivedbytes,macCalculated)){
            System.out.println("mac correct");
        }else{
            System.out.println("mac not correct");
        }

        return payload;
    }

    private byte[] encrypt(String message, int nextIdx, long nextTag)throws Exception{
        //generate message
        JSONObject payload=new JSONObject();
        payload.put("message",message);
        payload.put("nextIdx",nextIdx);
        payload.put("nextTag",nextTag);

        //get mac
        Mac hmac= Mac.getInstance("HmacSHA256");
        hmac.init(sendkey);
        hmac.update(payload.toString().getBytes());
        byte[] macresult=hmac.doFinal();
        String mac= getStringBase64(macresult);

        //put in message
        JSONObject toEncrypt=new JSONObject();
        toEncrypt.put("payload",payload.toString());
        toEncrypt.put("MAC",mac);
        //encrypt this message

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE,sendkey);
        return cipher.doFinal(toEncrypt.toString().getBytes());
    }
    private String getStringBase64(byte[] bytearray){
        return Base64.getEncoder().encodeToString(bytearray);
    }
    private byte[] getBytearray(String string){
        return Base64.getDecoder().decode(string);
    }

    public void bumpSend(SecretKey Key, int Tag, int idx, byte[] salt) {
        sendkey=Key;
        sendTag=Tag;
        sendIdx=idx;
        sendSalt=salt;
    }

    public void bumpReceive(SecretKey Key, int Tag, int idx, byte[] salt) {
        receiveKey=Key;
        receiveTag=Tag;
        receiveIdx=idx;
        receiveSalt=salt;
    }
}
