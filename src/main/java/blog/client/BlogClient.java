package blog.client;

import com.proto.blog.Blog;
import com.proto.blog.BlogId;
import com.proto.blog.BlogServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.Arrays;

public class BlogClient {
    public static void main(String[] args) {

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        run(channel);

        System.out.println("Shutting down");

        channel.shutdown();
    }

    private static void run(final ManagedChannel channel) {
        BlogServiceGrpc.BlogServiceBlockingStub stub = BlogServiceGrpc.newBlockingStub(channel);

        final BlogId blogId = createBlog(stub);

        if(blogId == null) {
            return;
        }
    }

    private static BlogId createBlog(final BlogServiceGrpc.BlogServiceBlockingStub stub) {
        try {
            final BlogId createdResponse = stub.createBlog(Blog.newBuilder()
                    .setAuthor("Calebe")
                    .setTitle("New Blog!")
                    .setContent("Hello World, this is a new blog!")
                    .build());

            System.out.println("Blog created: " + createdResponse.getId());
            return createdResponse;
        } catch (final StatusRuntimeException ex) {
            System.out.println("Couldn't create the blog");
            System.out.println(Arrays.asList(ex.getStackTrace()));
            return null;
        }
    }
}
