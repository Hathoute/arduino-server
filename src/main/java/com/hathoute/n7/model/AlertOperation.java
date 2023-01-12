package com.hathoute.n7.model;
public record AlertOperation(int metricId, AlertOperationType type, float value) {

  public boolean isTriggered(final float value) {
    return switch (type) {
      case LT -> value < this.value;
      case GT -> value > this.value;
      case EQ -> value == this.value;
    };
  }
  
}
