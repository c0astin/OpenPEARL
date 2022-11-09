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
