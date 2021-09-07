package com.ubicron;

/** Copyright 2021 David Corbo
 *  Job definition
 *  Last edited: 9/4/21
 */

import java.util.*;
import java.lang.*;
import com.google.common.base.*;

public class JSONItem {

    private static final String IND = "    ";

    public enum Types {
        STRUCT,
        ARRAY,
        STRING,
        INT,
        DOUBLE,
        BOOLEAN,
        NULL
    }

    public Map<String, JSONItem> structValue;
    public List<String> itemOrder;
    public List<JSONItem> arrayValue;
    public String stringValue;
    public int intValue;
    public double doubleValue;
    public boolean boolValue;
    public Types type;

    private void fail(String json) throws JSONParseException {
        throw new JSONParseException("could not parse json item: " + json);
    }

    private Object[] getKey(String json, int start) throws JSONParseException {
        // returns the key and the index where the key ends
        String ret = "";
        int strStart = -1;
        boolean escaped = false;

        int i = start;

        while (i < json.length() && json.charAt(i) == ' ') i++;
        if (i == json.length() || json.charAt(i) != '"') fail(json);
        strStart = i;
        i++;
        while (i < json.length()) {
            if (json.charAt(i) == '\\') {
                escaped = true;
                i++;
            } else {
                if (json.charAt(i) == '"' && !escaped) {
                    return new Object[]{json.substring(strStart + 1, i), i + 1};
                }
                escaped = false;
                i++;
            }
        }
        fail(json);
        return null;
    }

    private Object[] getValue(String json, int start) throws JSONParseException {
        // returns the JSONItem value and the index where the value ends
        boolean inValue = false;
        Stack<Character> stack = new Stack<>();

        // System.err.println(json);
        // System.err.println(start);

        int i = start;
        while (i < json.length()) {
            char c = json.charAt(i);
            // System.err.println(c);
            switch (c) {
                case '{':
                    stack.add('{');
                    i++;
                    break;
                case '[':
                    stack.add('[');
                    i++;
                    break;
                case '}':
                    if (stack.isEmpty() || stack.peek() != '{') fail(json);
                    stack.pop();
                    if (stack.isEmpty()) {
                        return new Object[]{new JSONItem(json.substring(start, i + 1)), i + 1};
                    }
                    i++;
                    break;
                case ']':
                    if (stack.isEmpty() || stack.peek() != '[') fail(json);
                    stack.pop();
                    if (stack.isEmpty()) {
                        return new Object[]{new JSONItem(json.substring(start, i + 1)), i + 1};
                    }
                    i++;
                    break;
                case '"':
                    i = ((Integer) getKey(json, i)[1]).intValue();
                    break;
                case ',':
                    if (stack.isEmpty()) return new Object[]{new JSONItem(json.substring(start, i)), i};
                    i++;
                    break;
                case ' ':
                    if (stack.isEmpty() && inValue) {
                        return new Object[]{new JSONItem(json.substring(start, i)), i};
                    }
                    i++;
                    break;
                default:
                    if (!inValue) inValue = true;
                    i++;
                    break;
            }
        }

        if (!stack.isEmpty()) fail(json);
        // System.err.println(json.substring(start));
        return new Object[]{new JSONItem(json.substring(start)), i};
    }

    private Object[] parseStruct(String json) throws JSONParseException {
        if (json.charAt(json.length() - 1) != '}') fail(json);
        json = json.substring(1, json.length() - 1).trim();
        if (json.length() == 0) throw new JSONParseException("empty object");
        if (json.charAt(json.length() - 1) == ',') fail(json);
        Map<String, JSONItem> struct = new HashMap<>();
        List<String> order = new ArrayList<>();

        int i = 0;
        while (i < json.length()) {
            Object[] key = getKey(json, i);
            i = ((Integer) key[1]).intValue();
            while (i < json.length() && ' ' == json.charAt(i)) i++;
            if (json.length() == i || json.charAt(i) != ':') fail(json);
            else i++;
            Object[] value = getValue(json, i);
            struct.put((String) key[0], (JSONItem) value[0]);
            order.add((String) key[0]);
            i = ((Integer) value[1]).intValue();
            while (i < json.length() && ' ' == json.charAt(i)) i++;
            if (i == json.length()) break;
            else if (json.charAt(i) != ',') fail(json);
            else i++;
        }

        return new Object[]{struct, order};
    }

    private List<JSONItem> parseArray(String json) throws JSONParseException {
        if (json.charAt(json.length() - 1) != ']') fail(json);
        json = json.substring(1, json.length() - 1).trim();
        if (json.length() == 0) throw new JSONParseException("empty array");
        if (json.charAt(json.length() - 1) == ',') fail(json);
        List<JSONItem> array = new ArrayList<>();

        int i = 0;

        // System.err.println("IN");
        // System.err.println(json);
        while (i < json.length()) {
            Object[] value = getValue(json, i);
            // System.err.println("IN");
            i = ((Integer) value[1]).intValue();
            array.add((JSONItem) value[0]);
            while (i < json.length() && ' ' == json.charAt(i)) i++;
            if (i == json.length()) break;
            else if (json.charAt(i) != ',') fail(json);
            else i++;
        }

        return array;
    }

    public JSONItem(String json) throws JSONParseException {
        json = json.trim();
        if (json.length() == 0) {
            throw new JSONParseException("attempted to parse empty json item");
        }
        this.itemOrder = null;
        this.structValue = null;
        this.arrayValue = null;
        this.intValue = -1;
        this.doubleValue = 0.0;
        this.boolValue = false;

        // deciding type
        switch (json.charAt(0)) {
            case '{':
                this.type = Types.STRUCT;
                Object[] ret = parseStruct(json);
                this.structValue = (Map<String, JSONItem>) ret[0];
                this.itemOrder = (List<String>) ret[1];
                break;
            case '[':
                this.type = Types.ARRAY;
                this.arrayValue = parseArray(json);
                break;
            case '"':
                // System.err.println("Getting in");
                this.type = Types.STRING;
                if (json.charAt(json.length() - 1) != '"') fail(json);
                // System.err.println(json.substring(1, json.length() - 1));
                this.stringValue = json.substring(1, json.length() - 1);
                break;
            case 'n':
                this.type = Types.NULL;
                if (!json.equals("null")) {
                    fail(json);
                }
                break;
            case 't':
                this.type = Types.BOOLEAN;
                if (!json.equals("true")) {
                    fail(json);
                }
                this.boolValue = true;
                break;
            case 'f':
                this.type = Types.BOOLEAN;
                if (!json.equals("false")) {
                    fail(json);
                }
                this.boolValue = false;
                break;
            default:
                if (json.charAt(0) < '0' || json.charAt(0) > '9') {
                    fail(json);
                }
                if (json.contains(".")) {
                    this.type = Types.DOUBLE;
                    try {
                        this.doubleValue = Double.parseDouble(json);
                    } catch (NumberFormatException e) {
                        fail(json);
                    }
                } else {
                    this.type = Types.INT;
                    try {
                        this.intValue = Integer.parseInt(json);
                    } catch (NumberFormatException e) {
                        fail(json);
                    }
                }
                break;
        }
    }

    public String print(int indent) {
        StringBuilder builder = new StringBuilder();
        switch (this.type) {
            case STRUCT:
                builder.append("{\n");
                for (int i = 0; i < this.itemOrder.size(); i++) {
                    String key = this.itemOrder.get(i);
                    builder.append(Strings.repeat(IND, indent + 1));
                    builder.append("\"" + key + "\": ");
                    builder.append(this.structValue.get(key).print(indent + 1));
                    if (i < this.itemOrder.size() - 1) builder.append(",");
                    builder.append("\n");
                }
                builder.append(Strings.repeat(IND, indent) + "}");
                break;
            case ARRAY:
                builder.append("[\n");
                for (int i = 0; i < this.arrayValue.size(); i++) {
                    builder.append(Strings.repeat(IND, indent + 1));
                    builder.append(this.arrayValue.get(i).print(indent + 1));
                    if (i < this.arrayValue.size() - 1) builder.append(",");
                    builder.append("\n");
                }
                builder.append(Strings.repeat(IND, indent) + "]");
                break;
            case STRING:
                builder.append("\"" + stringValue + "\"");
                break;
            case INT:
                builder.append(intValue);
                break;
            case DOUBLE:
                builder.append(doubleValue);
                break;
            case BOOLEAN:
                builder.append(boolValue);
                break;
            case NULL:
                builder.append("null");
                break;
        }
        return builder.toString();
    }

    public String print() {
        return print(0);
    }

    public JSONItem get(String key) throws JSONAccessException {
        if (key.length() == 0) return this;

        int splitPos = key.indexOf(".");
        if (splitPos == key.length() - 1) {
            throw new JSONAccessException("access path cannot end with .");
        }
        String path = "";
        if (splitPos != -1) {
            path = key.substring(splitPos + 1);
            key = key.substring(0, splitPos);
        }

        switch (this.type) {
            case STRUCT:
                if (structValue.containsKey(key)) {
                    return structValue.get(key).get(path);
                } else {
                    throw new JSONAccessException("object doesn't contain key " + key);
                }
            case ARRAY:
                try {
                    int index = Integer.parseInt(key);
                    if (index < 0 || index >= arrayValue.size()) {
                        throw new JSONAccessException("array access out of bounds");
                    }
                    return arrayValue.get(index).get(path);
                } catch (NumberFormatException e) {
                    throw new JSONAccessException("arrays can only be accessed by integer");
                }
        }
        throw new JSONAccessException("you can only access objects and arrays");
    }
}
