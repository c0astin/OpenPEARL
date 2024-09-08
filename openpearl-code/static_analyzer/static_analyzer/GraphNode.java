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

public class GraphNode implements IGraphNode
{
    protected String id = "";
    protected String label = "";

    public GraphNode(String id)
    {
        this(id, "");
    }

    public GraphNode(String id, String label)
    {
        this.id = id;
        this.label = label;
    }

    @Override
    public String getLabel()
    {
        return label;
    }

    @Override
    public void setLabel(String label)
    {
        this.label = label;
    }

    @Override
    public String getId()
    {
        return id;
    }

    public static GraphNode copy(GraphNode graphNode)
    {
        return new GraphNode(graphNode.getId(), graphNode.getLabel());
    }

    @Override
    public boolean isSame(IGraphNode graphNode, boolean compareLabels)
    {
        if (!getId().equals(graphNode.getId()))
        {
            return false;
        }

        if (!compareLabels)
        {
            return true;
        }

        if (label == null)
        {
            return (graphNode.getLabel() == null);
        }
        else
        {
            return (label.equals(graphNode.getLabel()));
        }
    }

    @Override
    public IGraphNode copy()
    {
        return new GraphNode(id, label);
    }

    @Override
    public String getInfo()
    {
        if ((label != null) && (!label.isEmpty()) && (!id.equals(label)))
        {
            return id + " " + label;
        }

        return id;
    }
}
