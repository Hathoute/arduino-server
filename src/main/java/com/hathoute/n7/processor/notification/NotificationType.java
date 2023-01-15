package com.hathoute.n7.processor.notification;

import com.hathoute.n7.model.AlertOperation;

public interface NotificationType {
  void notify(AlertOperation alertOperation, float value);
}
