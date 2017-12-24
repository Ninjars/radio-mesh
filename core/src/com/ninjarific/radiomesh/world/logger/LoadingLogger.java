package com.ninjarific.radiomesh.world.logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LoadingLogger {

    private final ILogger logger;
    private HashMap<String, Long> stages;
    private HashMap<String, Long> loops;
    private HashMap<String, List<Long>> loopIterations;

    private long startMs;

    public LoadingLogger(ILogger logger) {
        this.logger = logger;
    }

    private long getTimeMs() {
        return System.nanoTime() / 1000000;
    }

    private long getTimeNs() {
        return System.nanoTime() / 1000000;
    }

    public void start() {
        startMs = getTimeMs();
        logger.log("Beginning world generation");
        stages = new HashMap<>();
        loops = new HashMap<>();
        loopIterations = new HashMap<>();
    }

    public void beginningStage(String stage) {
        stages.put(stage, getTimeMs());
        logger.log("Begin " + stage);
    }

    public void completedStage(String stage) {
        logger.log("Completed " + stage + " ----:---- " + (getTimeMs() - stages.get(stage)));
    }

    public void end() {
        logger.log("Completed world generation " + (getTimeMs() - startMs));
        stages.clear();
        loops.clear();
        loopIterations.clear();
        stages = null;
        loops = null;
        loopIterations = null;
        startMs = -1;
    }

    public void startLoop(String tag) {
        loops.put(tag, getTimeMs());
        loopIterations.put(tag, new ArrayList<>());
        logger.log("Beginning loop  " + tag);
    }

    public void startLoopIteration(String tag) {
        List<Long> currentList = loopIterations.get(tag);
        currentList.add(getTimeNs());
        loopIterations.put(tag, currentList);
    }

    public void endLoopIteration(String tag) {
        List<Long> currentList = loopIterations.get(tag);
        int index = currentList.size() - 1;
        currentList.set(index, getTimeNs() - currentList.get(index));
        loopIterations.put(tag, currentList);
    }

    public void endLoop(String tag) {
        List<Long> values = loopIterations.get(tag);
        long sum = 0;
        for (long val : values) {
            sum += val;
        }
        logger.log("Completed loop  " + tag
                + "\n> duration ms  " + (getTimeMs() - loops.get(tag))
                + "\n> iterations   " + values.size()
                + "\n> avg iteration duration ns " + (sum / values.size()));
    }
}
