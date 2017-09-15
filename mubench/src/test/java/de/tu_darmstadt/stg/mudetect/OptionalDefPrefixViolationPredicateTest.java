package de.tu_darmstadt.stg.mudetect;

import de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder;
import de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder;
import org.junit.Test;

import java.util.Optional;

import static de.tu_darmstadt.stg.mudetect.model.TestAUGBuilder.buildAUG;
import static de.tu_darmstadt.stg.mudetect.model.TestOverlapBuilder.buildOverlap;
import static egroum.EGroumDataEdge.Type.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class OptionalDefPrefixViolationPredicateTest {
    @Test
    public void missingNonDefPrefixIsNoDecision() throws Exception {
        TestOverlapBuilder overlap = buildOverlap(buildAUG(), buildAUG().withActionNode("m()"));

        Optional<Boolean> decision = new OptionalDefPrefixViolationPredicate().apply(overlap.build());

        assertThat(decision, is(Optional.empty()));
    }

    @Test
    public void missingDefPrefixAndMoreIsNoDecision() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNodes("create()", "use()", "use2()").withDataNode("Object")
                .withDataEdge("create()", DEFINITION, "Object")
                .withDataEdge("Object", RECEIVER, "use()").withDataEdge("create()", RECEIVER, "use()")
                .withDataEdge("Object", RECEIVER, "use2()").withDataEdge("create()", RECEIVER, "use2()");
        TestAUGBuilder target = buildAUG().withActionNodes("use()").withDataNode("Object")
                .withDataEdge("Object", RECEIVER, "use()");
        TestOverlapBuilder overlap = buildOverlap(target, pattern).withNodes("use()", "Object")
                .withEdge("Object", RECEIVER, "use()");

        Optional<Boolean> decision = new OptionalDefPrefixViolationPredicate().apply(overlap.build());

        assertThat(decision, is(Optional.empty()));
    }

    @Test
    public void missingDefPrefixAndEdgeBetweenTwoMappedNodesIsNoDecision() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNodes("create()", "use()", "use2()").withDataNode("Object")
                .withDataEdge("create()", DEFINITION, "Object")
                .withDataEdge("Object", RECEIVER, "use()").withDataEdge("create()", RECEIVER, "use()")
                .withDataEdge("Object", RECEIVER, "use2()").withDataEdge("create()", RECEIVER, "use2()")
                .withDataEdge("use()", ORDER, "use2()");
        TestAUGBuilder target = buildAUG().withActionNodes("use()", "use2()").withDataNode("Object")
                .withDataEdge("Object", RECEIVER, "use()")
                .withDataEdge("Object", RECEIVER, "use2()");
        TestOverlapBuilder overlap = buildOverlap(target, pattern).withNodes("use()", "use2()", "Object")
                .withEdge("Object", RECEIVER, "use()")
                .withEdge("Object", RECEIVER, "use2()");

        Optional<Boolean> decision = new OptionalDefPrefixViolationPredicate().apply(overlap.build());

        assertThat(decision, is(Optional.empty()));
    }

    @Test
    public void missingDefPrefixIsNoViolation() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNodes("create()", "use()").withDataNode("Object")
                .withDataEdge("create()", DEFINITION, "Object")
                .withDataEdge("Object", RECEIVER, "use()").withDataEdge("create()", RECEIVER, "use()");
        TestAUGBuilder target = buildAUG().withActionNodes("use()").withDataNode("Object")
                .withDataEdge("Object", RECEIVER, "use()");
        TestOverlapBuilder overlap = buildOverlap(target, pattern).withNodes("use()", "Object")
                .withEdge("Object", RECEIVER, "use()");

        Optional<Boolean> decision = new OptionalDefPrefixViolationPredicate().apply(overlap.build());

        assertThat(decision, is(Optional.of(false)));
    }

    @Test
    public void missingDefPrefixWithPredecessorIsNoViolation() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNodes("create()", "use()").withDataNodes("Creator", "Object")
                .withDataEdge("Creator", RECEIVER, "create()")
                .withDataEdge("create()", DEFINITION, "Object")
                .withDataEdge("Object", RECEIVER, "use()").withDataEdge("create()", RECEIVER, "use()");
        TestAUGBuilder target = buildAUG().withActionNodes("use()").withDataNodes("Object")
                .withDataEdge("Object", RECEIVER, "use()");
        TestOverlapBuilder overlap = buildOverlap(target, pattern).withNodes("use()", "Object")
                .withEdge("Object", RECEIVER, "use()");

        Optional<Boolean> decision = new OptionalDefPrefixViolationPredicate().apply(overlap.build());

        assertThat(decision, is(Optional.of(false)));
    }

    @Test
    public void missingDefPrefixWithConditionEdgeIsNoViolation() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNodes("iterator()", "hasNext()", "next()").withDataNode("Iterator")
                .withDataEdge("iterator()", DEFINITION, "Iterator")
                .withDataEdge("Iterator", RECEIVER, "hasNext()")
                .withDataEdge("Iterator", RECEIVER, "next()")
                .withCondEdge("iterator()", "rep", "next()")
                .withCondEdge("hasNext()", "rep", "next()");
        TestAUGBuilder target = buildAUG().withActionNodes("hasNext()", "next()").withDataNode("Iterator")
                .withDataEdge("Iterator", RECEIVER, "hasNext()")
                .withDataEdge("Iterator", RECEIVER, "next()")
                .withCondEdge("hasNext()", "rep", "next()");
        TestOverlapBuilder overlap = buildOverlap(target, pattern).withNodes("hasNext()", "next()", "Iterator")
                .withEdge("Iterator", RECEIVER, "hasNext()")
                .withEdge("Iterator", RECEIVER, "next()")
                .withEdge("hasNext()", CONDITION, "next()");

        Optional<Boolean> decision = new OptionalDefPrefixViolationPredicate().apply(overlap.build());

        assertThat(decision, is(Optional.of(false)));
    }

    /**
     * In the following example, the usage of i2 might be mapped together with the iterator() call that creates i1,
     * because there is a rep edges from the i1 usage to the i2 usage. In this case only the def edge is missing.
     * However, this is still only a missing def prefix for the i2 usage.
     * <pre>
     *     Iterator i1 = iterator();
     *     while (i1.hasNext()) {
     *       Iterator i2 = i1.next();
     *       while(i2.hasNext()) {
     *         i2.next();
     *       }
     *     }
     * </pre>
     */
    @Test
    public void missingDefPrefixWithConditionEdgeToOtherDefinitionIsNoViolation() throws Exception {
        TestAUGBuilder pattern = buildAUG().withActionNodes("iterator()", "hasNext()", "next()").withDataNode("Iterator")
                .withDataEdge("iterator()", DEFINITION, "Iterator")
                .withDataEdge("Iterator", RECEIVER, "hasNext()")
                .withDataEdge("Iterator", RECEIVER, "next()")
                .withCondEdge("iterator()", "rep", "next()")
                .withCondEdge("hasNext()", "rep", "next()");
        TestAUGBuilder target = buildAUG().withActionNodes("iterator()", "hasNext()", "next()").withDataNode("Iterator")
                .withDataEdge("Iterator", RECEIVER, "hasNext()")
                .withDataEdge("Iterator", RECEIVER, "next()")
                .withCondEdge("iterator()", "rep", "next()")
                .withCondEdge("hasNext()", "rep", "next()");
        TestOverlapBuilder overlap = buildOverlap(target, pattern).withNodes("iterator()", "hasNext()", "next()", "Iterator")
                .withEdge("Iterator", RECEIVER, "hasNext()")
                .withEdge("Iterator", RECEIVER, "next()")
                .withEdge("iterator()", CONDITION, "next()")
                .withEdge("hasNext()", CONDITION, "next()");

        Optional<Boolean> decision = new OptionalDefPrefixViolationPredicate().apply(overlap.build());

        assertThat(decision, is(Optional.of(false)));
    }
}