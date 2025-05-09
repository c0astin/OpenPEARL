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

public class Edge
{
    public IGraphNode source;
    public IGraphNode target;
    public String label = "";
    public Subgraph subgraph = null;

    public Edge(IGraphNode source, IGraphNode target)
    {
        this(source, target, "");
    }

    public Edge(IGraphNode source, IGraphNode target, String label)
    {
        this.source = source;
        this.target = target;
        this.label = label;
    }

    public static Edge copy(Edge edge)
    {
        return new Edge(edge.source.copy(), edge.target.copy(), edge.label);
    }

    public boolean isSame(Edge edge, boolean compareNodeLabels)
    {
        if ((!source.isSame(edge.source, compareNodeLabels)) || (!target.isSame(edge.target, compareNodeLabels)))
        {
            return false;
        }

        if (label == null)
        {
            return (edge.label == null);
        }
        else
        {
            return (label.equals(edge.label));
        }
    }

    public String dump()
    {
        return source.getId() + " -> " + target.getId();
    }
}
