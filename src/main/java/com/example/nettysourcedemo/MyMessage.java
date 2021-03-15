package com.example.nettysourcedemo;

import lombok.Data;

import java.io.Serializable;

@Data
public class MyMessage implements Serializable {

    private int code ;
    private String message;
    private Object msg;
}
