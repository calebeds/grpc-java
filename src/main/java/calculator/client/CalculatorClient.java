package calculator.client;

import com.proto.calculator.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class CalculatorClient {
    public static void main(String[] args) throws InterruptedException {
        if(args.length == 0) {
            System.out.println("Need one argument to work");
            return;
        }

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        switch (args[0]) {
            case "sum": doSum(channel); break;
            case "primes": doPrimes(channel); break;
            case "avg": doAvg(channel); break;
            default:
                System.out.println("Keyword Invalid: " + args[0]);
        }

    }

    private static void doSum(final ManagedChannel channel) {
        System.out.println("Enter doSum");
        CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);
        SumResponse response = stub.sum(SumRequest.newBuilder().setFirstNumber(1).setSecondNumber(1).build());

        System.out.println("Sum 1 + 1 = " + response.getResult());
    }

    private static void doPrimes(final ManagedChannel channel) {
        System.out.println("Enter doPrimes");
        CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = CalculatorServiceGrpc.newBlockingStub(channel);

        stub.primes(PrimeRequest.newBuilder().setNumber(567890).build())
                .forEachRemaining(response -> {
                    System.out.println(response.getPrimeFactor());
                });
    }

    private static void doAvg(final ManagedChannel channel) throws InterruptedException {
        System.out.println("Enter doAvg");
        CalculatorServiceGrpc.CalculatorServiceStub stub = CalculatorServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<AvgRequest> stream = stub.avg(new StreamObserver<AvgResponse>() {
            @Override
            public void onNext(final AvgResponse avgResponse) {
                System.out.println("Avg = " + avgResponse.getResult());
            }

            @Override
            public void onError(final Throwable throwable) {

            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        IntStream.rangeClosed(1, 10)
                .forEach(number -> stream.onNext(AvgRequest.newBuilder().setNumber(number).build()));

        stream.onCompleted();
        latch.await(3, TimeUnit.SECONDS);
    }
}
