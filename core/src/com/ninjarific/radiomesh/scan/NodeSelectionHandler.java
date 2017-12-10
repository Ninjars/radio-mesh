package com.ninjarific.radiomesh.scan;

import com.ninjarific.radiomesh.scan.radialgraph.IPositionProvider;

public interface NodeSelectionHandler<T extends IPositionProvider> {
    void onNodeSelected(T selectedNode);
}
