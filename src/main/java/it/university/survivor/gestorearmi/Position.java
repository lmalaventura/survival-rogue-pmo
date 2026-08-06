public record Position(double x, double y) {
    public double distanceTo(Position other) {
        return Math.hypot(this.x - other.x(), this.y - other.y());
    }
}