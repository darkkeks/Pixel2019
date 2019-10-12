package ru.darkkeks.pixel;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.StringWriter;

public class Util {

    private static final ScriptEngine ENGINE = new ScriptEngineManager().getEngineByName("javascript");

    static {
        ScriptContext context = ENGINE.getContext();
        StringWriter writer = new StringWriter();
        context.setWriter(writer);
    }

    private static final String PREFIX = "window = {\n" +
            "    Math: Math,\n" +
            "    parseInt: parseInt,\n" +
            "    location: { \n" +
            "        host: \"https://vk.com\" \n" +
            "    },\n" +
            "    WebSocket: {kek:1}\n" +
            "};\n" +
            "document = {createElement: function(name) {return {tagName: name.toUpperCase()};}};\n";

    public static String evaluateJS(String js) {
        js = PREFIX + js;

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
