package me.vlastachu.feedwidget.model;


import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(strict = false, name = "image")
public class Image {
    @Element(name = "url")
    String url;
}