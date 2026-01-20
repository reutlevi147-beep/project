package com.mycasa.app;

import java.util.List;

public class FlowCategory {
    public String id;
    public String title;
    public List<FlowItem> items;
    public boolean isExpanded = false;

    public FlowCategory(String id, String title, List<FlowItem> items) {
        this.id = id;
        this.title = title;
        this.items = items;
    }
}
