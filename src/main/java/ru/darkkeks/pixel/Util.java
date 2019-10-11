package ru.darkkeks.pixel;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.StringWriter;

public class Util {

    private static final String PREFIX = "window = {\n" +
            "    Math: Math,\n" +
            "    parseInt: parseInt,\n" +
            "    location: { \n" +
            "        host: \"https://vk.com\" \n" +
            "    },\n" +
            "    WebSocket: {kek:1}\n" +
            "};";

    public static String evaluateJS(String js) {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
        ScriptContext context = engine.getContext();
        StringWriter writer = new StringWriter();
        context.setWriter(writer);

        js = PREFIX + js;

        try {
            Object obj = engine.eval(js);
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
