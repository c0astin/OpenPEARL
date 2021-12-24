/*
 * [A "BSD license"]
 *  Copyright (c) 2021 Ilja Mascharow
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

package org.openpearl.compiler.SemanticAnalysis;

import org.openpearl.compiler.ControlFlowGraph.ControlFlowGraph;
import org.openpearl.compiler.ControlFlowGraph.ControlFlowGraphNode;
import org.openpearl.compiler.ControlFlowGraph.ControlFlowGraphNodeType;
import org.openpearl.compiler.ErrorStack;

import java.util.*;

public class CheckUnreachableStatements {

    private List<ControlFlowGraph> cfgs;

    public CheckUnreachableStatements(List<ControlFlowGraph> cfgs) {
        this.cfgs = cfgs;
    }

    public void check() {
        cfgs.forEach(cfg -> {
            //Find all not visited Nodes
            ControlFlowGraphNode entryControlFlowGraphNode = cfg.getEntryNode();
            Set<ControlFlowGraphNode> visitedControlFlowGraphNodes = new HashSet<>();
            visitedControlFlowGraphNodes.add(entryControlFlowGraphNode);
            Queue<ControlFlowGraphNode> toCheckControlFlowGraphNodes = new ArrayDeque<>(entryControlFlowGraphNode.getInputNodes());
            while (!toCheckControlFlowGraphNodes.isEmpty()) {
                ControlFlowGraphNode controlFlowGraphNode = toCheckControlFlowGraphNodes.remove();
                if(visitedControlFlowGraphNodes.contains(controlFlowGraphNode)) continue;
                visitedControlFlowGraphNodes.add(controlFlowGraphNode);

                toCheckControlFlowGraphNodes.addAll(controlFlowGraphNode.getInputNodes());
            }
            Set<ControlFlowGraphNode> notVisitedControlFlowGraphNodes = new HashSet<>(cfg.getGraph());
            notVisitedControlFlowGraphNodes.removeAll(visitedControlFlowGraphNodes);

            //Group the Nodes
            Set<List<ControlFlowGraphNode>> notVisitedNodesGroups = new HashSet<>();
            Set<ControlFlowGraphNode> alreadyGroupedControlFlowGraphNodes = new HashSet<>();
            notVisitedControlFlowGraphNodes.forEach(notVisitedControlFlowGraphNode -> {
                if(alreadyGroupedControlFlowGraphNodes.contains(notVisitedControlFlowGraphNode) || visitedControlFlowGraphNodes.contains(notVisitedControlFlowGraphNode)) return;

                List<ControlFlowGraphNode> currentGroup = new ArrayList<>();
                currentGroup.add(notVisitedControlFlowGraphNode);
                alreadyGroupedControlFlowGraphNodes.add(notVisitedControlFlowGraphNode);

                Queue<ControlFlowGraphNode> queue = new ArrayDeque<>(notVisitedControlFlowGraphNode.getInputNodes());
                queue.addAll(notVisitedControlFlowGraphNode.getOutputNodes());
                while (!queue.isEmpty()) {
                    ControlFlowGraphNode controlFlowGraphNode = queue.remove();
                    if(alreadyGroupedControlFlowGraphNodes.contains(controlFlowGraphNode) || visitedControlFlowGraphNodes.contains(controlFlowGraphNode)) continue;

                    currentGroup.add(controlFlowGraphNode);
                    alreadyGroupedControlFlowGraphNodes.add(controlFlowGraphNode);

                    if(controlFlowGraphNode.getInputNodes().size() == 1)
                        queue.addAll(controlFlowGraphNode.getInputNodes());
                    if(controlFlowGraphNode.getOutputNodes().size() == 1)
                        queue.addAll(controlFlowGraphNode.getOutputNodes());
                }
                notVisitedNodesGroups.add(currentGroup);
            });

            //Output Warning for every highest Node in every Group
            List<ControlFlowGraphNode> highestNotNullControlFlowGraphNodes = new ArrayList<>();
            notVisitedNodesGroups.forEach(group -> {
                group.sort(Comparator.comparing(ControlFlowGraphNode::getId));
                for(ControlFlowGraphNode controlFlowGraphNode : group) {
                    if(controlFlowGraphNode.getCtx() != null) {
                        if(controlFlowGraphNode.getType() != ControlFlowGraphNodeType.THEN_START
                                && controlFlowGraphNode.getType() != ControlFlowGraphNodeType.ELSE_START
                                && controlFlowGraphNode.getType() != ControlFlowGraphNodeType.ALT_START
                                && controlFlowGraphNode.getType() != ControlFlowGraphNodeType.OUT_START ) {
                            highestNotNullControlFlowGraphNodes.add(controlFlowGraphNode);
                            return;
                        }
                    }
                }
            });
            highestNotNullControlFlowGraphNodes.sort(Comparator.comparing(ControlFlowGraphNode::getId));
            highestNotNullControlFlowGraphNodes.forEach(controlFlowGraphNode -> {
                ErrorStack.warn(controlFlowGraphNode.getCtx(), "unreachable", "Code is not reachable");
            });
        });
    }
}
