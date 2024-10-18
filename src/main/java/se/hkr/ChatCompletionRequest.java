package se.hkr;

import java.util.List;

public record ChatCompletionRequest(
        String model,
        List<Message> messages,
        Double temperature,
        Integer max_tokens,
        Double top_p,
        Integer n,
        Boolean stream,
        String stop
) {
    public ChatCompletionRequest(String model, List<Message> messages) {
        this(model, messages, 1.0, 2048, 1.0, 1, false, null);
    }
    public record Message(String role, String content) {}
}
