package schollier.milan;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Hello world!
 *
 */
public class Server
{

    private void startServer(){
        try{
            Registry registry=LocateRegistry.createRegistry(1099);
            registry.rebind("Server",new ConnectImpl());
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("server running");
    }
    public static void main()
    {
        Server main=new Server();
        main.startServer();
    }
}
