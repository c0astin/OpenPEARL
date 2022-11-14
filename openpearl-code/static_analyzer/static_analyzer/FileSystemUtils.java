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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileSystemUtils
{
    public static synchronized boolean fileExists(String file)
    {
        return Files.exists(Paths.get(file));
    }

    public static synchronized String readFileAsString(String file)
    {
        String result = "";

        try {
            byte[] encoded = Files.readAllBytes(Paths.get(file));
            result = new String(encoded, Charset.defaultCharset());
        } catch (Exception ignored) {}

        return result;
    }

    public static synchronized boolean writeStringToFile(String file, String content, boolean append)
    {
        try
        {
            File fileObject = new File(file);
            String directoryPath = fileObject.getParent();
            Files.createDirectories(Paths.get(directoryPath));
        }
        catch (Exception ignored) {}

        try
        {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, append));
            writer.write(content);
            writer.close();
            return true;
        }
        catch (Exception ignored) {}

        return false;
    }

    public static synchronized void clearFileContent(String file)
    {
        try
        {
            new FileWriter(file, false).close();
        }
        catch (Exception ignored) {}
    }
}
