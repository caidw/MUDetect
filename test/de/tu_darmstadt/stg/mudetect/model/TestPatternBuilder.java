package de.tu_darmstadt.stg.mudetect.model;

import egroum.EGroumEdge;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.someAUG;

public class TestPatternBuilder {
    public static Pattern somePattern() {
        return somePattern(someAUG());
    }

    public static Pattern somePattern(AUG patternAUG) {
        return somePattern(patternAUG, 1);
    }

    public static Pattern somePattern(AUG patternAUG, int support) {
        Pattern pattern = new Pattern(support);
        patternAUG.vertexSet().forEach(pattern::addVertex);
        for (EGroumEdge edge : patternAUG.edgeSet()) {
            pattern.addEdge(edge.getSource(), edge.getTarget(), edge);
        }
        return pattern;
    }

    public static Pattern somePattern(TestAUGBuilder builder) {
        return somePattern(builder.build());
    }

    public static Pattern somePattern(TestAUGBuilder builder, int support) {
        return somePattern(builder.build(), support);
    }
}
