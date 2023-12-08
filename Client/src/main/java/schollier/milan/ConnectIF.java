package schollier.milan;
import java.rmi.Remote;

public interface ConnectIF extends Remote{
    void write(int idx, String message, String tag);
    String get(int idx, int tag);

}
