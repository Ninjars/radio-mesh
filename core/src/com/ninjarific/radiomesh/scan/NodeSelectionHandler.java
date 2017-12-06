package com.ninjarific.radiomesh.scan;

import com.ninjarific.radiomesh.scan.nodes.IPositionProvider;

public interface NodeSelectionHandler<T extends IPositionProvider> {
    void onNodeSelected(T selectedNode);
}
