package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.aug.model.Node;
import de.tu_darmstadt.stg.mudetect.aug.model.actions.MethodCallNode;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class VeryPrevalentNodePredicate implements Predicate<Node> {
    private static final Set<String> veryUnspecificTypes = new HashSet<>();

    static {
        veryUnspecificTypes.add("Object");
        veryUnspecificTypes.add("Class");
        veryUnspecificTypes.add("System");
        veryUnspecificTypes.add("Throwable");
        veryUnspecificTypes.add("Exception");
    }

    @Override
    public boolean test(Node node) {
        if (node instanceof MethodCallNode) {
            String declaringTypeName = ((MethodCallNode) node).getDeclaringTypeName();
            return veryUnspecificTypes.contains(declaringTypeName);
        }
        return false;
    }
}
