package blog.server;

import com.google.protobuf.Empty;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.proto.blog.Blog;
import com.proto.blog.BlogId;
import com.proto.blog.BlogServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Objects;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;

public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    private final MongoCollection<Document> mongoCollection;

    public BlogServiceImpl(final MongoClient client) {
        final MongoDatabase db = client.getDatabase("blogdb");

        this.mongoCollection = db.getCollection("blog");
    }

    @Override
    public void createBlog(final Blog request, final StreamObserver<BlogId> responseObserver) {
        final Document doc = new Document("author", request.getAuthor())
                .append("title", request.getTitle())
                .append("content", request.getContent());

        InsertOneResult result;

        try {
            result = mongoCollection.insertOne(doc);
        } catch (final MongoException exception) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(exception.getLocalizedMessage())
                    .asRuntimeException());
            return;
        }

        if(!result.wasAcknowledged() || result.getInsertedId() == null) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Blog couldn't be created")
                    .asRuntimeException());
            return;
        }

        final String id = result.getInsertedId().asObjectId().getValue().toString();

        responseObserver.onNext(BlogId.newBuilder().setId(id).build());
        responseObserver.onCompleted();
    }

    @Override
    public void readBlog(final BlogId request, final StreamObserver<Blog> responseObserver) {
        if(request.getId().isEmpty()) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("The blog id cannot be empty")
                    .asRuntimeException());
            return;
        }

        final String id = request.getId();
        final Document result = mongoCollection.find(eq("_id", new ObjectId(id))).first();

        if(result == null) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("Blog was not found")
                    .augmentDescription("Blog id: " + id)
                    .asRuntimeException());
            return;
        }

        responseObserver.onNext(Blog.newBuilder()
                .setAuthor(result.getString("author"))
                .setTitle(result.getString("title"))
                .setContent(result.getString("content"))
                .build());

        responseObserver.onCompleted();
    }

    @Override
    public void updateBlog(final Blog request, final StreamObserver<Empty> responseObserver) {
        if(request.getId().isEmpty()) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("The blog id cannot be empty")
                    .asRuntimeException());
            return;
        }

        final String id = request.getId();
        final Document result = mongoCollection.findOneAndUpdate(eq("_id", new ObjectId(id)),
                combine(
                        Updates.set("author", request.getAuthor()),
                        Updates.set("title", request.getTitle()),
                        Updates.set("content", request.getContent())
                ));

        if(result == null) {
            responseObserver.onError(Status.NOT_FOUND
                            .withDescription("The blog was not found")
                            .augmentDescription("Blog ID: " + id)
                    .asRuntimeException());

            return;
        }

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void listBlogs(final Empty request, final StreamObserver<Blog> responseObserver) {
        for(final Document document: mongoCollection.find()) {
            responseObserver.onNext(Blog.newBuilder()
                            .setId(document.getObjectId("_id").toString())
                            .setAuthor(document.getString("author"))
                            .setTitle(document.getString("title"))
                            .setContent(document.getString("content"))
                    .build());
        }

        responseObserver.onCompleted();
    }

    @Override
    public void deleteBlog(final BlogId request, final StreamObserver<Empty> responseObserver) {
        if(request.getId().isEmpty()) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("The blog id cannot be empty")
                    .asRuntimeException());
            return;
        }

        final String id = request.getId();

        DeleteResult result;

        try {
            result = mongoCollection.deleteOne(eq("_id", new ObjectId(id)));
        } catch (MongoException ex) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("The blog couldn't be deleted")
                    .asRuntimeException());
            return;
        }

        if(!result.wasAcknowledged()) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("The blog couldn't be deleted")
                    .asRuntimeException());

            return;
        }

        if(result.getDeletedCount() == 0) {
            responseObserver.onError(Status.NOT_FOUND
                            .withDescription("The blog was not found")
                            .augmentDescription("BlogId: " + id)
                    .asRuntimeException());
            return;
        }

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }
}
