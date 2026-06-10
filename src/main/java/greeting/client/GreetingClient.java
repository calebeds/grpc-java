package greeting.client;

import com.proto.greeting.GreetingRequest;
import com.proto.greeting.GreetingResponse;
import com.proto.greeting.GreetingServiceGrpc;
import io.grpc.*;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreetingClient {
    public static void main(String[] args) throws InterruptedException {
        if(args.length == 0) {
            System.out.println("Need one argument to work");
            return;
        }

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        switch (args[0]) {
            case "greet": doGreet(channel); break;
            case "greet_many_times": doGreetManyTimes(channel); break;
            case "long_greet": doLongGreet(channel); break;
            case "greet_everyone": doGreetEveryone(channel); break;
            case "greet_with_deadline": doGreetWithDeadline(channel); break;
            default:
                System.out.println("Keyword Invalid: " + args[0]);
        }

        System.out.println("Shutting down");

        channel.shutdown();
    }

    private static void doGreet(final ManagedChannel channel) {
        System.out.println("Enter doGreet");
        GreetingServiceGrpc.GreetingServiceBlockingStub stub = GreetingServiceGrpc.newBlockingStub(channel);
        GreetingResponse response = stub.greet(GreetingRequest.newBuilder().setFirstName("Calebe").build());

        System.out.println("Greeting: " + response.getResult());
    }

    private static void doGreetManyTimes(final ManagedChannel channel) {
        System.out.println("Enter doGreetManyTimes");
        GreetingServiceGrpc.GreetingServiceBlockingStub stub = GreetingServiceGrpc.newBlockingStub(channel);

        stub.greetManyTimes(GreetingRequest.newBuilder().setFirstName("Calebe").build())
                .forEachRemaining(greetingResponse -> {
                    System.out.println(greetingResponse.getResult());
                });
    }

    private static void doLongGreet(final ManagedChannel channel) throws InterruptedException {
        System.out.println("Enter doLongGreet");
        GreetingServiceGrpc.GreetingServiceStub stub = GreetingServiceGrpc.newStub(channel);

        List<String> names = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        Collections.addAll(names, "Calebe", "Mary", "Test");

        StreamObserver<GreetingRequest> stream = stub.longGreet(new StreamObserver<GreetingResponse>() {
            @Override
            public void onNext(final GreetingResponse greetingResponse) {
                System.out.println(greetingResponse.getResult());
            }

            @Override
            public void onError(final Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        for (String name: names) {
            stream.onNext(GreetingRequest.newBuilder().setFirstName(name).build());
        }

        stream.onCompleted();

        latch.await(3, TimeUnit.SECONDS);
    }

    private static void doGreetEveryone(final ManagedChannel channel) throws InterruptedException {
        System.out.println("Enter doGreetEveryone");
        final GreetingServiceGrpc.GreetingServiceStub stub = GreetingServiceGrpc.newStub(channel);
        final CountDownLatch latch = new CountDownLatch(1);

        final StreamObserver<GreetingRequest> streamObserver = stub.greetEveryone(new StreamObserver<GreetingResponse>() {
            @Override
            public void onNext(final GreetingResponse greetingResponse) {
                System.out.println(greetingResponse.getResult());
            }

            @Override
            public void onError(final Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        Arrays.asList("Calebe", "Mary", "Test")
                .forEach(name -> streamObserver.onNext(GreetingRequest.newBuilder().setFirstName(name).build()));

        streamObserver.onCompleted();
        latch.await(3, TimeUnit.SECONDS);
    }

    private static void doGreetWithDeadline(final ManagedChannel channel) {
        System.out.println("Enter doGreetWithDeadline");
        GreetingServiceGrpc.GreetingServiceBlockingStub stub = GreetingServiceGrpc.newBlockingStub(channel);
        GreetingResponse response = stub.withDeadline(Deadline.after(3, TimeUnit.SECONDS))
                .greetWithDeadline(GreetingRequest.newBuilder().setFirstName("Calebe").build());

        System.out.println("Greeting withing deadline: " + response.getResult());

        try {
            response = stub.withDeadlineAfter(100, TimeUnit.MILLISECONDS)
                    .greetWithDeadline(GreetingRequest.newBuilder().setFirstName("Calebe").build());

            System.out.println("Greeting deadline exceeded: " + response.getResult());
        } catch (StatusRuntimeException e) {
            if(e.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED) {
                System.out.println("Deadline has been exceeded");
            } else {
                System.out.println("Got an exception in greetWithDeadline");
                System.out.println(Arrays.asList(e.getStackTrace()));
            }
        }
    }
}
