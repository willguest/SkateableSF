package com.example.skateable_sf

public class Note {

    public var principal: String = ""
    public var value: String = ""

    public constructor()

    public constructor(principal: String, value: String) {
        this.principal = principal
        this.value = value
    }
}