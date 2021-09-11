package com.ubicron;

/** Copyright 2021 David Corbo
 *  Job definition
 *  Last edited: 9/4/21
 */

import java.util.*;
import java.lang.*;

public class Input {

    private JSONItem tree;
    private List<String> keys;
    
    private List<String> recKeys(JSONItem input) throws JSONAccessException {
        List<String> ret = new ArrayList<>();
        if (input.type != JSONItem.Types.STRUCT) {
            ret.add("");
            return ret;
        }
        for (String item : input.itemOrder) {
            for (String path : recKeys(input.get(item))) {
                ret.add(item + "." + path);
            }
        }
        return ret;
    }

    public Input(JSONItem inputJSON) throws JSONAccessException {
        this.tree = inputJSON;
        List<String> dottedKeys = recKeys(inputJSON);
        this.keys = new ArrayList<>();
        for (String key : dottedKeys) {
            this.keys.add(key.substring(0, key.length() - 1));
        }
    }

    public JSONItem getTree() {
        return this.tree;
    }

    public String getValue(String path) throws JSONAccessException {
        return this.tree.get(path).print();
    }

    public List<String> getKeys() {
        return this.keys;
    }

    public Input merge(Input input) {
        try {
            JSONItem mergedTree = new JSONItem("{}");
            for (String key : this.keys) {
                mergedTree.insert(key, this.tree.get(key));
            }
            for (String key : input.getKeys()) {
                mergedTree.insert(key, input.getTree().get(key));
            }
            return new Input(mergedTree);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    public Input fillSubCall(Input genInput) throws JobExecutionException {
        System.out.println(this);
        System.out.println(genInput);
        try {
            JSONItem filledSub = new JSONItem("{}");
            for (String key : this.keys) {
                filledSub.insert(key, genInput.getTree().get(this.tree.get(key).stringValue));
            }
            return new Input(filledSub);
        } catch (Exception e) {
            throw new JobExecutionException("couldn't fill sub call");
        }
        
    }

    public String toString() {
        try {
            StringBuilder builder = new StringBuilder();
            for (String key : this.keys) {
                builder.append(key + ": " + this.tree.get(key).print() + "\n");
            }
            return builder.toString();
        } catch (JSONAccessException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }
}