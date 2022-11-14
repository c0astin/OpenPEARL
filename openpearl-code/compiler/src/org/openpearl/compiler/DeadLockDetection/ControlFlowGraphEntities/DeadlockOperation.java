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


package org.openpearl.compiler.DeadLockDetection.ControlFlowGraphEntities;

import java.util.ArrayList;
import java.util.List;

public class DeadlockOperation extends AbstractControlFlowGraphEntity
{
    public static String RESOURCE_TYPE_SEMAPHORE = "SEMA";
    public static String ACTION_TYPE_REQUEST = "REQUEST";
    public static String ACTION_TYPE_RELEASE = "RELEASE";

    public static String RESOURCE_TYPE_BOLT = "BOLT";
    public static String ACTION_TYPE_ENTER = "ENTER";
    public static String ACTION_TYPE_LEAVE = "LEAVE";
    public static String ACTION_TYPE_RESERVE = "RESERVE";
    public static String ACTION_TYPE_FREE = "FREE";

    public static String CONTEXT_TYPE_TASK = "TASK";
    public static String CONTEXT_TYPE_PROCEDURE = "PROCEDURE";

    public String contextType = "";
    public String contextIdentifier = "";
    public String actionType = "";
    public String resourcesType = "";
    public List<String> resourceIdentifiers = new ArrayList<>();

    public List<String> executingTasks = new ArrayList<>();
}
