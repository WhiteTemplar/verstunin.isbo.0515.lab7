import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Executors;

/**
 * Created by admin on 07.04.2017.
 */
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(Executors.newFixedThreadPool(10));
        AsynchronousServerSocketChannel ass= AsynchronousServerSocketChannel.open(group);
        ass.bind(new InetSocketAddress(80));
        ass.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel s, Void param) {
                ass.accept(null, this);
                process(s);
            }

            @Override
            public void failed(Throwable error, Void param) {
                error.printStackTrace();
            }
        });
        Thread.sleep(60000);
    }
    static void process(AsynchronousSocketChannel s){
        ByteBuffer buf= ByteBuffer.allocate(10240);
        StringBuilder reqest = new StringBuilder();
        s.read(buf,null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer result, Void attachment) {
                buf.flip();
                byte[] data = new byte[buf.remaining()];
                buf.get(data);
                reqest.append(new String(data));
                int len= reqest.length();
                if (len>=4 && reqest.substring(len-4).equals("\r\n\r\n")){
                    System.out.println(reqest);
                    sendRespouse(s);
                }else{
                    buf.clear();
                    s.read(buf,null,this);
                }


            }

            @Override
            public void failed(Throwable exc, Void attachment) {

            }
        });
    }
    static void sendRespouse(AsynchronousSocketChannel s){
        ByteBuffer buf = ByteBuffer.wrap("H".getBytes());
        s.write(buf, null, new CompletionHandler<Integer, Void>(){
            public void completed(Integer result, Void param){
                try{
                    s.close();;
                } catch(IOException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Throwable exc, Void attachment) {

            }
        });
    }
}
