package schollier.milan;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ConnectIF extends Remote{
    boolean write(int idx, String message, String tag) throws RemoteException;
    String get(int idx, int tag) throws RemoteException;
}
