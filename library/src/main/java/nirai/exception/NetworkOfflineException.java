package nirai.exception;

public class NetworkOfflineException extends Exception {
    private static final long serialVersionUID = -9190771299628373370L;
    public NetworkOfflineException() {
        super();
    }
    public NetworkOfflineException(Throwable cause) {
        super(cause);
    }
}
