package calculator.server;

import com.proto.calculator.*;
import io.grpc.stub.StreamObserver;

public class CalculatorServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {
    @Override
    public void sum(final SumRequest request, final StreamObserver<SumResponse> responseObserver) {
        responseObserver.onNext(SumResponse.newBuilder().setResult(
                request.getFirstNumber() + request.getSecondNumber()
        ).build());
        responseObserver.onCompleted();
    }

    @Override
    public void primes(final PrimeRequest request, final StreamObserver<PrimeResponse> responseObserver) {
        int number = request.getNumber();
        int divisor = 2;

        while (number > 1) {
            if(number % divisor == 0) {
                number = number / divisor;
                responseObserver.onNext(PrimeResponse.newBuilder().setPrimeFactor(divisor).build());
            } else {
                divisor++;
            }
        }

        responseObserver.onCompleted();
    }
}
