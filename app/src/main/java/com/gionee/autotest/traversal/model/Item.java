package com.gionee.autotest.traversal.model;

public class Item {
    String timestamp;

    boolean visited;

    public Item(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

        return timestamp != null ? timestamp.equals(item.timestamp) : item.timestamp == null;
    }

    @Override
    public int hashCode() {
        return timestamp != null ? timestamp.hashCode() : 0;
    }
}