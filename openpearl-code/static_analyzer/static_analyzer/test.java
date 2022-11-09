package static_analyzer;

import static_analyzer.ControlFlowInterpreter.ControlFlowInterpreter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class test
{
    public static void main(String[] args)
    {
        doTests();
    }

    private static void doTests()
    {
        IntegerGraph integerGraph = new IntegerGraph();
        List<Integer> sort = new ArrayList<>();

        // Test: Get all paths of a graph
        if (true)
        {
            integerGraph.addEdge(1, 2);
            integerGraph.addEdge(2, 3);
            integerGraph.addEdge(3, 4);
            integerGraph.addEdge(4, 5);
            integerGraph.addEdge(5, 6);
            integerGraph.addEdge(6, 7);
            integerGraph.addEdge(7, 8);

            integerGraph.addEdge(5, 40);
            integerGraph.addEdge(40, 3);

            integerGraph.addEdge(7, 60);
            integerGraph.addEdge(60, 70);
            integerGraph.addEdge(70, 80);
            integerGraph.addEdge(80, 7);

            printPathsOfGraph(integerGraph);
            integerGraph.reset();

            integerGraph.addEdge(1, 2);
            integerGraph.addEdge(2, 3);
            integerGraph.addEdge(3, 4);
            integerGraph.addEdge(4, 5);
            integerGraph.addEdge(5, 6);
            integerGraph.addEdge(6, 7);
            integerGraph.addEdge(7, 8);

            integerGraph.addEdge(3, 40);
            integerGraph.addEdge(40, 50);
            integerGraph.addEdge(50, 60);
            integerGraph.addEdge(60, 7);

            integerGraph.addEdge(50, 600);
            integerGraph.addEdge(600, 7);

            printPathsOfGraph(integerGraph);
        }

        // Test: Get nodes topological sorted without cycle
        if (true)
        {
            integerGraph.reset();

            integerGraph.addEdge(3, 0, "t1");
            integerGraph.addEdge(0, 1, "t1");
            integerGraph.addEdge(3, 2, "t2");
            integerGraph.addEdge(2, 1, "t2");

            sort = integerGraph.topologicalSort();

            for (Integer current : sort)
            {
                System.out.println("---> " + current);
            }
        }

        // Test: Get nodes topological sorted with cycle (1 -> 3 -> 2 -> 1)
        if (true)
        {
            integerGraph.reset();

            integerGraph.addEdge(0, 1, "A");
            integerGraph.addEdge(0, 2, "B");
            integerGraph.addEdge(0, 3, "C");
            integerGraph.addEdge(1, 3, "A");
            integerGraph.addEdge(3, 2, "B");
            integerGraph.addEdge(2, 1, "C");

            sort = integerGraph.topologicalSort();

            for (Integer current : sort)
            {
                System.out.println("---> " + current);
            }
        }

        // Test: Get nodes topological sorted without cycle
        if (true)
        {
            integerGraph.reset();

            integerGraph.addEdge(1, 0);
            integerGraph.addEdge(1, 2);
            integerGraph.addEdge(1, 4);
            integerGraph.addEdge(2, 3);
            integerGraph.addEdge(4, 3);
            integerGraph.addEdge(4, 5);
            integerGraph.addEdge(5, 6);

            sort = integerGraph.topologicalSort();

            for (Integer current : sort)
            {
                System.out.println("---> " + current);
            }
        }

        // Test: Compare paths
        if (true)
        {
            List<ApplicationControlFlowGraphNode> pathA = new ArrayList<>();
            List<ApplicationControlFlowGraphNode> pathB = new ArrayList<>();

            pathA.add(new ApplicationControlFlowGraphNode("a"));
            pathA.add(new ApplicationControlFlowGraphNode("b"));
            pathA.add(new ApplicationControlFlowGraphNode("c"));
            pathA.add(new ApplicationControlFlowGraphNode("d"));
            pathA.add(new ApplicationControlFlowGraphNode("d2"));
            pathA.add(new ApplicationControlFlowGraphNode("d3"));
            pathA.add(new ApplicationControlFlowGraphNode("d4"));
            pathA.add(new ApplicationControlFlowGraphNode("e"));
            pathA.add(new ApplicationControlFlowGraphNode("f"));
            pathA.add(new ApplicationControlFlowGraphNode("g"));
            pathA.add(new ApplicationControlFlowGraphNode("h"));
            pathA.add(new ApplicationControlFlowGraphNode("i"));

            pathB.add(new ApplicationControlFlowGraphNode("a"));
            pathB.add(new ApplicationControlFlowGraphNode("b"));
            pathB.add(new ApplicationControlFlowGraphNode("c"));
            pathB.add(new ApplicationControlFlowGraphNode("d"));
            pathB.add(new ApplicationControlFlowGraphNode("d500"));
            pathB.add(new ApplicationControlFlowGraphNode("e"));
            pathB.add(new ApplicationControlFlowGraphNode("f"));
            pathB.add(new ApplicationControlFlowGraphNode("f2"));
            pathB.add(new ApplicationControlFlowGraphNode("f3"));
            pathB.add(new ApplicationControlFlowGraphNode("f4"));
            pathB.add(new ApplicationControlFlowGraphNode("f5"));
            pathB.add(new ApplicationControlFlowGraphNode("f6"));
            pathB.add(new ApplicationControlFlowGraphNode("f7"));
            pathB.add(new ApplicationControlFlowGraphNode("g"));
            pathB.add(new ApplicationControlFlowGraphNode("h"));
            pathB.add(new ApplicationControlFlowGraphNode("i"));

            System.out.println(ControlFlowInterpreter.comparePaths(pathA, pathB));
        }
    }

    private static void printPathsOfGraph(IntegerGraph integerGraph)
    {
        List<List<Integer>> paths2 = integerGraph.getPathsByLabel(1, null);

        for (List<Integer> path : paths2)
        {
            System.out.println("---> " + Arrays.toString(path.toArray()));
        }

        System.out.println("\n");
    }
}
