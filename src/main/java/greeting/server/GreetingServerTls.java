package greeting.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.File;
import java.io.IOException;

public class GreetingServerTls {
    private static final int PORT = 50051;

    public static void main(String[] args) throws IOException, InterruptedException {

        Server server = ServerBuilder
                .forPort(PORT)
                .useTransportSecurity(
                        new File("ssl/server.crt"),
                        new File("ssl/server.pem")
                )
                .addService(new GreetingServerImpl())
                .build();

        server.start();

        System.out.println("Server started");
        System.out.println("Listening on port: " + PORT);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Received Shutdown Request");
            server.shutdown();
            System.out.println("Server stopped");
        }));

        server.awaitTermination();
    }
}
