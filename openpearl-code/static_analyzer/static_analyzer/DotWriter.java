package static_analyzer;

public class DotWriter
{
    private StringBuilder stringBuilder = new StringBuilder();
    private int level = 0;
    private int currentClusterId = -1;

    public DotWriter()
    {
        reset();
    }

    public void writeLine(String content)
    {
        if (content.contains("}"))
        {
            level--;
        }

        for (int i = 0; i < level; i++)
        {
            stringBuilder.append("    ");
        }

        stringBuilder.append(content).append("\n");

        if (content.contains("{"))
        {
            level++;
        }
    }

    public String getContent()
    {
        return stringBuilder.toString();
    }

    public void reset()
    {
        stringBuilder.setLength(0);
        level = 0;
        currentClusterId = -1;
    }

    public String getClusterName()
    {
        currentClusterId++;
        return "cluster" + currentClusterId;
    }
}
