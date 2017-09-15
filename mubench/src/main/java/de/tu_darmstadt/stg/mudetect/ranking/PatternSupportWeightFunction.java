package de.tu_darmstadt.stg.mudetect.ranking;

import de.tu_darmstadt.stg.mudetect.mining.Model;
import de.tu_darmstadt.stg.mudetect.model.Overlap;
import de.tu_darmstadt.stg.mudetect.model.Overlaps;

public class PatternSupportWeightFunction implements ViolationWeightFunction {
    @Override
    public double getWeight(Overlap violation, Overlaps overlaps, Model model) {
        return violation.getPattern().getSupport() / (double) model.getMaxPatternSupport();
    }

    @Override
    public String getFormula(Overlap violation, Overlaps overlaps, Model model) {
        return String.format("pattern support = %d / %d", violation.getPattern().getSupport(), model.getMaxPatternSupport());
    }
}