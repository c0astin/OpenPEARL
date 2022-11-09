package static_analyzer;

import static_analyzer.AnalyzerMessage.Message;
import static_analyzer.AnalyzerMessage.MessageStack;
import static_analyzer.ControlFlowInterpreter.ControlFlowInterpreter;

import java.util.*;

public class StaticAnalyzer
{
    public static String RELEASE_DATE = "18.02.2022";

    public static String inputFile = "";
    public static String pearlFilename = "";
    public static String baseDirectory = "";

    PlainDotGraph dotGraph;
    ApplicationControlFlowGraph applicationControlFlowGraph;

    public static MessageStack messageStack = new MessageStack();

    private static final Map<String, PathNodeMetadata> pathNodeMetadata = new HashMap<>();

    public static void setPathNodeMetadata(String nodeId, PathNodeMetadata metadata)
    {
        pathNodeMetadata.put(nodeId, metadata);
    }

    public static List<PathNodeMetadata> getPathNodeMetadataByNodeId(String nodeId)
    {
        List<PathNodeMetadata> metadataEntries = new ArrayList<>();

        if (pathNodeMetadata.containsKey(nodeId))
        {
            metadataEntries.add(pathNodeMetadata.get(nodeId));
        }
        else if (nodeId.startsWith("l_"))
        {
            String[] lines = nodeId.substring(2).split("_");

            for (String currentLine : lines)
            {
                int currentRelatedLine = Integer.parseInt(currentLine);

                for (Map.Entry<String, PathNodeMetadata> currentEntry : pathNodeMetadata.entrySet())
                {
                    if (currentEntry.getValue().unknownStatement != null)
                    {
                        if (currentRelatedLine == currentEntry.getValue().codeLineNumber)
                        {
                            metadataEntries.add(currentEntry.getValue());
                        }
                    }
                }
            }
        }

        return metadataEntries;
    }

    public static void main(String[] args)
    {
        System.out.println("StaticAnalyzer " + RELEASE_DATE + "\n");

        if (args.length < 1) {
            printHelp();
            System.exit(1);
            return;
        }

        if (!checkAndProcessArguments(args)) {
            System.exit(1);
            return;
        }

        if (Objects.equals(inputFile, "")) {
            System.err.println("No input file specified");
            System.exit(1);
        }

        pearlFilename = inputFile
                .replace(".prl", "")
                .replace("_d_cfg.dot", "")
                .replace("_cfg.dot", "")
                .replace(".dot", "");

        baseDirectory = System.getProperty("user.dir").replace("\\", "/");

        inputFile = baseDirectory + "/" + pearlFilename + "_d_cfg.dot";

        if (!FileSystemUtils.fileExists(inputFile))
        {
            System.err.println("Input file not found: " + inputFile);
            System.exit(1);
        }

        StaticAnalyzer staticAnalyzer = new StaticAnalyzer();
        staticAnalyzer.readDotGraphInput();

        staticAnalyzer.performTests();
    }

    private static boolean checkAndProcessArguments(String[] args)
    {
        int i = 0;

        boolean result = true;

        while (i < args.length)
        {
            String arg = args[i++];

            if (arg.equals("--help"))
            {
                printHelp();
            }
            else if (arg.charAt(0) != '-')
            {
                inputFile = arg;
            }
            else
            {
                System.err.println("unknown option (" + arg + ")");
                result = false;
            }
        }

        return result;
    }

    private static void printHelp() {

        System.err.println(
                  "java static-analyzer (" + RELEASE_DATE + ")\n"
                + " Options:\n"
                + "  --help                      Print this help message\n"
                + "  inputFile                   OpenPEARL .prl input file in working directory\n"
        );
    }

    private boolean readDotGraphInput()
    {
        dotGraph = PlainDotGraph.fromGraphDotFile(inputFile);

        if (dotGraph == null)
        {
            return false;
        }

        String plainGraphFilename = baseDirectory + "/" + pearlFilename + "_cfg_01.dot";
        FileSystemUtils.writeStringToFile(plainGraphFilename, dotGraph.generateDotFile(), false);

        applicationControlFlowGraph = dotGraph.generateApplicationFlowGraph();

        String applicationGraphFilename = baseDirectory + "/" + pearlFilename + "_cfg_02.dot";
        FileSystemUtils.writeStringToFile(applicationGraphFilename, applicationControlFlowGraph.generateDotFile(), false);

        return true;
    }

    private boolean performTests()
    {
        Graph<ApplicationControlFlowGraphNode> graph = new Graph<>();

        for (Edge current : applicationControlFlowGraph.edges)
        {
            ApplicationControlFlowGraphNode sourceNode = (ApplicationControlFlowGraphNode) current.source;
            ApplicationControlFlowGraphNode targetNode = (ApplicationControlFlowGraphNode) current.target;
            graph.addEdge(sourceNode, targetNode, current.label);
        }

        ControlFlowInterpreter controlFlowInterpreter = new ControlFlowInterpreter();
        controlFlowInterpreter.interpret(applicationControlFlowGraph, graph);

        CounterMap<Message.Severity> results = new CounterMap<>();

        for (Message.Severity severity : Message.Severity.values())
        {
            List<Message> messages = StaticAnalyzer.messageStack.get(severity);

            if (messages.size() > 0)
            {
                for (Message message : messages)
                {
                    while (message.content.endsWith("\n"))
                    {
                        message.content = message.content.substring(0, message.content.length() - 1);
                    }

                    System.out.println(message.severity + ": " + message.content + "\n");

                    results.add(message.severity);
                }
            }
        }

        System.out.println();

        for (Message.Severity severity : Message.Severity.values())
        {
            int currentCount = results.get(severity);

            if (currentCount > 0)
            {
                System.out.println(currentCount + "x " + severity);
            }
        }

        if (results.isEmpty())
        {
            System.out.println("All analyses passed");
        }

        System.out.println();

        return true;
    }
}
