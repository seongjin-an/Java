
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Sender {


    public static boolean send(String host, int port, String msg) throws SocketException {


        boolean result = false;

        Socket client = null;
        BufferedOutputStream bos = null;


        try {
            StringBuilder message = new StringBuilder();

            message.append(String.format("%-" + 8 + "s", msg.length()));
            message.append(msg);


            client = new Socket(host, port);

//            client.connect(new InetSocketAddress(host, port));		// 소켓 연결 타입아웃 시간 설정

            bos = new BufferedOutputStream(client.getOutputStream());

            bos.write(message.toString().getBytes(StandardCharsets.UTF_8));
            bos.flush();

            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bos != null){
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (client != null){
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    public static void main(String[] args) throws SocketException {
        send("192.168.229.128", 22222, "hello ansj asdlkfj;lkawej awlkfmnawle;kfj;oaweljfo;iawejfoi  lkaerwjf;olaerwijgoaerg");
    }

}
