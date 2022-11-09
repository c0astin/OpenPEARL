package static_analyzer.AnalyzerMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MessageStack
{
    private final List<Message> messages = new ArrayList<>();

    public void add(Message message)
    {
        for (Message currentMessage : messages)
        {
            if (currentMessage.isSame(message))
            {
                currentMessage.actualizePriority(message);

                return;
            }
        }

        messages.add(message);
    }

    public void remove(String problem)
    {
        List<Message> removedMessages = new ArrayList<>();

        for (Message currentMessage : messages)
        {
            if (currentMessage.problem.equals(problem))
            {
                removedMessages.add(currentMessage);
            }
        }

        messages.removeAll(removedMessages);
    }

    public List<Message> get(Message.Severity severity)
    {
        List<Message> currentSeverityMessages = new ArrayList<>();

        for (Message currentMessage : messages)
        {
            if (currentMessage.severity == severity)
            {
                currentSeverityMessages.add(currentMessage);
            }
        }

        return currentSeverityMessages;
    }

    public Message getByProblem(String problem)
    {
        for (Message currentMessage : messages)
        {
            if (Objects.equals(currentMessage.problem, problem))
            {
                return currentMessage;
            }
        }

        return null;
    }
}
