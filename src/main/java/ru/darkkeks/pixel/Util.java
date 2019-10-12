package ru.darkkeks.pixel;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.StringWriter;

public class Util {

    private static final ScriptEngine ENGINE = new ScriptEngineManager().getEngineByName("javascript");

    private static final String PREFIX =
            "window = {\n" +
                    "    Math: Math,\n" +
                    "    parseInt: parseInt,\n" +
                    "    location: { \n" +
                    "        host: \"https://vk.com\" \n" +
                    "    },\n" +
                    "    WebSocket: {kek:1}\n" +
                    "};\n" +
                    "document = {\n" +
                    "    createElement: function(name) {\n" +
                    "         return {\n" +
                    "             tagName: name.toUpperCase()\n" +
                    "         };\n" +
                    "    }\n" +
                    "};\n" +
                    "setTimeout = function() { return 0; }; \n";

    static {
        ScriptContext context = ENGINE.getContext();
        StringWriter writer = new StringWriter();
        context.setWriter(writer);
        try {
            ENGINE.eval(PREFIX);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    public static String evaluateJS(String js) {
        try {
            Object obj = ENGINE.eval(js);
            if(obj instanceof Double) {
                return String.valueOf(((Double) obj).intValue());
            }
            return String.valueOf(obj);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException(js);
    }

}
