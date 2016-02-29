package com.sbbic.net;

import java.io.Serializable;

/**
 * Created by God on 2016/2/29.
 */
public class RequestParameter implements Serializable {
    private String name;
    private String value;

    public RequestParameter(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
