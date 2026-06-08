package greeting.server;

import com.proto.greeting.GreetingRequest;
import com.proto.greeting.GreetingResponse;
import com.proto.greeting.GreetingServiceGrpc;
import io.grpc.stub.StreamObserver;

import java.util.stream.IntStream;

public class GreetingServerImpl extends GreetingServiceGrpc.GreetingServiceImplBase {
    @Override
    public void greet(final GreetingRequest request, final StreamObserver<GreetingResponse> responseObserver) {
        responseObserver.onNext(GreetingResponse.newBuilder().setResult("Hello " + request.getFirstName()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void greetManyTimes(final GreetingRequest request, final StreamObserver<GreetingResponse> responseObserver) {
        GreetingResponse response = GreetingResponse.newBuilder().setResult("Hello " + request.getFirstName()).build();

        IntStream.range(0, 10)
                .forEach(number -> {
                    responseObserver.onNext(response);
                });

        responseObserver.onCompleted();
    }
}
