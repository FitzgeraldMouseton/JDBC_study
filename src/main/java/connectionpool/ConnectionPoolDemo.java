package connectionpool;

public class ConnectionPoolDemo implements Runnable {

    public static void main(String[] args) {

        new ConnectionPool();

        System.out.println("Thread                   Conn");
        System.out.println("------                   ----");

        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(new ConnectionPoolDemo(), "ConnectionPoolDemo_" + i);
            thread.start();
        }
    }

    public void run() {
        for (int i = 0; i < 3; i++) {
            Posts posts = new Posts();
            posts.tryConnection();
        }
    }
}
