package org.automation.records;

import java.util.Map;

public record Action(
        String actionType,
        String locator,
        String[] arguments,
        String testcaseId,
//        String expectedResultType, // e.g., "text", "url", "visible"
//        String expectedResult, // e.g., "Welcome", "https://dashboard", "#logout"
        Map<String,String> additionalData,
        String epic,
        String feature,
        String story,
        String description
){}

/*
private final Map<String, Consumer<Action>> handlers = Map.of(
    "gotoUrl", this::gotoUrl,
    "click",   this::click,
    "type",    this::type
);

@Override
public void execute(Action action) {
    Consumer<Action> handler = handlers.get(action.action_type());
    if (handler == null) throw new IllegalArgumentException("Unknown: " + action.action_type());
    handler.accept(action);
}
 */