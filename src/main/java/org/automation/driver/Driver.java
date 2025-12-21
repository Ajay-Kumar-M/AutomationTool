package org.automation.driver;

import org.automation.records.Action;

public interface Driver {
    void init(String browser);
//    void gotoUrl(String url);
//    void type(String locator, String text);
//    void click(String locator);
    void execute(Action action);
    void close();
}
