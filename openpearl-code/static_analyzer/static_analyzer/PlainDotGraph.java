/*
 * [A "BSD license"]
 *  Copyright (c) 2022 Jan Knoblauch
 *  
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package static_analyzer;

//import javax.json.*;
//import javax.json.stream.JsonParser;
//
//import org.glassfish.json.JsonParserImpl;

import java.util.*;

//import com.eclipsesource.json.Json;

//import com.eclipsesource.json.JsonArray;
//import com.eclipsesource.json.JsonObject;
//import com.eclipsesource.json.JsonValue;

import static_analyzer.AnalyzerMessage.Message;
import static_analyzer.ControlFlowGraphEntities.AbstractTaskControl;
import static_analyzer.ControlFlowGraphEntities.FlowControl;
import static_analyzer.ControlFlowGraphEntities.TaskControlActivate;
import static_analyzer.ControlFlowGraphEntities.UnknownStatement;

public class PlainDotGraph {
	private static final String DOT_FILE_BLOCK_UNKNOWN = "DOT_FILE_BLOCK_UNKNOWN";
	private static final String DOT_FILE_BLOCK_PROCEDURE = "DOT_FILE_BLOCK_PROCEDURE";
	private static final String DOT_FILE_BLOCK_TASK = "DOT_FILE_BLOCK_TASK";

	List<Edge> edges = new ArrayList<>();

	Map<String, String> nodeLabels = new HashMap<>();
	Map<String, UnknownStatement> irrelevantNodes = new HashMap<>();

	Map<String, Procedure> procedures = new HashMap<>();
	Map<String, Task> tasks = new HashMap<>();

	Map<String, List<DeadlockOperation>> deadlockOperations = new HashMap<>();
	Map<String, DeadlockOperation> deadlockOperationNodes = new HashMap<>();

	private final List<Edge> exportedEdges = new ArrayList<>();
	private DotWriter dotWriter;

	Map<String, FlowControl> flowControlStatementsByNodeId = new HashMap<>();

	Map<String, DeadlockResource> deadlockResources = new HashMap<>();

	Map<String, List<String>> irrelevantNodePaths = new HashMap<>();
	Map<String, IGraphNode> irrelevantNodeCompoundNodes = new HashMap<>();

	public static String getDeadlockOperationId(DeadlockOperation deadlockOperation) {
		StringBuilder id = new StringBuilder(
				"a=" + deadlockOperation.actionType + ",t=" + deadlockOperation.resourcesType + ",r=[");

		for (String resourceIdentifier : deadlockOperation.resourceIdentifiers) {
			id.append(resourceIdentifier).append(",");
		}

		id = new StringBuilder(id.substring(0, id.length() - 1) + "]");

		return id.toString();
	}

	public static List<String> getDeadlockOperationIds(DeadlockOperation deadlockOperation) {
		List<String> ids = new ArrayList<>();

		String id = "a=" + deadlockOperation.actionType + ",t=" + deadlockOperation.resourcesType + ",r=";

		for (String resourceIdentifier : deadlockOperation.resourceIdentifiers) {
			ids.add(id + resourceIdentifier);
		}

		return ids;
	}

	public void addDeadlockOperation(DeadlockOperation deadlockOperation) {
		String deadlockOperationId = getDeadlockOperationId(deadlockOperation);

		List<DeadlockOperation> currentList = deadlockOperations.get(deadlockOperationId);

		if (currentList == null) {
			currentList = new ArrayList<>();
		}

		currentList.add(deadlockOperation);

		deadlockOperations.put(deadlockOperationId, currentList);
	}

	public void removeDuplicateEdges() {
		List<Edge> duplicates = new ArrayList<>();

		for (int outerIndex = 0; outerIndex < edges.size() - 1; outerIndex++) {
			for (int innerIndex = outerIndex + 1; innerIndex < edges.size(); innerIndex++) {
				if (edges.get(outerIndex).isSame(edges.get(innerIndex), true)) {
					duplicates.add(edges.get(outerIndex));
				}
			}
		}

		for (Edge duplicate : duplicates) {
			edges.remove(duplicate);
		}
	}

	public void removeDeclarations() {
		List<Edge> declarations = new ArrayList<>();

		for (Edge edge : edges) {
			String sourceNodeLabel = getNodeLabel(edge.source);
			String targetNodeLabel = getNodeLabel(edge.target);

			if ((sourceNodeLabel.startsWith("DCL")) || (targetNodeLabel.startsWith("DCL"))) {
				declarations.add(edge);
			}
		}

		for (Edge declaration : declarations) {
			edges.remove(declaration);
		}
	}

	public String generateDotFile() {
		prepareGraphExport();

		dotWriter.writeLine("digraph G {");

		dotWriter.writeLine("subgraph " + dotWriter.getClusterName() + " {");
		dotWriter.writeLine("label = \"ApplicationStart\"");
		dotWriter.writeLine("applicationStart [ label=\"applicationStart\" ]");
		for (Map.Entry<String, Task> taskEntry : tasks.entrySet()) {
			if (taskEntry.getValue().runAtStartup) {
				dotWriter.writeLine("applicationStart -> Start_" + taskEntry.getValue().taskIdentifier);
			}
		}
		dotWriter.writeLine("}");

		dotWriter.writeLine("subgraph " + dotWriter.getClusterName() + " {");
		dotWriter.writeLine("label = \"InactiveTasks\"");
		for (Map.Entry<String, Task> taskEntry : tasks.entrySet()) {
			if (!taskEntry.getValue().runAtStartup) {
				dotWriter.writeLine("Start_" + taskEntry.getValue().taskIdentifier + " [ label=\"Start_"
						+ taskEntry.getValue().taskIdentifier + "\" ]");
			}
		}
		dotWriter.writeLine("}");

		dotWriter.writeLine("subgraph " + dotWriter.getClusterName() + " {");
		dotWriter.writeLine("label = \"ApplicationEnd\"");
		dotWriter.writeLine("applicationEnd [ label=\"applicationEnd\" ]");
		for (Map.Entry<String, Task> taskEntry : tasks.entrySet()) {
			dotWriter.writeLine("End_" + taskEntry.getValue().taskIdentifier + " -> applicationEnd");
		}
		dotWriter.writeLine("}");

		for (Map.Entry<String, Procedure> entry : procedures.entrySet()) {
			dotWriter.writeLine("subgraph " + dotWriter.getClusterName() + " {");
			dotWriter.writeLine("label = \"" + entry.getValue().procedureName + "\"");

			for (Edge edge : entry.getValue().procedureEdges) {
				writeEdge(edge);
			}

			dotWriter.writeLine("}");
		}

		for (Map.Entry<String, Task> taskEntry : tasks.entrySet()) {
			for (AbstractTaskControl abstractTaskControl : taskEntry.getValue().taskControlStatements) {
				if (abstractTaskControl instanceof TaskControlActivate) {
					dotWriter.writeLine(abstractTaskControl.statementLine + " -> Start_"
							+ abstractTaskControl.targetTaskIdentifier + " [ label=\"" + "system" + "\" ] ");
				}
			}
		}

		for (Edge edge : edges) {
			writeEdge(edge);
		}

		dotWriter.writeLine("}\n");

		return dotWriter.getContent();
	}

	private void prepareGraphExport() {
		removeDuplicateEdges();
		removeDeclarations();

		exportedEdges.clear();

		dotWriter = new DotWriter();
	}

	public ApplicationControlFlowGraph generateApplicationFlowGraph() {
		prepareGraphExport();

		ApplicationControlFlowGraph applicationControlFlowGraph = new ApplicationControlFlowGraph();
		applicationControlFlowGraph.tasks = tasks;
		applicationControlFlowGraph.deadlockResources = deadlockResources;

		Subgraph clusterApplicationStart = new Subgraph();
		clusterApplicationStart.label = "ApplicationStart";
		ApplicationControlFlowGraphNode graphNodeApplicationStart = new ApplicationControlFlowGraphNode(
				"applicationStart", "applicationStart");
		clusterApplicationStart.nodes.add(graphNodeApplicationStart);
		applicationControlFlowGraph.objectsByNodeId.put("applicationStart", graphNodeApplicationStart);
		for (Map.Entry<String, Task> taskEntry : tasks.entrySet()) {
			if (taskEntry.getValue().runAtStartup) {
				clusterApplicationStart.edges.add(new Edge(graphNodeApplicationStart,
						new ApplicationControlFlowGraphNode("Start_" + taskEntry.getValue().taskIdentifier)));
				applicationControlFlowGraph.objectsByNodeId.put("Start_" + taskEntry.getValue().taskIdentifier,
						taskEntry);
			}
		}

		Subgraph clusterInactiveTasks = new Subgraph();
		clusterInactiveTasks.label = "InactiveTasks";
		applicationControlFlowGraph.objectsByNodeId.put("activate", null);
		for (Map.Entry<String, Task> taskEntry : tasks.entrySet()) {
			if (!taskEntry.getValue().runAtStartup) {
				ApplicationControlFlowGraphNode inactiveTaskNode = new ApplicationControlFlowGraphNode(
						"Start_" + taskEntry.getValue().taskIdentifier, "Start_" + taskEntry.getValue().taskIdentifier);
				clusterInactiveTasks.nodes.add(inactiveTaskNode);
				applicationControlFlowGraph.objectsByNodeId.put("Start_" + taskEntry.getValue().taskIdentifier,
						taskEntry);
			}
		}

		Subgraph clusterApplicationEnd = new Subgraph();
		clusterApplicationEnd.label = "ApplicationEnd";
		ApplicationControlFlowGraphNode graphNodeApplicationEnd = new ApplicationControlFlowGraphNode("applicationEnd",
				"applicationEnd");
		clusterApplicationEnd.nodes.add(graphNodeApplicationEnd);
		applicationControlFlowGraph.objectsByNodeId.put("applicationEnd", null);
		for (Map.Entry<String, Task> taskEntry : tasks.entrySet()) {
			clusterApplicationEnd.edges
					.add(new Edge(new ApplicationControlFlowGraphNode("End_" + taskEntry.getValue().taskIdentifier),
							graphNodeApplicationEnd));
			applicationControlFlowGraph.objectsByNodeId.put("End_" + taskEntry.getValue().taskIdentifier, taskEntry);
		}

		applicationControlFlowGraph.subgraphs.add(clusterApplicationStart);
		applicationControlFlowGraph.subgraphs.add(clusterInactiveTasks);
		applicationControlFlowGraph.subgraphs.add(clusterApplicationEnd);

		for (Map.Entry<String, Procedure> entry : procedures.entrySet()) {
			Subgraph procedureSubgraph = new Subgraph();
			procedureSubgraph.label = entry.getValue().procedureName;

			List<Edge> sortedProcedureEdges = compactIrrelevantEdges(entry.getValue().procedureEdges);

			for (Edge edge : sortedProcedureEdges) {
				addEdge(procedureSubgraph.edges, edge);

				setEdgeObjects(applicationControlFlowGraph, edge);
			}

			applicationControlFlowGraph.subgraphs.add(procedureSubgraph);
		}

		List<Edge> sortedEdges = compactIrrelevantEdges(edges);

		for (Map.Entry<String, Task> taskEntry : tasks.entrySet()) {
			for (AbstractTaskControl abstractTaskControl : taskEntry.getValue().taskControlStatements) {
				if (abstractTaskControl instanceof TaskControlActivate) {
					sortedEdges.add(new Edge(new ApplicationControlFlowGraphNode(abstractTaskControl.statementLine),
							new ApplicationControlFlowGraphNode("Start_" + abstractTaskControl.targetTaskIdentifier),
							"system"));

					applicationControlFlowGraph.objectsByNodeId.put(abstractTaskControl.statementLine,
							abstractTaskControl);
				}
			}
		}

		for (Edge edge : sortedEdges) {
			addEdge(applicationControlFlowGraph.root.edges, edge);

			setEdgeObjects(applicationControlFlowGraph, edge);
		}

		for (Subgraph subgraph : applicationControlFlowGraph.subgraphs) {
			for (Edge edge : subgraph.edges) {
				edge.subgraph = subgraph;
				applicationControlFlowGraph.edges.add(edge);

				setEdgeObjects(applicationControlFlowGraph, edge);
			}
		}

		for (Edge edge : applicationControlFlowGraph.root.edges) {
			edge.subgraph = applicationControlFlowGraph.root;
			applicationControlFlowGraph.edges.add(edge);

			setEdgeObjects(applicationControlFlowGraph, edge);
		}

		applicationControlFlowGraph.reduce();

		return applicationControlFlowGraph;
	}

	private Object getNodeObject(String nodeId) {
		if (flowControlStatementsByNodeId.get(nodeId) != null) {
			return flowControlStatementsByNodeId.get(nodeId);
		}

		return null;
	}

	private void setEdgeObjects(ApplicationControlFlowGraph graph, Edge edge) {
		graph.objectsByNodeId.put(edge.source.getId(), getNodeObject(edge.source.getId()));
		graph.objectsByNodeId.put(edge.target.getId(), getNodeObject(edge.target.getId()));
	}

	private List<Edge> compactIrrelevantEdges(List<Edge> edges) {
		irrelevantNodePaths.clear();
		irrelevantNodeCompoundNodes.clear();

		List<String> edgeLabels = new ArrayList<>();

		for (Edge edge : edges) {
			String edgeLabel = Utils.toDef(edge.label);

			if (!edgeLabels.contains(edgeLabel)) {
				edgeLabels.add(edgeLabel);
			}
		}

		List<Edge> result = new ArrayList<>();

		for (String edgeLabel : edgeLabels) {
			result.addAll(compactIrrelevantEdgesWithLabel(edges, edgeLabel));
		}

		return result;
	}

	private List<Edge> compactIrrelevantEdgesWithLabel(List<Edge> edges, String label) {
		List<IGraphNode> nodes = new ArrayList<>();
		IntegerGraph integerGraph = new IntegerGraph();
		List<Edge> sortedEdges = new ArrayList<>();

		List<Edge> edgesWithCurrentLabel = new ArrayList<>();

		for (Edge edge : edges) {
			if (Utils.equalStrings(label, edge.label)) {
				edgesWithCurrentLabel.add(edge);
			}
		}

		for (Edge edge : edgesWithCurrentLabel) {
			boolean containsSource = false;

			for (IGraphNode iGraphNode : nodes) {
				if (iGraphNode.getId().equals(edge.source.getId())) {
					containsSource = true;
					break;
				}
			}

			boolean containsTarget = false;

			for (IGraphNode iGraphNode : nodes) {
				if (iGraphNode.getId().equals(edge.target.getId())) {
					containsTarget = true;
					break;
				}
			}

			if (!containsSource) {
				nodes.add(edge.source);
			}

			if (!containsTarget) {
				nodes.add(edge.target);
			}
		}

		for (Edge edge : edgesWithCurrentLabel) {
			int sourceIndex = 0;
			int targetIndex = 0;

			for (IGraphNode iGraphNode : nodes) {
				if (iGraphNode.getId().equals(edge.source.getId())) {
					break;
				}

				sourceIndex++;
			}

			for (IGraphNode iGraphNode : nodes) {
				if (iGraphNode.getId().equals(edge.target.getId())) {
					break;
				}

				targetIndex++;
			}

			integerGraph.addEdge(sourceIndex, targetIndex, edge.label);
		}

		List<Integer> compressedNodes = new ArrayList<>();
		List<Integer> currentCompressedNodes = new ArrayList<>();

		List<Integer> startNodeIndexes = integerGraph.getStartNodes();

		for (int currentIndex : startNodeIndexes) {
			List<Integer> startNodeExclusivePath = integerGraph.getExclusivePathFromNode(currentIndex, false);

			currentCompressedNodes.add(currentIndex);

			currentCompressedNodes.addAll(startNodeExclusivePath);

			if (currentCompressedNodes.size() > 1) {
				compactIrrelevantEdgesInBlock(nodes, currentCompressedNodes, sortedEdges, label);
			}

			compressedNodes.addAll(currentCompressedNodes);

			currentCompressedNodes.clear();
		}

		List<List<Integer>> exclusivePathsSorted = new ArrayList<>();
		Map<Integer, Integer> exclusivePathLength = new HashMap<>();

		Map<Integer, List<Integer>> exclusivePaths = new HashMap<>();

		for (int currentIndex : integerGraph.getNodeIndexes()) {
			List<Integer> exclusivePath = integerGraph.getExclusivePathFromNode(currentIndex, false);

			if (exclusivePath.size() > 0) {
				exclusivePaths.put(currentIndex, exclusivePath);
				exclusivePathLength.put(currentIndex, exclusivePath.size());
			}
		}

		exclusivePathLength.entrySet().stream().sorted((k1, k2) -> -k1.getValue().compareTo(k2.getValue()))
				.forEach(k -> exclusivePathsSorted.add(exclusivePaths.get(k.getKey())));

		for (List<Integer> path : exclusivePathsSorted) {
			boolean valid = true;

			for (Integer pathNode : path) {
				if (compressedNodes.contains(pathNode)) {
					valid = false;
					break;
				}
			}

			if (!valid) {
				continue;
			}

			compactIrrelevantEdgesInBlock(nodes, path, sortedEdges, label);

			compressedNodes.addAll(path);
		}

		for (Edge edge : edges) {
			if ((irrelevantNodes.containsKey(edge.source.getId()))
					&& (irrelevantNodes.containsKey(edge.target.getId()))) {
				// Irrelevant -> irrelevant
			} else {
				if (irrelevantNodes.containsKey(edge.source.getId())) {
					edge.source = getIrrelevantCompoundNode(edge.source);
				} else if (irrelevantNodes.containsKey(edge.target.getId())) {
					edge.target = getIrrelevantCompoundNode(edge.target);
				}

				if ((edge.label.equals(label)) && (!listContainsEdge(sortedEdges, edge, true))) {
					sortedEdges.add(edge);
				}
			}
		}

		return sortedEdges;
	}

	private boolean listContainsEdge(List<Edge> list, Edge edge, boolean compareNodeLabels) {
		for (Edge current : list) {
			if (current.isSame(edge, compareNodeLabels)) {
				return true;
			}
		}

		return false;
	}

	private void compactIrrelevantEdgesInBlock(List<IGraphNode> nodes, List<Integer> sort, List<Edge> sortedEdges,
			String label) {
		List<UnknownStatement> currentIrrelevantNodes = new ArrayList<>();
		int startCodeLineNumber = -1;
		boolean inIrrelevantBlock = false;

		List<IGraphNode> reducedNodes = new ArrayList<>();

		for (Integer index : sort) {
			IGraphNode graphNode = nodes.get(index);

			if (irrelevantNodes.containsKey(graphNode.getId())) {
				inIrrelevantBlock = true;

				UnknownStatement currentStatement = irrelevantNodes.get(graphNode.getId());

				currentIrrelevantNodes.add(currentStatement);

				if ((startCodeLineNumber == -1) || (currentStatement.codeLineNumber < startCodeLineNumber)) {
					startCodeLineNumber = currentStatement.codeLineNumber;
				}
			} else {
				if (inIrrelevantBlock) {
					inIrrelevantBlock = false;

					String irrelevantNodeId = "";

					List<String> currentIrrelevantNodeIds = new ArrayList<>();

					for (UnknownStatement currentStatement : currentIrrelevantNodes) {
						irrelevantNodeId += "_" + currentStatement.codeLineNumber;
						currentIrrelevantNodeIds.add(currentStatement.nodeId);
					}

					IGraphNode currentIrrelevantCompoundNode = new GraphNode("l" + irrelevantNodeId, null);

					reducedNodes.add(currentIrrelevantCompoundNode);
					reducedNodes.add(graphNode);
					irrelevantNodePaths.put("l" + irrelevantNodeId, currentIrrelevantNodeIds);
					irrelevantNodeCompoundNodes.put("l" + irrelevantNodeId, currentIrrelevantCompoundNode);

					currentIrrelevantNodes.clear();
				} else {
					reducedNodes.add(graphNode);
				}
			}
		}

		if (inIrrelevantBlock) {
			String irrelevantNodeId = "";

			List<String> currentIrrelevantNodeIds = new ArrayList<>();

			for (UnknownStatement currentStatement : currentIrrelevantNodes) {
				irrelevantNodeId += "_" + currentStatement.codeLineNumber;
				currentIrrelevantNodeIds.add(currentStatement.nodeId);
			}

			IGraphNode currentIrrelevantCompoundNode = new GraphNode("l" + irrelevantNodeId, null);

			reducedNodes.add(currentIrrelevantCompoundNode);
			irrelevantNodePaths.put("l" + irrelevantNodeId, currentIrrelevantNodeIds);
			irrelevantNodeCompoundNodes.put("l" + irrelevantNodeId, currentIrrelevantCompoundNode);

			currentIrrelevantNodes.clear();
		}

		IGraphNode sourceGraphNode = null;

		for (IGraphNode graphNode : reducedNodes) {
			if (sourceGraphNode != null) {
				Edge edge = new Edge(sourceGraphNode, graphNode, label);
				sortedEdges.add(edge);
			}

			sourceGraphNode = graphNode;
		}
	}

	private IGraphNode getIrrelevantCompoundNode(IGraphNode node) {
		String notCompoundNodeId = node.getId();

		IGraphNode compoundNode = null;

		for (Map.Entry<String, List<String>> current : irrelevantNodePaths.entrySet()) {
			if (current.getValue().contains(notCompoundNodeId)) {
				compoundNode = irrelevantNodeCompoundNodes.get(current.getKey());
				break;
			}
		}

		if (compoundNode != null) {
			node = compoundNode;
		}

		return node;
	}

	private void writeEdge(Edge edge) {
		for (Edge currentEdge : exportedEdges) {
			if (currentEdge.isSame(edge, false)) {
				return;
			}
		}

		String currentLine = getNodeLabel(edge.source) + " -> " + getNodeLabel(edge.target);

		if (!edge.label.equals("")) {
			currentLine += " [ label=\"" + edge.label + "\" ]";
		}

		dotWriter.writeLine(currentLine);

		exportedEdges.add(edge);
	}

	private void addEdge(List<Edge> edgeList, Edge edge) {
		for (Edge currentEdge : exportedEdges) {
			if (currentEdge.isSame(edge, false)) {
				return;
			}
		}

		edge.source = generateApplicationGraphNode(edge.source);
		edge.target = generateApplicationGraphNode(edge.target);

		if ((edge.source == null) || (edge.target == null)) {
			return;
		}

		edgeList.add(edge);

		exportedEdges.add(edge);
	}

	public String getEdgeDump(Edge edge) {
		String content = getNodeLabel(edge.source) + " -> " + getNodeLabel(edge.target);

		if (!Utils.toDef(edge.label).isEmpty()) {
			content += " [" + edge.label + "]";
		}

		return content;
	}

	public String getNodeLabel(IGraphNode graphNode) {
		if (irrelevantNodes.containsKey(graphNode.getId())) {
			return "l_" + irrelevantNodes.get(graphNode.getId()).codeLineNumber + "_irrelevant_"
					+ graphNode.getId().replace("node", "");
		}

		String nodeLabel = nodeLabels.get(graphNode.getId());

		if ((nodeLabel != null) && (nodeLabel.length() > 0)) {
			return nodeLabel;
		}

		DeadlockOperation deadlockOperation = deadlockOperationNodes.get(graphNode.getId());

		if (deadlockOperation != null) {
			return deadlockOperation.getLabel();
		}

		return graphNode.getId();
	}

	public ApplicationControlFlowGraphNode generateApplicationGraphNode(IGraphNode graphNode) {
		ApplicationControlFlowGraphNode applicationGraphNode = new ApplicationControlFlowGraphNode(graphNode.getId(),
				graphNode.getLabel());

		String nodeLabel = nodeLabels.get(graphNode.getId());

		if (irrelevantNodes.containsKey(graphNode.getId())) {
			nodeLabel = "l_" + irrelevantNodes.get(graphNode.getId()).codeLineNumber + "_irrelevant_"
					+ graphNode.getId().replace("node", "");
		}

		if (nodeLabel != null) {
			applicationGraphNode.setLabel(nodeLabel);
		}

		DeadlockOperation deadlockOperation = deadlockOperationNodes.get(graphNode.getId());

		if (deadlockOperation != null) {
			applicationGraphNode.setLabel(deadlockOperation.getLabel());
			applicationGraphNode.deadlockOperation = deadlockOperation;
		}

		return applicationGraphNode;
	}

	private boolean constructPlainGraph(String filePath) {
		String content = FileSystemUtils.readFileAsString(filePath);

		if ((content == null) || (content.equals(""))) {
			return false;
		}

		String[] lines = content.split("\\r?\\n");

		int graphLevel = 0;
		boolean commentActive = false;

		String currentBlockMode = DOT_FILE_BLOCK_UNKNOWN;

		String currentTaskName = "";
		String currentProcedureName = "";

		for (String currentLine : lines) {
			currentLine = currentLine.trim();

			if (currentLine.equals("")) {
				continue;
			}

			if (graphLevel > 0) {
				if (currentLine.equals("}")) {
					graphLevel--;

					currentBlockMode = DOT_FILE_BLOCK_UNKNOWN;

					continue;
				}

				if (currentLine.startsWith("/*")) {
					if (!currentLine.endsWith("*/")) {
						commentActive = true;
					}
				} else if (currentLine.endsWith("*/")) {
					commentActive = false;
				} else if (!commentActive) {
					if (currentLine.contains("Procedure: ")) {
						currentBlockMode = DOT_FILE_BLOCK_PROCEDURE;

						currentProcedureName = currentLine.replace("label = \"Procedure: ", "").replace("\"", "");

						Procedure currentProcedureObject = procedures.get(currentProcedureName);

						if (currentProcedureObject == null) {
							currentProcedureObject = new Procedure();
							currentProcedureObject.procedureName = currentProcedureName;
						}

						procedures.put(currentProcedureName, currentProcedureObject);
					} else if (currentLine.contains("Task: ")) {
						currentBlockMode = DOT_FILE_BLOCK_TASK;

						currentTaskName = currentLine.replace("label = \"Task: ", "").replace("\"", "");

						Task currentTaskObject = tasks.get(currentTaskName);

						if (currentTaskObject == null) {
							currentTaskObject = new Task();
						}

						tasks.put(currentTaskName, currentTaskObject);
					} else if (currentLine.contains(" -> ")) {
						String[] lineEdges = currentLine.split(">");

						if (lineEdges.length == 2) {
							lineEdges[0] = lineEdges[0].replace("-", "").replace(";", "").trim();
							lineEdges[1] = lineEdges[1].replace("-", "").replace(";", "").trim();

							Edge currentEdge = new Edge(new GraphNode(lineEdges[0]),
									new GraphNode(lineEdges[1])/* , lineEdges[0] + " to " + lineEdges[1] */);

							if (currentBlockMode.equals(DOT_FILE_BLOCK_TASK)) {
								currentEdge.label = currentTaskName;
							}

							edges.add(currentEdge);

							if (currentBlockMode.equals(DOT_FILE_BLOCK_PROCEDURE)) {
								if (!currentProcedureName.isEmpty()) {
									Procedure currentProcedureObject = procedures.get(currentProcedureName);

									currentProcedureObject.procedureEdges.add(currentEdge);

									procedures.put(currentProcedureName, currentProcedureObject);
								}
							} else if (currentBlockMode.equals(DOT_FILE_BLOCK_TASK)) {
								if (!currentTaskName.isEmpty()) {
									Task currentTaskObject = tasks.get(currentTaskName);

									currentTaskObject.taskEdges.add(currentEdge);

									tasks.put(currentTaskName, currentTaskObject);
								}
							}
						}
					} else if ((currentLine.contains(" [ ")) && (currentLine.endsWith(" ]"))) {
						boolean inLabel = false;
						StringBuilder nodeIdBuilder = new StringBuilder();
						StringBuilder nodeLabelBuilder = new StringBuilder();

						for (char current : currentLine.toCharArray()) {
							if ((!inLabel) && (current == '[')) {
								inLabel = true;
							} else {
								if (!inLabel) {
									nodeIdBuilder.append(current);
								} else {
									nodeLabelBuilder.append(current);
								}
							}
						}

						String nodeId = nodeIdBuilder.toString().trim();
						String nodeLabel = nodeLabelBuilder.toString().trim();

						PathNodeMetadata pathNodeMetadata = new PathNodeMetadata();

						//nodeLabel = nodeLabel.replace("label=\"", "").replace("\" ]", "").replace("'", "\"");
						// decoding step1: remove starting part 'label="' and trailing part '" ]' of the node
						nodeLabel = nodeLabel.replace("label=\"", "").replace("\" ]", "");
						
						//if ((nodeLabel.startsWith("{\"")) && (nodeLabel.endsWith("}"))) {
						// step 2: check if the content looks like json --> is in {}
    					if ((nodeLabel.startsWith("{")) && (nodeLabel.endsWith("}"))) {
    						// fine we have a json which where the keys and values delimiters were encode for dot
    						
    						String decoded = "";
    						for (int i=0; i<nodeLabel.length()-1; i++) {
    							if (nodeLabel.charAt(i) == '\\') {
    								if (nodeLabel.charAt(i+1) == '"') {
    									decoded += '"';
    									i++;
    								} else if (nodeLabel.charAt(i+1) == '\\') {
    									decoded += '\\';
    									i++;
    								}
    							} else {
    								decoded += nodeLabel.charAt(i);
    							}
    							
    						}
							decoded += nodeLabel.charAt(nodeLabel.length()-1);
							MyJsonObject jsonObject = new MyJsonObject(decoded);

							String jsonClass = jsonObject.getString("class", "");

							if (jsonClass.equals("DeadlockOperation")) {
								DeadlockOperation deadlockOperation = new DeadlockOperation();
								deadlockOperation.id = nodeId;
								deadlockOperation.resourcesType = jsonObject.getString("resourcesType", "");
								deadlockOperation.actionType = jsonObject.getString("actionType", "");
								deadlockOperation.codeFilename = jsonObject.getString("codeFilename", "");
								deadlockOperation.codeLineNumber = jsonObject.getInt("codeLineNumber", 0);

								for (String resourceIdentifier : jsonObject.getStringArray("resourceIdentifiers")) {
									deadlockOperation.resourceIdentifiers.add(resourceIdentifier);
								}

								addDeadlockOperation(deadlockOperation);

								deadlockOperationNodes.put(nodeId, deadlockOperation);

								pathNodeMetadata.deadlockOperation = deadlockOperation;
							} else if (jsonClass.equals("DeadlockControlFlowGraphTask")) {
								String taskIdentifier = jsonObject.getString("taskIdentifier", "");

								if (!taskIdentifier.isEmpty()) {
									Task currentTaskData = tasks.get(taskIdentifier);
									currentTaskData.startNode = nodeId;
									currentTaskData.taskIdentifier = taskIdentifier;
									currentTaskData.runAtStartup = jsonObject.getBoolean("runAtStartup", false);
									currentTaskData.global = jsonObject.getBoolean("global", false);

									if (currentTaskData.global) {
										Message globalTaskMessage = new Message(Message.Severity.WARNING);
										globalTaskMessage.problem = "global task " + taskIdentifier;
										globalTaskMessage.content = "The task " + taskIdentifier
												+ " is global. At the moment, the analysis considers all modules separately and does not take into account control of the task in other modules";
										StaticAnalyzer.messageStack.add(globalTaskMessage);
									}

									pathNodeMetadata.task = currentTaskData;
								}
							} else if (jsonClass.equals("DeadlockResourceDeclaration")) {
								DeadlockResource deadlockResource = new DeadlockResource();

								deadlockResource.resourceIdentifier = jsonObject.getString("resourceIdentifier", null);

								if ((deadlockResource.resourceIdentifier != null)
										&& (deadlockResource.resourceIdentifier.length() > 0)) {
									deadlockResource.resourceType = jsonObject.getString("resourceType", null);
									deadlockResource.codeFilename = jsonObject.getString("codeFilename", null);
									deadlockResource.codeLineNumber = jsonObject.getInt("codeLineNumber", -1);
									deadlockResource.presetValue = jsonObject.getInt("presetValue", 0);
									deadlockResource.global = jsonObject.getBoolean("global", false);
									deadlockResources.put(deadlockResource.resourceIdentifier, deadlockResource);

									if (deadlockResource.global) {
										Message globalResourceMessage = new Message(Message.Severity.WARNING);
										globalResourceMessage.problem = "global resource "
												+ deadlockResource.resourceType + " "
												+ deadlockResource.resourceIdentifier;
										globalResourceMessage.content = "The resource " + deadlockResource.resourceType
												+ " " + deadlockResource.resourceIdentifier
												+ " is global. At the moment, the analysis considers all modules separately and does not take into account usages of the resource in other modules";
										StaticAnalyzer.messageStack.add(globalResourceMessage);
									}

									pathNodeMetadata.deadlockResource = deadlockResource;
								}
							} else if (jsonClass.equals("DeadlockControlFlowGraphProcedure")) {
								String procedureIdentifier = jsonObject.getString("procedureIdentifier", null);

								Procedure procedure = procedures.get(procedureIdentifier);

								if (procedure == null) {
									procedure = new Procedure();
									procedure.procedureName = procedureIdentifier;
								}

								if ((procedure.procedureName != null) && (procedure.procedureName.length() > 0)) {
									procedure.codeFilename = jsonObject.getString("codeFilename", null);
									procedure.codeLineNumber = jsonObject.getInt("codeLineNumber", -1);
									procedure.global = jsonObject.getBoolean("global", false);

									if (procedure.global) {
										Message globalProcedureMessage = new Message(Message.Severity.WARNING);
										globalProcedureMessage.problem = "global procedure " + procedure.procedureName;
										globalProcedureMessage.content = "The procedure " + procedure.procedureName
												+ " is global. At the moment, the analysis considers all modules separately and does not consider calls of the procedure in other modules";
										StaticAnalyzer.messageStack.add(globalProcedureMessage);
									}

									procedures.put(procedure.procedureName, procedure);

									pathNodeMetadata.procedure = procedure;
								}
							} else if (jsonClass.equals("ProcedureCall")) {
								String procedureName = jsonObject.getString("procedure", null);

								Procedure calledProcedureObject = procedures.get(procedureName);

								if (calledProcedureObject == null) {
									calledProcedureObject = new Procedure();
									calledProcedureObject.procedureName = procedureName;
								}

								calledProcedureObject.callNodes.add(nodeId);

								procedures.put(procedureName, calledProcedureObject);

								pathNodeMetadata.codeFilename = jsonObject.getString("codeFilename", null);
								pathNodeMetadata.codeLineNumber = jsonObject.getInt("codeLineNumber", 0);
								pathNodeMetadata.statement = "CALL " + calledProcedureObject.procedureName;
							} else if (jsonClass.equals("TaskControlActivate")) {
								TaskControlActivate taskControlActivate = new TaskControlActivate();
								taskControlActivate.nodeId = nodeId;
								taskControlActivate.codeFilename = jsonObject.getString("codeFilename", null);
								taskControlActivate.codeLineNumber = jsonObject.getInt("codeLineNumber", 0);
								taskControlActivate.sourceTaskIdentifier = currentTaskName;
								taskControlActivate.targetTaskIdentifier = jsonObject.getString("taskIdentifier", null);
								taskControlActivate.statementLine = "l_" + taskControlActivate.codeLineNumber
										+ "_ACTIVATE_" + taskControlActivate.targetTaskIdentifier;

								tasks.get(currentTaskName).taskControlStatements.add(taskControlActivate);

								nodeLabels.put(nodeId, taskControlActivate.statementLine);

								pathNodeMetadata.taskControlActivate = taskControlActivate;
							} else if (jsonClass.equals("FlowControl")) {
								FlowControl flowControl = new FlowControl();
								flowControl.codeFilename = jsonObject.getString("codeFilename", null);
								flowControl.codeLineNumber = jsonObject.getInt("codeLineNumber", 0);
								flowControl.nodeId = nodeId;
								flowControl.type = jsonObject.getString("type", "");
								flowControl.statement = jsonObject.getString("statement", "");

								flowControlStatementsByNodeId.put(flowControl.nodeId, flowControl);

								nodeLabels.put(nodeId, flowControl.getNodeLabel());

								pathNodeMetadata.flowControl = flowControl;
							} else if (jsonClass.equals("Unknown")) {
								UnknownStatement unknownStatement = new UnknownStatement();
								unknownStatement.codeFilename = jsonObject.getString("codeFilename", null);
								unknownStatement.codeLineNumber = jsonObject.getInt("codeLineNumber", 0);
								unknownStatement.statement = jsonObject.getString("statement", "");
								unknownStatement.nodeId = nodeId;

								irrelevantNodes.put(nodeId, unknownStatement);

								pathNodeMetadata.unknownStatement = unknownStatement;
							}

							if (!nodeId.trim().isEmpty()) {
								pathNodeMetadata.apply();

								StaticAnalyzer.setPathNodeMetadata(nodeId, pathNodeMetadata);
							}
						} else if ((!currentBlockMode.equals(DOT_FILE_BLOCK_UNKNOWN)) && (nodeLabel.equals("Entry"))) {
							if (currentBlockMode.equals(DOT_FILE_BLOCK_PROCEDURE)) {
								procedures.get(currentProcedureName).startNode = nodeId;

								nodeLabels.put(nodeId, "Entry_" + currentProcedureName);
							} else if (currentBlockMode.equals(DOT_FILE_BLOCK_TASK)) {

							}
						} else if ((!currentBlockMode.equals(DOT_FILE_BLOCK_UNKNOWN)) && (nodeLabel.equals("End"))) {
							if (currentBlockMode.equals(DOT_FILE_BLOCK_PROCEDURE)) {
								procedures.get(currentProcedureName).endNode = nodeId;

								nodeLabels.put(nodeId, "End_" + currentProcedureName);
							} else if (currentBlockMode.equals(DOT_FILE_BLOCK_TASK)) {
								tasks.get(currentTaskName).endNode = nodeId;

								nodeLabels.put(nodeId, "End_" + currentTaskName);
							}
						}
					}
				}
			}

			if ((currentLine.startsWith("digraph")) || (currentLine.startsWith("subgraph"))) {
				graphLevel++;
			}
		}

		for (Map.Entry<String, Procedure> procedureEntry : procedures.entrySet()) {
			for (String callNode : procedureEntry.getValue().callNodes) {
				for (Edge edge : edges) {
					if (edge.target.getId().equals(callNode)) {
						edge.target = new GraphNode(procedureEntry.getValue().startNode);
					} else if (edge.source.getId().equals(callNode)) {
						edge.source = new GraphNode(procedureEntry.getValue().endNode);
					}
				}
			}
		}

		for (Map.Entry<String, Task> taskEntry : tasks.entrySet()) {
			for (Edge edge : edges) {
				if (edge.source.getId().equals(taskEntry.getValue().startNode)) {
					edge.source = new GraphNode("Start_" + taskEntry.getValue().taskIdentifier);
				}
			}
		}

		return true;
	}

	public static PlainDotGraph fromGraphDotFile(String filePath) {
		PlainDotGraph dotGraph = new PlainDotGraph();

		if (!dotGraph.constructPlainGraph(filePath)) {
			return null;
		}

		return dotGraph;
	}
}
