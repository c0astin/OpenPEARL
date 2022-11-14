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

package static_analyzer.AnalyzerMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MessageStack
{
    private final List<Message> messages = new ArrayList<>();

    public void add(Message message)
    {
        for (Message currentMessage : messages)
        {
            if (currentMessage.isSame(message))
            {
                currentMessage.actualizePriority(message);

                return;
            }
        }

        messages.add(message);
    }

    public void remove(String problem)
    {
        List<Message> removedMessages = new ArrayList<>();

        for (Message currentMessage : messages)
        {
            if (currentMessage.problem.equals(problem))
            {
                removedMessages.add(currentMessage);
            }
        }

        messages.removeAll(removedMessages);
    }

    public List<Message> get(Message.Severity severity)
    {
        List<Message> currentSeverityMessages = new ArrayList<>();

        for (Message currentMessage : messages)
        {
            if (currentMessage.severity == severity)
            {
                currentSeverityMessages.add(currentMessage);
            }
        }

        return currentSeverityMessages;
    }

    public Message getByProblem(String problem)
    {
        for (Message currentMessage : messages)
        {
            if (Objects.equals(currentMessage.problem, problem))
            {
                return currentMessage;
            }
        }

        return null;
    }
}
