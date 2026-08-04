package it.university.survivor.model;

public record Position(double x, double y) {

    public Position {
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException("Coordinates must be finite");
        }
    }
}
