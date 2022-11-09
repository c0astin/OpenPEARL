package static_analyzer;

import java.util.ArrayList;
import java.util.List;

public class Procedure
{
    int codeLineNumber = 0;
    String codeFilename = "";

    String procedureName = "";
    List<Edge> procedureEdges = new ArrayList<>();
    List<String> callNodes = new ArrayList<>();

    String startNode = "";
    String endNode = "";

    boolean global = false;
}
