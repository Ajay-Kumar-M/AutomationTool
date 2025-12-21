package org.automation.records;

import java.util.Map;

public record Action(
        String action_type,
        String locator,
        String[] arguments,
        String testcase_id,
        String expected_result, // e.g., "text:Welcome", "url:https://dashboard", "visible:#logout"
        Map<String,String> additional_data
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