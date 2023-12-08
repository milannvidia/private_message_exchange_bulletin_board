package schollier.milan;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.rmi.registry.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class Client
{
    public static Random rng=new Random();
    KeyGenerator kg=KeyGenerator.getInstance("AES");
    SecretKeyFactory kdf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    public ArrayList<Contact> contacts=new ArrayList<>();
    public String name;
    ConnectIF connect;
    GUI gui;

    public boolean write(int idx,String message,String tag){
        return connect.write(idx,message,tag);
    }
    public String get(int idx, int tag){
        return connect.get(idx,tag);
    }

    public SecretKey kdfGen(PBEKeySpec spec) throws Exception {
        byte[] generated= kdf.generateSecret(spec).getEncoded();
        return new SecretKeySpec(generated, 0,generated.length,"AES");
    }
    public Client(String name) throws NoSuchAlgorithmException {
        this.name=name;
        kg.init(256);
        try{
            Registry myRegistry= LocateRegistry.getRegistry("localhost",1099);
            connect=(ConnectIF) myRegistry.lookup("Server");

        }catch (Exception e){
            e.printStackTrace();
        }
        gui=new GUI(this);
    }


    private void addContact(Contact contact) {
        this.contacts.add(contact);
    }

    private void receive() throws Exception {
        for (Contact c:this.contacts) {
            c.receive();
        }
    }
    //static functions
    private static void bump(Client a, Client b){
        SecretKey MasterKey_AB=a.kg.generateKey();
        SecretKey MasterKey_BA=b.kg.generateKey();
        Contact contactA=new Contact(a.name,b);
        Contact contactB=new Contact(b.name,a);

        int ABidx=rng.nextInt(20);
        int BAidx=rng.nextInt(20);
        int ABTag=rng.nextInt();
        int BATag=rng.nextInt();

        byte[] ABsalt=new byte[64];
        new Random().nextBytes(ABsalt);
        byte[] BAsalt=new byte[64];
        new Random().nextBytes(BAsalt);

        //ab interaction
        contactA.bumpSend(MasterKey_AB, ABTag, ABidx, ABsalt);
        contactB.bumpReceive(MasterKey_AB, ABTag, ABidx, ABsalt);
        //ba interaction
        contactB.bumpSend(MasterKey_BA, BATag, BAidx, BAsalt);
        contactA.bumpReceive(MasterKey_BA, BATag, BAidx, BAsalt);


        a.addContact(contactB);
        b.addContact(contactA);
        a.gui.update();
        b.gui.update();
    }
    public static void main( ) throws Exception {
        Client A=new Client("Jan");
        Client B=new Client("Piet");
        bump(A,B);
        while (true){
            TimeUnit.SECONDS.sleep(1);
            A.receive();
            B.receive();
        }
    }

}

