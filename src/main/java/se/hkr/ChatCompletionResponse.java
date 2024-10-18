package se.hkr;

import java.util.List;

public record ChatCompletionResponse(
        String id,
        String object,
        Long created,
        String model,
        List<Choice> choices,
        Usage usage
) {
    public record Choice(
            Integer index,
            Message message,
            String finish_reason
    ) {}

    public record Message(
            String role,
            String content
    ) {}

    public record Usage(
            Integer prompt_tokens,
            Integer completion_tokens,
            Integer total_tokens
    ) {}
}
