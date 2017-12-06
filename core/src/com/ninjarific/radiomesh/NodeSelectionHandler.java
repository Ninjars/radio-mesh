package com.ninjarific.radiomesh;

import com.ninjarific.radiomesh.nodes.IPositionProvider;

public interface NodeSelectionHandler<T extends IPositionProvider> {
    void onNodeSelected(T selectedNode);
}
