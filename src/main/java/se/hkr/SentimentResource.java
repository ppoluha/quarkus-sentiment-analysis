package se.hkr;

import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Path("/analyze")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SentimentResource {

    @RestClient
    OpenAIClient openAIClient;

    @POST
    @RunOnVirtualThread
    public Response analyzeSentiment(ReviewRequest request) {
        long startTime = System.currentTimeMillis();
        List<CompletableFuture<String>> futureSentiments;
        try (ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            futureSentiments = new ArrayList<>();
            for (String review : request.reviews) {
                String prompt = "Analyze the sentiment in the following text and determine whether it is positive, negative, or neutral. Answer with one word.\n\n" + review;
                CompletableFuture<String> futureCompletion = CompletableFuture.supplyAsync(() -> {
                    var req = new ChatCompletionRequest("gpt-4o-mini", List.of(new ChatCompletionRequest.Message("user", prompt)));
                    ChatCompletionResponse resp = openAIClient.getCompletion(req);
                    return resp.choices().getFirst().message().content().trim();
                }, virtualThreadExecutor);
                futureSentiments.add(futureCompletion);
            }
        }
        // Wait for all futures to complete and collect the results
        List<String> sentiments = futureSentiments.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        System.out.println("Elapsed time parallel: " + (System.currentTimeMillis() - startTime));
        return Response.ok(sentiments).build();
    }

    public Response analyzeSentimentSequential(ReviewRequest request) {
        long startTime = System.currentTimeMillis();
        List<String> sentiments = new ArrayList<>();
        for (String review : request.reviews) {
            String prompt = "Analyze the sentiment in the following text and determine whether it is positive, negative, or neutral. Answer with one word.\n\n" + review;
            var req = new ChatCompletionRequest("gpt-4o-mini", List.of(new ChatCompletionRequest.Message("user", prompt)));
            ChatCompletionResponse resp = openAIClient.getCompletion(req);
            String sentiment = resp.choices().getFirst().message().content().trim();
            sentiments.add(sentiment);
        }
        System.out.println("Elapsed time sequential: " + (System.currentTimeMillis() - startTime));
        return Response.ok(sentiments).build();
    }
}
