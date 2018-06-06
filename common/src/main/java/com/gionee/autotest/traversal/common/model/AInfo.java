package com.gionee.autotest.traversal.common.model;

public class AInfo{
    public String name ;
    public String label ;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AInfo aInfo = (AInfo) o;

        if (!name.equals(aInfo.name)) return false;
        return label.equals(aInfo.label);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + label.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "AInfo{" +
                "name='" + name + '\'' +
                ", label='" + label + '\'' +
                '}';
    }
}