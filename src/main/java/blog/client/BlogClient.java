package blog.client;

import com.google.protobuf.Empty;
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

        readBlog(stub, blogId);
        updateBlog(stub, blogId);
        listBlogs(stub);
    }

    private static void listBlogs(BlogServiceGrpc.BlogServiceBlockingStub stub) {
        stub.listBlogs(Empty.getDefaultInstance()).forEachRemaining(System.out::print);
    }

    private static void readBlog(BlogServiceGrpc.BlogServiceBlockingStub stub, BlogId blogId) {
        try {
            Blog readResponse = stub.readBlog(blogId);

            System.out.println("Blog read: " + readResponse);
        } catch (StatusRuntimeException ex) {
            System.out.println("Couldn't read the blog.");
            System.out.println(Arrays.asList(ex.getStackTrace()));
        }
    }

    private static void updateBlog(BlogServiceGrpc.BlogServiceBlockingStub stub, BlogId blogId) {
        try {
            final Blog newBlog = Blog.newBuilder()
                    .setId(blogId.getId())
                    .setAuthor("Calebe Oliveira")
                    .setTitle("New Blog (Changed)")
                    .setContent("Hello world this is my first blog! I've added some content")
                    .build();
            stub.updateBlog(newBlog);
            System.out.println("Blog updated: " + newBlog);
        } catch (StatusRuntimeException e) {
            System.out.println("Could't update the blog");
            System.out.println(Arrays.asList(e.getStackTrace()));
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
