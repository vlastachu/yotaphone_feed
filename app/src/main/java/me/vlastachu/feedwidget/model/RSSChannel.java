package me.vlastachu.feedwidget.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(strict = false, name = "rss")
public class RSSChannel {
    @Element(name = "channel")
    Feed channel;
}
