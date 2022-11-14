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

package static_analyzer.ControlFlowInterpreter;

import static_analyzer.AnalyzerMessage.Message;
import static_analyzer.ApplicationControlFlowGraphNode;
import static_analyzer.DeadlockOperation;

import java.util.ArrayList;
import java.util.List;

public class ControlFlowInterpreterPath
{
    List<ApplicationControlFlowGraphNode> path = new ArrayList<>();

    boolean correct = false;

    boolean notAllRequestedSema = false;
    boolean notAllReleasedSema = false;
    boolean notAllReservedBolt = false;
    boolean notAllFreedBolt = false;
    boolean notAllEnteredBolt = false;
    boolean notAllLeavedBolt = false;

    List<DeadlockOperation> performedDeadlockOperations = new ArrayList<>();
    List<Message> messageStackReferences = new ArrayList<>();

    public void apply()
    {
        if (path == null)
        {
            path = new ArrayList<>();
        }

        performedDeadlockOperations.clear();

        for (ApplicationControlFlowGraphNode node : path)
        {
            if (node.deadlockOperation != null)
            {
                performedDeadlockOperations.add(node.deadlockOperation);
            }
        }
    }
}
