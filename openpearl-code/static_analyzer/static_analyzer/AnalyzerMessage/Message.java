package static_analyzer.AnalyzerMessage;

public class Message
{
    public static enum Severity
    {
        ERROR, WARNING, INFO
    }

    public Severity severity;
    public String content = "";
    public String problem = "";
    public int priority = 0;

    public Message(Severity severity)
    {
        this.severity = severity;
    }

    public boolean isSame(Message message)
    {
        return (problem.equals(message.problem));
    }

    public void actualizePriority(Message message)
    {
        if ((isSame(message)) && (priority < message.priority))
        {
            severity = message.severity;
            content = message.content;
            priority = message.priority;
        }
    }
}
