package rellotge;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Date;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import common.BufferManager;
import common.KeyLog;

/*
 * a simple static http server
*/
@SuppressWarnings("restriction")
public class HttpServ {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(9000), 0);
        server.createContext("/rellotge", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class MyHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            processQuery(t.getRequestURI().getQuery());
            byte[] response = "OK!!".getBytes();
            t.sendResponseHeaders(200, response.length);
            OutputStream os = t.getResponseBody();
            os.write(response);
            os.close();
        }
        private void processQuery(String query) {
            String[] elementsQuery = query.split("&");
            String optionSelected = "";
            String idUser = "";
            for (int i = 0; i < elementsQuery.length; i++) {
                String[] pair = elementsQuery[i].split("=");
                if(pair[0].equals("optionSelected")) {
                	optionSelected = pair[1];
                }
                if(pair[0].equals("idUser")) {
                	idUser = pair[1];
                }
            }
            
            String buffer2="opt:"+optionSelected+";iduser:"+idUser;
            String d = KeyLog.sdf.format(new Date());
    		buffer2 = (buffer2+"/"+d);    		
			BufferManager.movementsFunction(BufferManager.PUT, buffer2);
            
        }        
    }
}
