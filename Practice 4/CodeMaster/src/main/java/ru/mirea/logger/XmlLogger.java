package ru.mirea.logger;

import ru.mirea.Utils;

import java.beans.XMLEncoder;
import java.io.*;
import java.nio.charset.Charset;

public class XmlLogger implements Logger {

    private final String logDir = "logs";
    private final String logFile = "game.xml";

    @Override
    public void save(Object o) {
        File file = new File(logDir, Utils.getUniqueFileName(logFile));
        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();
        try(XMLEncoder encoder = new XMLEncoder(
                new FileOutputStream(file),
                Charset.defaultCharset().name(),
                true,
                0
        )) {
            encoder.writeObject(o);
        } catch (IOException e) {
            System.err.println("Error writing to log file");
        }
    }

}
