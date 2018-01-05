package com.ninjarific.radiomesh.world.worldgenerator

import com.ninjarific.radiomesh.coordinates.Bounds
import com.ninjarific.radiomesh.world.data.Center
import com.ninjarific.radiomesh.world.data.Corner
import com.ninjarific.radiomesh.world.data.Edge

data class MapData(val seed: Long,
                   val centers: List<Center>,
                   val corners: List<Corner>,
                   val edges: List<Edge>,
                   val bounds: Bounds)