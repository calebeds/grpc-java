package blog.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import greeting.server.GreetingServerImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class BlogServer {
    private static final int PORT = 50051;

    public static void main(String[] args) throws IOException, InterruptedException {

        final MongoClient client = MongoClients.create("mongodb://root:root@localhost:27017");

        Server server = ServerBuilder
                .forPort(PORT)
                .addService(new BlogServiceImpl(client))
                .build();

        server.start();

        System.out.println("Server started");
        System.out.println("Listening on port: " + PORT);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Received Shutdown Request");
            server.shutdown();
            client.close();
            System.out.println("Server stopped");
        }));

        server.awaitTermination();
    }
}
