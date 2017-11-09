package com.ninjarific.radiomesh.forcedirectedgraph;

import com.ninjarific.radiomesh.nodes.INode;

public class ForceConnection {

    public final INode from;
    public final INode to;
    private final int hashCode;

    public ForceConnection(INode from, INode to) {
        this.from = from;
        this.to = to;
        this.hashCode =  17 * Math.max(to.hashCode(), from.hashCode()) + 3 * Math.min(to.hashCode(), from.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ForceConnection
                && (((ForceConnection) obj).from == from || ((ForceConnection) obj).from == to)
                && (((ForceConnection) obj).to == from || ((ForceConnection) obj).to == to);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    public boolean contains(INode node) {
        return from == node || to == node;
    }
}
